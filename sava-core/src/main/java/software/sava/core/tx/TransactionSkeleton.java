package software.sava.core.tx;

import software.sava.core.accounts.PublicKey;
import software.sava.core.accounts.lookup.AddressLookupTable;
import software.sava.core.accounts.meta.AccountMeta;
import software.sava.core.accounts.meta.LookupTableAccountMeta;
import software.sava.core.programs.Discriminator;

import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.encoding.CompactU16Encoding.*;
import static software.sava.core.tx.Transaction.SIGNATURE_LENGTH;
import static software.sava.core.tx.Transaction.VERSIONED_BIT_MASK;
import static software.sava.core.tx.TransactionSkeletonRecord.LEGACY_INVOKED_INDEXES;
import static software.sava.core.tx.TransactionSkeletonRecord.NO_TABLES;

public interface TransactionSkeleton {

  static TransactionSkeleton deserializeSkeleton(final byte[] data) {
    int o = 0;
    final int numSignatures = decode(data, o);
    o += getByteLen(data, o);
    o += (numSignatures * SIGNATURE_LENGTH);
    final int messageOffset = o;

    int version = data[o++] & 0xFF;
    final int numRequiredSignatures;
    if (signedByte(version)) {
      numRequiredSignatures = data[o++];
      version &= 0x7F;
    } else {
      numRequiredSignatures = version;
      version = VERSIONED_BIT_MASK;
    }
    final int numReadonlySignedAccounts = data[o++];
    final int numReadonlyUnsignedAccounts = data[o++];

    final int numIncludedAccounts = decode(data, o);
    o += getByteLen(data, o);
    final int accountsOffset = o;
    o += numIncludedAccounts << 5;

    final int recentBlockHashIndex = o;
    o += Transaction.BLOCK_HASH_LENGTH;

    final int numInstructions = decode(data, o);
    o += getByteLen(data, o);
    final int instructionsOffset = o;

    if (version >= 0) {
      final int[] invokedIndexes = new int[numInstructions];
      for (int i = 0, numAccounts, len; i < numInstructions; ++i) {
        invokedIndexes[i] = decode(data, o);
        o += getByteLen(data, o);

        numAccounts = decode(data, o);
        o += getByteLen(data, o);
        o += numAccounts;

        len = decode(data, o);
        o += getByteLen(data, o);
        o += len;
      }
      if (o < data.length) {
        final int numLookupTables = decode(data, o);
        ++o;
        final int lookupTablesOffset = o;
        if (numLookupTables > 0) {
          final PublicKey[] lookupTableAccounts = new PublicKey[numLookupTables];
          int numAccounts = numIncludedAccounts;
          for (int t = 0, numWriteIndexes, numReadIndexes; t < numLookupTables; ++t) {
            lookupTableAccounts[t] = PublicKey.readPubKey(data, o);
            o += PUBLIC_KEY_LENGTH;

            numWriteIndexes = decode(data, o);
            o += getByteLen(data, o);
            o += numWriteIndexes;
            numAccounts += numWriteIndexes;

            numReadIndexes = decode(data, o);
            o += getByteLen(data, o);
            o += numReadIndexes;
            numAccounts += numReadIndexes;
          }
          Arrays.sort(invokedIndexes);
          return new TransactionSkeletonRecord(
              data,
              version,
              messageOffset,
              numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts,
              numIncludedAccounts, accountsOffset,
              recentBlockHashIndex,
              numInstructions, instructionsOffset, invokedIndexes,
              lookupTablesOffset, lookupTableAccounts,
              numAccounts
          );
        } else {
          return new TransactionSkeletonRecord(
              data,
              version,
              messageOffset,
              numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts,
              numIncludedAccounts, accountsOffset,
              recentBlockHashIndex,
              numInstructions, instructionsOffset, invokedIndexes,
              lookupTablesOffset, NO_TABLES,
              numIncludedAccounts
          );
        }
      } else {
        return new TransactionSkeletonRecord(
            data,
            version,
            messageOffset,
            numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts,
            numIncludedAccounts, accountsOffset,
            recentBlockHashIndex,
            numInstructions, instructionsOffset, invokedIndexes,
            data.length, NO_TABLES,
            numIncludedAccounts
        );
      }
    } else {
      for (int i = 0, numAccounts, len; i < numInstructions; ++i) {
        o += getByteLen(data, o); // program index

        numAccounts = decode(data, o);
        o += getByteLen(data, o);
        o += numAccounts;

        len = decode(data, o);
        o += getByteLen(data, o);
        o += len;
      }
      return new TransactionSkeletonRecord(
          data,
          version,
          messageOffset,
          numRequiredSignatures, numReadonlySignedAccounts, numReadonlyUnsignedAccounts,
          numIncludedAccounts, accountsOffset,
          recentBlockHashIndex,
          numInstructions, instructionsOffset, LEGACY_INVOKED_INDEXES,
          -1, NO_TABLES,
          numIncludedAccounts
      );
    }
  }

  byte[] data();

  int numSignatures();

  default int numSigners() {
    return numSignatures();
  }

  String id();

  int version();

  boolean isVersioned();

  boolean isLegacy();

  int numReadonlySignedAccounts();

  int numReadonlyUnsignedAccounts();

  int recentBlockHashIndex();

  byte[] blockHash();

  String base58BlockHash();

  int numIncludedAccounts();

  int numAccounts();

  default int numIndexedAccounts() {
    return numAccounts() - numIncludedAccounts();
  }

  PublicKey[] lookupTableAccounts();

  AccountMeta[] parseAccounts();

  AccountMeta[] parseAccounts(final Map<PublicKey, AddressLookupTable> lookupTables);

  default AccountMeta[] parseAccounts(final Stream<AddressLookupTable> lookupTables) {
    final var lookupTableMap = lookupTables.collect(Collectors
        .toUnmodifiableMap(AddressLookupTable::address, Function.identity()));
    return parseAccounts(lookupTableMap);
  }

  AccountMeta[] parseAccounts(final List<PublicKey> writableLoaded, final List<PublicKey> readonlyLoaded);

  PublicKey feePayer();

  AccountMeta[] parseSignerAccounts();

  PublicKey[] parseSignerPublicKeys();

  AccountMeta[] parseNonSignerAccounts();

  PublicKey[] parseNonSignerPublicKeys();

  AccountMeta[] parseAccounts(final AddressLookupTable lookupTable);

  PublicKey[] parseProgramAccounts();

  int serializedInstructionsLength();

  Instruction[] parseInstructions(final AccountMeta[] accounts);

  default Instruction[] parseLegacyInstructions() {
    return parseInstructions(parseAccounts());
  }

  /**
   * Program accounts will be included for each instruction.
   * Instruction accounts will not.
   */
  Instruction[] parseInstructionsWithoutAccounts();

  /**
   * If this is a versioned transaction accounts which are indexed into a lookup table will be null.
   * Signing accounts and program accounts will always be included.
   */
  Instruction[] parseInstructionsWithoutTableAccounts();

  Instruction[] filterInstructions(final AccountMeta[] accounts, final Discriminator discriminator);

  default Instruction[] filterInstructionsWithoutTableAccounts(final Discriminator discriminator) {
    return filterInstructions(parseAccounts(), discriminator);
  }

  Instruction[] filterInstructionsWithoutAccounts(final Discriminator discriminator);

  Transaction createTransaction(final List<Instruction> instructions);

  default Transaction createTransaction(final Instruction[] instructions) {
    return createTransaction(Arrays.asList(instructions));
  }

  default Transaction createTransaction(final AccountMeta[] accounts) {
    final var instructions = parseInstructions(accounts);
    return createTransaction(instructions);
  }

  default Transaction createTransaction() {
    final var accounts = parseAccounts();
    return createTransaction(accounts);
  }

  Transaction createTransaction(final List<Instruction> instructions, final AddressLookupTable lookupTable);

  default Transaction createTransaction(final Instruction[] instructions, final AddressLookupTable lookupTable) {
    return createTransaction(Arrays.asList(instructions), lookupTable);
  }

  default Transaction createTransaction(final AccountMeta[] accounts, final AddressLookupTable lookupTable) {
    final var instructions = parseInstructions(accounts);
    return createTransaction(instructions, lookupTable);
  }

  default Transaction createTransaction(final AddressLookupTable lookupTable) {
    final var accounts = parseAccounts(lookupTable);
    return createTransaction(accounts, lookupTable);
  }

  default Transaction createTransaction(final AccountMeta[] accounts,
                                        final LookupTableAccountMeta[] tableAccountMetas) {
    final var instructions = parseInstructions(accounts);
    return createTransaction(Arrays.asList(instructions), tableAccountMetas);
  }

  default Transaction createTransaction(final LookupTableAccountMeta[] tableAccountMetas) {
    final var accounts = parseAccounts(Arrays.stream(tableAccountMetas).map(LookupTableAccountMeta::lookupTable));
    return createTransaction(accounts, tableAccountMetas);
  }

  Transaction createTransaction(final List<Instruction> instructions, final LookupTableAccountMeta[] tableAccountMetas);
}
