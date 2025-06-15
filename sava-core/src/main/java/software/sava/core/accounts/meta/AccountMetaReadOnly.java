package software.sava.core.accounts.meta;

import software.sava.core.accounts.PublicKey;

sealed class AccountMetaReadOnly implements AccountMeta permits
    AccountMetaFeePayer,
    AccountMetaInvoked,
    AccountMetaInvokedAndWrite,
    AccountMetaReadOnlySigner,
    AccountMetaSignerWriter,
    AccountMetaWrite {

  protected final PublicKey publicKey;

  AccountMetaReadOnly(final PublicKey publicKey) {
    this.publicKey = publicKey;
  }

  @Override
  public final PublicKey publicKey() {
    return publicKey;
  }

  @Override
  public boolean feePayer() {
    return false;
  }

  @Override
  public boolean signer() {
    return false;
  }

  @Override
  public boolean write() {
    return false;
  }

  @Override
  public boolean invoked() {
    return false;
  }

  @Override
  public AccountMeta merge(final AccountMeta accountMeta) {
    return accountMeta;
  }

  @Override
  public boolean equals(final Object o) {
    return this == o || (o.getClass() == AccountMetaReadOnly.class && publicKey.equals(((AccountMetaReadOnly) o).publicKey));
  }

  @Override
  public int hashCode() {
    return publicKey.hashCode();
  }

  @Override
  public final String toString() {
    return """
        {
          "publicKey": "%s",
          "feePayer": %b,
          "signer": %b,
          "writable": %b,
          "invoked": %b
        }""".formatted(
        publicKey.toBase58(),
        feePayer(),
        signer(),
        write(),
        invoked()
    );
  }
}
