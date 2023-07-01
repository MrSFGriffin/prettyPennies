using Core.Model;
using Microsoft.EntityFrameworkCore;

namespace Core.Database
{
    public class CoreDbContext : DbContext
    {
        public CoreDbContext(DbContextOptions<CoreDbContext> options)
        : base(options)
        {
        }

        public DbSet<ApiKey> ApiKeys { get; set; }
        public DbSet<Credential> Credentials { get; set; }
        public DbSet<JsObject> JsObjects { get; set; }
        public DbSet<Store> Stores { get; set; }
        public DbSet<User> Users { get; set; }

        protected override void OnModelCreating(ModelBuilder modelBuilder)
        {
            modelBuilder.Entity<ApiKey>().ToTable(nameof(ApiKey));
            modelBuilder.Entity<Credential>().ToTable(nameof(Credential));
            modelBuilder.Entity<JsObject>().ToTable(nameof(JsObject));
            modelBuilder.Entity<Store>().ToTable(nameof(Store));
            modelBuilder.Entity<User>().ToTable(nameof(User));
            base.OnModelCreating(modelBuilder);
        }
    }
}
