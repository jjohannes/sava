package software.sava.core.accounts.token.extensions;

import software.sava.core.accounts.PublicKey;

import static software.sava.core.accounts.PublicKey.PUBLIC_KEY_LENGTH;
import static software.sava.core.accounts.PublicKey.readPubKey;

public record GroupMemberPointer(PublicKey authority, PublicKey memberAddress) implements MintTokenExtension {

  public static final int BYTES = PUBLIC_KEY_LENGTH + PUBLIC_KEY_LENGTH;

  public static GroupMemberPointer read(final byte[] data, final int offset) {
    if (data == null || data.length == 0) {
      return null;
    }
    final var authority = readPubKey(data, offset);
    final var memberAddress = readPubKey(data, offset + PUBLIC_KEY_LENGTH);
    return new GroupMemberPointer(authority, memberAddress);
  }

  @Override
  public ExtensionType extensionType() {
    return ExtensionType.GroupMemberPointer;
  }

  @Override
  public int l() {
    return BYTES;
  }

  @Override
  public int write(final byte[] data, final int offset) {
    authority.write(data, offset);
    memberAddress.write(data, offset + PUBLIC_KEY_LENGTH);
    return BYTES;
  }
}
