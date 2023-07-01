using Core.Database;
using Core.Model;
using Microsoft.EntityFrameworkCore;
using Microsoft.Extensions.Logging;

namespace Core.Security;

public class UserService : IUserService
{
    private readonly IKeyService keyService;
    private readonly ICryptoService cryptoService;
    private readonly CoreDbContext db;
    private readonly ILogger<UserService> logger;

    public UserService(
        IKeyService keyService, 
        ICryptoService cryptoService, 
        CoreDbContext db,
        ILogger<UserService> logger)
    {
        this.keyService = keyService;
        this.cryptoService = cryptoService;
        this.db = db;
        this.logger = logger;
    }

    public async Task<User> GetUserAsync(string userName)
        => await db.Users.FindAsync(userName) ?? new User();

    public async Task<IEnumerable<User>> GetUsersAsync()
        => await db.Users.ToListAsync();

    public async Task<bool> DeleteUserAsync(string userName)
    {
        var user = await db.Users.FindAsync(userName);
        if (user == null)
        {
            return false;
        }

        db.Users.Remove(user);

        var credential = await db.Credentials.FindAsync(userName);
        if (credential == null)
        {
            return false;
        }

        db.Credentials.Remove(credential);
        
        return await db.SaveChangesAsync() > 0;
    }

    public async Task<User> CreateUserAsync(
        string userName, Roles role, string displayName, string password)
    {
        var (_, key) = await keyService.GenerateApiKeyAsync(role);
        var user = new User
        {
            UserName = userName,
            DisplayName = displayName,
            Role = (int) role,
            ApiKey = key,
            CreateAtUtc = DateTime.UtcNow,
        };
        var credential = new Credential
        {
            UserName = userName,
            Password = cryptoService.Encrypt(password)
        };

        await db.Users.AddAsync(user);
        await db.Credentials.AddAsync(credential);
        await db.SaveChangesAsync();
        return user;
    }

    public async Task<User> CreateInitialUserAsync(
        string userName, string displayName, string password)
    {
        if (await db.Users.AnyAsync())
        {
            throw new InvalidOperationException("Cannot create initial user, as one already exists.");
        }

        return await CreateUserAsync(userName, Roles.Admin, displayName, password);
    }

    public async Task<User?> AuthenticateAsync(string userName, string password)
    {
        logger.LogInformation($"Authentication: finding {userName}.");
        var user = await db.Users.FindAsync(userName);
        if (user == null) { return new User(); }


        logger.LogInformation($"Authentication: finding {userName} creditials.");
        var credential = await db.Credentials.FindAsync(user.UserName);
        if (credential == null) { return new User(); }

        logger.LogInformation($"Authentication: verifying {userName} password.");
        var verified = cryptoService.Verify(credential.Password, password);
        logger.LogInformation($"Authentication: {userName} verification {verified}.");
        return verified 
            ? user
            : new User();
    }

    public async Task<bool> UpdateUserAsync(string userName, Roles role, string displayName)
    {
        var user = await db.Users.FindAsync(userName);
        if (user == null)
        {
            return false;
        }

        user.Role = (int) role;
        user.DisplayName = displayName;
        return await db.SaveChangesAsync() > 0;
    }

    public async Task<bool> ChangePasswordAsync(string userName, string oldPassword, string newPassword)
    {
        if (string.IsNullOrWhiteSpace(newPassword) || newPassword.Count() < 8)
        {
            return false;
        }

        var credential = await db.Credentials.FindAsync(userName);
        if (credential == null ) { return false; }

        if (cryptoService.Verify(credential.Password, oldPassword))
        {
            credential.Password = cryptoService.Encrypt(newPassword);
            return await db.SaveChangesAsync() > 1;
        }

        return false;
    }
}
