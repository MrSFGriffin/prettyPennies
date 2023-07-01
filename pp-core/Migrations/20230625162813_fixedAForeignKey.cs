using Microsoft.EntityFrameworkCore.Migrations;

#nullable disable

namespace pp_core.Migrations
{
    /// <inheritdoc />
    public partial class fixedAForeignKey : Migration
    {
        /// <inheritdoc />
        protected override void Up(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "UserId",
                table: "JsObject");

            migrationBuilder.AddColumn<string>(
                name: "UserName",
                table: "JsObject",
                type: "TEXT",
                nullable: false,
                defaultValue: "");
        }

        /// <inheritdoc />
        protected override void Down(MigrationBuilder migrationBuilder)
        {
            migrationBuilder.DropColumn(
                name: "UserName",
                table: "JsObject");

            migrationBuilder.AddColumn<int>(
                name: "UserId",
                table: "JsObject",
                type: "INTEGER",
                nullable: false,
                defaultValue: 0);
        }
    }
}
