using System.ComponentModel.DataAnnotations;

namespace Core.Model
{
    public class ApiKey
    {
        [Key]
        public int Id { get; set; }
        public string Key { get; set; }
        public DateTime CreatedAt { get; set; }
        public int Role { get; set; } = (int) Roles.Normal;
    }

    public enum Roles
    {
        Normal,
        Admin,
        None
    }
}
