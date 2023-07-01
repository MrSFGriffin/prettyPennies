namespace Core.Security;

public interface ICryptoService
{
    string Encrypt(string value);
    bool Verify(string hashed, string value);
}