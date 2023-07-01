using Core.Model;

namespace Core.Security;

public interface IKeyService
{
    Task<(bool success, string key)> GenerateFirstApiKeyAsync();
    Task<(int apiKeyId, string key)> GenerateApiKeyAsync(Roles role);
    Task<Roles> GetRoleAsync(string key);
    Task RevokeKeyAsync(string key);
    string Hash(string value);
}
