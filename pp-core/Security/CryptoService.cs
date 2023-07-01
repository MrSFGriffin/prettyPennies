using Isopoh.Cryptography.Argon2;
using Microsoft.Extensions.Configuration;
using System.Security.Cryptography;
using System.Text;

namespace Core.Security;

public class CryptoService : ICryptoService
{
    private string? secret;

    public CryptoService(IConfiguration config)
    {
        secret = config["CryptoSecret"];
    }

    public string Encrypt(string value)
    {
        if (secret == null)
        {
            throw new ApplicationException(
                "Either no or the wrong crypto secret in the configuration. " +
                "Set the correct one as an environment variable called: 'CryptoSecret'.");
        }

        var salt = new byte[16];
        var rng = RandomNumberGenerator.Create();
        rng.GetBytes(salt);
        Argon2Config config = new Argon2Config
        {
            Password = Encoding.UTF8.GetBytes(value),
            Salt = salt,
            Secret = Encoding.UTF8.GetBytes(secret),
        };
        return Argon2.Hash(config);
    }

    public bool Verify(string hashed, string value)
    {
        if (secret == null)
        {
            throw new ApplicationException(
                "No crypto secret in the configuration. " +
                "Add one as an environment variable called: 'CryptoSecret'.");
        }

        var config = new Argon2Config
        {
            Password = Encoding.UTF8.GetBytes(value),
            Secret = Encoding.UTF8.GetBytes(secret)
        };
        return Argon2.Verify(hashed, config);
    }
}
