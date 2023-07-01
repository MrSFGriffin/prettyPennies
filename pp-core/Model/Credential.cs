using System.ComponentModel.DataAnnotations;

namespace Core.Model
{
    public class Credential
    {
        [Key]
        public string UserName { get; set; } = "";
        public string Password { get; set; } = "";
    }
}
