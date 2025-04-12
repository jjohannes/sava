package software.sava.rpc.json.http.response;

import software.sava.core.accounts.PublicKey;
import software.sava.rpc.json.PublicKeyEncoding;
import systems.comodal.jsoniter.ContextFieldBufferPredicate;
import systems.comodal.jsoniter.FieldBufferPredicate;
import systems.comodal.jsoniter.JsonIterator;

import java.util.ArrayList;
import java.util.List;
import java.util.OptionalInt;
import java.util.SequencedCollection;

import static java.util.Objects.requireNonNullElse;
import static software.sava.rpc.json.http.response.AccountInfo.BYTES_IDENTITY;
import static software.sava.rpc.json.http.response.AccountInfo.parseAccountsFromKeys;
import static software.sava.rpc.json.http.response.JsonUtil.parseEncodedData;
import static systems.comodal.jsoniter.JsonIterator.fieldEquals;

public record TxSimulation(Context context,
                           TransactionError error,
                           List<String> logs,
                           List<AccountInfo<byte[]>> accounts,
                           List<InnerInstructions> innerInstructions,
                           ReplacementBlockHash replacementBlockHash,
                           OptionalInt unitsConsumed,
                           PublicKey programId,
                           byte[] data) {

  public static TxSimulation parse(final SequencedCollection<PublicKey> accounts,
                                   final JsonIterator ji,
                                   final Context context) {
    final var parser = new Parser(context, accounts);
    ji.testObject(parser);
    return parser.create();
  }

  public static TxSimulation parse(final JsonIterator ji) {
    return parse(null, ji, null);
  }

  private static final ContextFieldBufferPredicate<Parser> RETURN_DATA_PARSER = (parser, buf, offset, len, ji) -> {
    if (fieldEquals("programId", buf, offset, len)) {
      parser.programId = PublicKeyEncoding.parseBase58Encoded(ji);
    } else if (fieldEquals("data", buf, offset, len)) {
      parser.data = parseEncodedData(ji);
    } else {
      ji.skip();
    }
    return true;
  };

  private static final OptionalInt NO_BUDGET = OptionalInt.empty();
  static final List<String> NO_LOGS = List.of();
  static final List<AccountInfo<byte[]>> NO_ACCOUNTS = List.of();
  static final List<InnerInstructions> NO_INNER_INSTRUCTIONS = List.of();

  private static final class Parser extends RootBuilder implements FieldBufferPredicate {

    private final SequencedCollection<PublicKey> accountPubKeys;
    private TransactionError error;
    private List<String> logs;
    private List<InnerInstructions> innerInstructions;
    private List<AccountInfo<byte[]>> accounts;
    private ReplacementBlockHash replacementBlockHash;
    private int unitsConsumed = -1;
    private PublicKey programId;
    private byte[] data;

    private Parser(final Context context, final SequencedCollection<PublicKey> accountPubKeys) {
      super(context);
      this.accountPubKeys = accountPubKeys;
    }

    private TxSimulation create() {
      return new TxSimulation(
          context,
          error,
          logs == null || logs.isEmpty() ? NO_LOGS : logs,
          accounts == null || accounts.isEmpty() ? NO_ACCOUNTS : accounts,
          requireNonNullElse(innerInstructions, NO_INNER_INSTRUCTIONS),
          replacementBlockHash,
          unitsConsumed < 0 ? NO_BUDGET : OptionalInt.of(unitsConsumed),
          programId,
          data
      );
    }

    @Override
    public boolean test(final char[] buf, final int offset, final int len, final JsonIterator ji) {
      if (fieldEquals("err", buf, offset, len)) {
        error = TransactionError.parseError(ji);
      } else if (fieldEquals("logs", buf, offset, len)) {
        this.logs = new ArrayList<>();
        while (ji.readArray()) {
          logs.add(ji.readString());
        }
      } else if (fieldEquals("accounts", buf, offset, len)) {
        if (accountPubKeys == null || accountPubKeys.isEmpty()) {
          ji.skip();
        } else {
          accounts = parseAccountsFromKeys(accountPubKeys, ji, context, BYTES_IDENTITY);
        }
      } else if (fieldEquals("unitsConsumed", buf, offset, len)) {
        unitsConsumed = ji.readInt();
      } else if (fieldEquals("returnData", buf, offset, len)) {
        ji.testObject(this, RETURN_DATA_PARSER);
      } else if (fieldEquals("innerInstructions", buf, offset, len)) {
        innerInstructions = InnerInstructions.parseInstructions(ji);
      } else if (fieldEquals("replacementBlockhash", buf, offset, len)) {
        replacementBlockHash = ReplacementBlockHash.parse(ji);
      } else {
        ji.skip();
      }
      return true;
    }
  }
}
