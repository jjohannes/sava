package software.sava.core.accounts.token.extensions;

public record Uninitialized() implements MintTokenExtension {

  public static final Uninitialized INSTANCE = new Uninitialized();

  @Override
  public ExtensionType extensionType() {
    return ExtensionType.Uninitialized;
  }

  @Override
  public int l() {
    return 0;
  }

  @Override
  public int write(final byte[] data, final int offset) {
    return 0;
  }
}
