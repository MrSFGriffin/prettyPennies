using Core.Database;
using Core.Model;
using Microsoft.AspNetCore.Authorization;
using Microsoft.AspNetCore.Mvc;
using Microsoft.EntityFrameworkCore;
using System.Security.Claims;

namespace API.Controllers;

[Authorize(Roles = "Admin")]
[ApiController]
[Route("api/[controller]")]
public class StoreController : ControllerBase
{
    private readonly CoreDbContext db; 

    public StoreController(CoreDbContext db)
    {
        this.db = db;   
    }

    [HttpGet]
    public async Task<IEnumerable<Store>> GetStoreAsync()
        => await db.Stores.ToListAsync();

    [HttpGet("{storeId}")]
    public async Task<IEnumerable<JsObject>> GetObjectsAsync(int storeId)
        => await db
            .JsObjects
            .Where(jo => jo.StoreId == storeId)
            .ToListAsync();

    [HttpGet("{storeId}/{jsObjectId}")]
    public async Task<IEnumerable<JsObject>> GetObjectsAsync(int storeId, int jsObjectId)
        => await db
            .JsObjects
            .Where(jo => jo.StoreId == storeId && jo.Id == jsObjectId)
            .ToListAsync();

    [HttpPut]
    public async Task<Store> CreateStoreAsync(string name)
    {
        var store = new Store
        {
            Name = name
        };
        await db.Stores.AddAsync(store);
        await db.SaveChangesAsync();
        return store;
    }

    [HttpPost("{storeId}")]
    public async Task<ActionResult<JsObject>> StoreJsonAsync(int storeId, string json)
    {
        var user = await GetUserFromIdentityAsync();
        if (user == null)
        {
            return StatusCode(StatusCodes.Status401Unauthorized, "No valid user.");
        }

        var jsObject = new JsObject
        {
            StoreId = storeId,
            UserName = user.UserName,
            Json = json
        };
        await db.JsObjects.AddAsync(jsObject);
        await db.SaveChangesAsync();
        return jsObject;
    }

    [HttpDelete("{storeId}/{JsObjectId}")]
    public async Task<ActionResult> DeleteJsonAsync(int storeId, int jsObjectId)
    {
        var user = await GetUserFromIdentityAsync();
        if (user == null)
        {
            return StatusCode(StatusCodes.Status401Unauthorized, "No valid user.");
        }

        var jsObject = db.JsObjects.FirstOrDefault(jo =>
            jo.StoreId == storeId && jo.Id == jsObjectId && jo.UserName == user.UserName);
        if (jsObject == null)
        {
            return StatusCode(StatusCodes.Status204NoContent);
        }

        db.JsObjects.Remove(jsObject);
        await db.SaveChangesAsync();
        return Ok();
    }

    private async Task<User?> GetUserFromIdentityAsync()
    {
        var apiKey = User.Claims.FirstOrDefault(c => c.Type == ClaimTypes.Sid)?.Value;
        return await db.Users.FirstOrDefaultAsync(u => u.ApiKey == apiKey);
    }
}
