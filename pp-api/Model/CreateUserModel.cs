using Core.Model;

namespace API.Model;

public class CreateUserModel
{
    public string UserName { get; set; }
    public string DisplayName { get; set; }
    public Roles Role { get; set; }
    public string Password { get; set; }
}