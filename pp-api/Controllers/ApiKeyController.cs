using Core.Model;
using Core.Security;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;

namespace API.Controllers
{
    [Authorize(Roles = "Admin")]
    [ApiController]
    [Route("api/[controller]")]
    public class ApiKeyController : ControllerBase
    {
        private readonly IKeyService keyService;

        public ApiKeyController(IKeyService keyService)
        {
            this.keyService = keyService;
        }

        [AllowAnonymous]
        [HttpPut(Name = "GenerateFirstApiKey")]
        public async Task<ActionResult<string>> Put()
        {
            var (success, key) = await keyService.GenerateFirstApiKeyAsync();
            return success
                ? key
                : StatusCode(
                    StatusCodes.Status400BadRequest,
                    "Could not create a first api key. " +
                    "This is probably because there is already an API key in the system.");
        }

        [HttpPost(Name = "GenerateKey")]
        public async Task<string> PostAsync(Roles role) 
            => (await keyService.GenerateApiKeyAsync(role)).key;

        [HttpDelete(Name = "RevokeKey")]
        public async Task DeleteAsync(string key)
        {
            await keyService.RevokeKeyAsync(key);
        }
    }
}