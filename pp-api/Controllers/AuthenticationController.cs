using API.Model;
using Core.Model;
using Core.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers;

[Authorize(Roles = "Admin,Normal")]
[ApiController]
[Route("api/[controller]")]
public class AuthenticationController : ControllerBase 
{
    private readonly IUserService userService;

    public AuthenticationController(IUserService userService)
    {
        this.userService = userService;
    }

    [AllowAnonymous]
    [HttpPost()]
    public async Task<ActionResult<User?>> Post([FromBody] LoginModel loginModel)
        => await userService.AuthenticateAsync(loginModel.UserName, loginModel.Password);
}
