using API.Model;
using Core.Model;
using Core.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[Authorize(Roles = "Admin")]
[ApiController]
[Route("api/[controller]")]
public class UserController : ControllerBase
{
    private readonly IUserService userService;

    public UserController(IUserService userService)
    {
        this.userService = userService;
    }

    [AllowAnonymous]
    [HttpPut()]
    public async Task<ActionResult<User>> CreateInitialUserAsync(
        string userName, string displayName, string password)
            => await userService.CreateInitialUserAsync(userName, displayName, password);

    [HttpPost()]
    public async Task<ActionResult<User>> CreateUserAsync([FromBody] CreateUserModel model)
            => await userService.CreateUserAsync(
                model.UserName, model.Role, model.DisplayName, model.Password);

    [HttpPost("{userName}")]
    public async Task<ActionResult<bool>> UpdateUser(
        string userName, Roles role, string displayName)
            => await userService.UpdateUserAsync(userName, role, displayName);

    [HttpPost("{userName}/password")]
    public async Task<ActionResult<bool>> UpdatePassword(
        string userName, string oldPassword, string newPassword)
            => await userService.ChangePasswordAsync(userName, oldPassword, newPassword);

    [HttpDelete("{userName}")]
    public async Task<bool> DeleteUserAsync(string userName)
        => await userService.DeleteUserAsync(userName);

    [HttpGet()]
    public async Task<IEnumerable<User>> GetUsers()
        => await userService.GetUsersAsync();

    [HttpGet("{userName}")]
    public async Task<ActionResult<User>> GetUser(string userName)
    {
        var user = await userService.GetUserAsync(userName);
        return userName != "" && !string.IsNullOrWhiteSpace(user.UserName)
            ? user
            : StatusCode(StatusCodes.Status404NotFound);
    }
}
