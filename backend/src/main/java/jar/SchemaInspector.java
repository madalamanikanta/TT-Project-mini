package jar;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;

@Component
public class SchemaInspector implements CommandLineRunner {

    @Autowired
    private DataSource dataSource;

    @Override
    public void run(String... args) throws Exception {
        try (Connection conn = dataSource.getConnection()) {
            try (java.sql.Statement stmt = conn.createStatement()) {
                stmt.execute("ALTER TABLE internships DROP COLUMN IF EXISTS organization");
                System.out.println("Successfully dropped organization column from internships table.");
            } catch (Exception e) {
                System.out.println("Error dropping column: " + e.getMessage());
            }

            DatabaseMetaData metaData = conn.getMetaData();
            
            System.out.println("=== INTERNSHIPS COLUMNS ===");
            ResultSet rs1 = metaData.getColumns(null, null, "internships", null);
            while (rs1.next()) {
                System.out.println(rs1.getString("COLUMN_NAME") + " - " + rs1.getString("TYPE_NAME"));
            }
            
            System.out.println("=== SKILLS COLUMNS ===");
            ResultSet rs2 = metaData.getColumns(null, null, "skills", null);
            while (rs2.next()) {
                System.out.println(rs2.getString("COLUMN_NAME") + " - " + rs2.getString("TYPE_NAME"));
            }

            System.out.println("=== INTERNSHIP_SKILLS COLUMNS ===");
            ResultSet rs3 = metaData.getColumns(null, null, "internship_skills", null);
            while (rs3.next()) {
                System.out.println(rs3.getString("COLUMN_NAME") + " - " + rs3.getString("TYPE_NAME"));
            }
        }
    }
}
