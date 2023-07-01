using Core.Database;
using Core.Model;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Caching.Memory;
using System.Net;
using System.Security.Cryptography;
using System.Text;

namespace Core.Security;

public class KeyService : IKeyService
{
    private readonly IMemoryCache memCache;
    private readonly CoreDbContext db;

    public KeyService(IMemoryCache memCache, CoreDbContext db)
    {
        this.memCache = memCache;
        this.db = db;
    }

    public async Task<(bool success, string key)> GenerateFirstApiKeyAsync()
    {
        var count = await db.ApiKeys.CountAsync();
        if (count > 0)
        {
            return (false, "");
        }

        return (true, (await GenerateApiKeyAsync(Roles.Admin)).key);
    }

    public async Task<(int apiKeyId, string key)> GenerateApiKeyAsync(Roles role)
    {
        var rng = RandomNumberGenerator.Create();
        var bytes = new byte[32];
        rng.GetBytes(bytes);
        const int length = 32;
        var key = $"PP-{WebUtility.UrlEncode(Convert.ToBase64String(bytes))}".Substring(0, length);
        var hashed = SHA256.HashData(Encoding.UTF8.GetBytes(key));
        var apiKey = new ApiKey
        {
            Key = Encoding.UTF8.GetString(hashed),
            Role = (int)role
        };
        await db.AddAsync(apiKey);
        await db.SaveChangesAsync();
        return (apiKey.Id, key);
    }

    public string Hash(string value)
        => Encoding.UTF8.GetString(SHA256.HashData(Encoding.UTF8.GetBytes(value)));

    public async Task<Roles> GetRoleAsync(string key)
    {
        if (!memCache.TryGetValue(key, out Roles role))
        {
            role = Roles.None;
            var hashed = Hash(key);
            var apiKey = await db.ApiKeys.FirstOrDefaultAsync(k => k.Key == hashed);
            if (apiKey != null)
            {
                role = (Roles)apiKey.Role;
                memCache.Set(hashed, role);
            }
        }

        return role;
    }

    public async Task RevokeKeyAsync(string key)
    {
        var hashed = Hash(key);
        var apiKey = await db.ApiKeys.FirstOrDefaultAsync(k => k.Key == hashed);
        if (apiKey == null)
        {
            return;
        }

        db.ApiKeys.Remove(apiKey);
        await db.SaveChangesAsync();
        memCache.Remove(hashed);
    }
}
