using System.ComponentModel.DataAnnotations;

namespace Core.Model
{
    public class Store
    {
        [Key]
        public int Id { get; set; }
        public string Name { get; set; }
    }
}
