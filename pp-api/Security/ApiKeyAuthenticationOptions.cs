using Microsoft.AspNetCore.Authentication;

namespace API.Security;

public class ApiKeyAuthenticationOptions : AuthenticationSchemeOptions
{
    public const string DefaultScheme = "ApiKey";
    public const string HeaderName = "x-api-key";
}
