package dev.danielcorrea.backbdb;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIfEnvironmentVariable;
import org.springframework.boot.test.context.SpringBootTest;

/**
 * Integration test that loads the full Spring application context.
 * Requires database and mail server configuration.
 * Only runs in CI/CD environment where DB_URL environment variable is explicitly set.
 * 
 * To run locally, set the DB_URL environment variable:
 * export DB_URL=jdbc:mysql://localhost:3306/approval_flow_test
 */
@SpringBootTest
@EnabledIfEnvironmentVariable(named = "DB_URL", matches = "jdbc:mysql://.*", disabledReason = "Integration test - requires MySQL database. Set DB_URL environment variable to run.")
class BackBdbApplicationTests {

	@Test
	void contextLoads() {
		// This test verifies that the Spring context loads successfully
		// Only runs when DB_URL environment variable is set to a MySQL JDBC URL
	}

}
