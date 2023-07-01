using Core.Model;
using Core.Security;
using Microsoft.AspNetCore.Authentication;
using Microsoft.Extensions.Options;
using Microsoft.OpenApi.Extensions;
using System.Security.Claims;
using System.Text.Encodings.Web;

namespace API.Security;

public class ApiKeyAuthenticationHandler : AuthenticationHandler<ApiKeyAuthenticationOptions>
{

    public ApiKeyAuthenticationHandler(
        IOptionsMonitor<ApiKeyAuthenticationOptions> options,
        ILoggerFactory logger,
        UrlEncoder encoder,
        ISystemClock clock,
        IKeyService keyService)
        : base(options, logger, encoder, clock)
    {
        KeyService = keyService;
    }

    public IKeyService KeyService { get; }

    protected override async Task<AuthenticateResult> HandleAuthenticateAsync()
    {
        if (!Request.Headers.TryGetValue(ApiKeyAuthenticationOptions.HeaderName, out var apiKey) || apiKey.Count != 1)
        {
            Logger.LogWarning("No x-api-key header received.");
            return AuthenticateResult.Fail("Invalid parameters");
        }

        var role = await KeyService.GetRoleAsync(apiKey.First() ?? "");
        if (role == Roles.None)
        {
            Logger.LogWarning("Received API key does not match any valid API key.");
            return AuthenticateResult.Fail("Invalid parameters");
        }

        Logger.LogInformation("Api key is valid.");
        var claims = new[] { 
            new Claim(ClaimTypes.Role, role.GetDisplayName()),
            new Claim(ClaimTypes.Sid, apiKey.First() ?? "")
        };
        var identity = new ClaimsIdentity(claims, ApiKeyAuthenticationOptions.DefaultScheme);
        var identities = new List<ClaimsIdentity> { identity };
        var principal = new ClaimsPrincipal(identities);
        var ticket = new AuthenticationTicket(principal, ApiKeyAuthenticationOptions.DefaultScheme);
        return AuthenticateResult.Success(ticket);
    }
}
