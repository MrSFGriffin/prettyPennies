using System.ComponentModel.DataAnnotations;
using System.ComponentModel.DataAnnotations.Schema;

namespace Core.Model;

public class User
{
    [Key]
    public string UserName { get; set; } = "";
    public string DisplayName { get; set; } = "";
    public int Role { get; set; }
    [NotMapped]
    public string RoleName => Enum.GetName(typeof(Roles), this.Role) ?? "Unknown";
    public string ApiKey { get; set; }
    public DateTime CreateAtUtc { get; set; }
}
