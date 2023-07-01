using System.ComponentModel.DataAnnotations;

namespace Core.Model
{
    public class JsObject
    {
        [Key]
        public int Id { get; set; }
        public string UserName { get; set; }
        public int StoreId { get; set; }
        public string? Json { get; set; }
    }
}
