using Core.Model;

namespace Core.Security;

public interface IUserService
{
    Task<User> GetUserAsync(string userName);
    Task<IEnumerable<User>> GetUsersAsync();
    Task<bool> UpdateUserAsync(string userName, Roles role, string displayName);
    Task<bool> ChangePasswordAsync(string userName, string oldPassword, string newPassword);
    Task<bool> DeleteUserAsync(string userName);
    Task<User> CreateUserAsync(string userName, Roles rolel, string displayName, string password);
    Task<User> CreateInitialUserAsync(string userName, string displayName, string password);
    Task<User?> AuthenticateAsync(string userName, string password);
}
