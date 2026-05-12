package io.nicheblog.dreamdiary;

import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.Test;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Smoke test to ensure the application context starts.
 */
class DreamdiaryApplicationStartupTest {

    @Test
    void contextLoads() {
        Assumptions.assumeTrue(isDatabaseAvailable(),
                "Skipping startup smoke test because MariaDB test schema is unavailable.");
        try (ConfigurableApplicationContext applicationContext = new SpringApplicationBuilder(DreamdiaryApplication.class)
                .profiles("test")
                .run()) {
            assertNotNull(applicationContext, "Application context was null.");
            assertTrue(applicationContext.isActive(), "Application context failed to start.");
        }
    }

    private boolean isDatabaseAvailable() {
        final String url = "jdbc:mariadb://localhost:3306/dreamdiary";
        final String username = "root";
        final String password = "admin12!@";

        try (Connection ignored = DriverManager.getConnection(url, username, password)) {
            return true;
        } catch (SQLException e) {
            return false;
        }
    }
}
