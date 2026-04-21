package com.rra.taxhandbook.migration;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Arrays;
import java.util.stream.Collectors;

import org.flywaydb.core.Flyway;
import org.h2.jdbcx.JdbcDataSource;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class MigrationAdoptionIntegrationTests {

	private static final String JDBC_URL = "jdbc:h2:mem:migrationtestdb;DB_CLOSE_DELAY=-1;MODE=PostgreSQL";

	private Flyway flyway;

	@BeforeEach
	void setUp() throws SQLException {
		try (var connection = DriverManager.getConnection(JDBC_URL, "sa", "");
			 var statement = connection.createStatement()) {
			statement.execute("drop all objects");
			statement.execute(
				"""
				create alias if not exists SPLIT_PART as $$
				String splitPart(String value, String delimiter, int index) {
					if (value == null || delimiter == null || index < 1) {
						return null;
					}
					String[] parts = value.split(java.util.regex.Pattern.quote(delimiter), -1);
					return index <= parts.length ? parts[index - 1] : "";
				}
				$$;
				"""
			);
		}
		JdbcDataSource dataSource = new JdbcDataSource();
		dataSource.setURL(JDBC_URL);
		dataSource.setUser("sa");
		dataSource.setPassword("");
		flyway = Flyway.configure()
			.dataSource(dataSource)
			.locations("classpath:db/migration")
			.load();
		flyway.migrate();
	}

	@Test
	void flywayAppliesFullMigrationChainWithoutPendingSteps() {
		var appliedVersions = Arrays.stream(flyway.info().applied())
			.map(info -> info.getVersion() == null ? null : info.getVersion().getVersion())
			.filter(version -> version != null)
			.collect(Collectors.toList());

		assertEquals(java.util.List.of("1", "2", "3", "4", "5", "6", "7", "8", "9", "10"), appliedVersions);
		assertEquals(0, flyway.info().pending().length);
	}

	@Test
	void migratedSchemaMatchesCurrentUserAndContentExpectations() {
		assertTrue(columnExists("USERS", "FIRST_NAME"));
		assertTrue(columnExists("USERS", "LAST_NAME"));
		assertTrue(columnExists("USERS", "USERNAME"));
		assertTrue(columnExists("USERS", "IS_ACTIVE"));
		assertTrue(columnExists("USERS", "IS_LOCKED"));
		assertTrue(columnExists("USERS", "FAILED_LOGIN_ATTEMPTS"));
		assertTrue(columnExists("USERS", "LAST_LOGIN_AT"));
		assertTrue(columnExists("USERS", "PHONE_NUMBER"));
		assertTrue(columnExists("USERS", "DEPARTMENT"));
		assertTrue(columnExists("USERS", "POSITION"));
		assertTrue(columnExists("USERS", "UPDATED_AT"));
		assertTrue(columnExists("USERS", "DELETED_AT"));
		assertTrue(columnExists("USERS", "CREATED_BY"));
		assertTrue(columnExists("USERS", "UPDATED_BY"));
		assertFalse(columnExists("USERS", "FULL_NAME"));

		assertTrue(columnExists("CONTENT_TOPICS", "SCHEDULED_PUBLISH_AT"));
		assertFalse(tableExists("EMPLOYEE_DIRECTORY_SNAPSHOT"));
		assertTrue(tableExists("EMAIL_NOTIFICATIONS"));
		assertTrue(columnExists("EMAIL_NOTIFICATIONS", "STATUS"));
		assertTrue(columnExists("EMAIL_NOTIFICATIONS", "NEXT_ATTEMPT_AT"));
		assertTrue(columnExists("EMAIL_NOTIFICATIONS", "RECIPIENT_USERNAME"));
		assertTrue(tableExists("HOMEPAGE_CONTENTS"));
		assertTrue(tableExists("CONTENT_TOPIC_WORKFLOW_HISTORY"));
	}

	@Test
	void migrationSeedsCmsBackedContactContent() {
		assertEquals(1, countRows(
			"""
			select count(*)
			from content_section_translations
			where slug = ?
			  and locale = ?
			""",
			"contact",
			"EN"
		));
		assertEquals(1, countRows(
			"""
			select count(*)
			from content_topic_translations
			where slug = ?
			  and locale = ?
			""",
			"rra-contact-details",
			"EN"
		));
		assertEquals(1, countRows(
			"""
			select count(*)
			from content_topic_translations
			where slug = ?
			  and locale = ?
			""",
			"tax-centres",
			"EN"
		));
	}

	private boolean tableExists(String tableName) {
		return countRows(
			"select count(*) from information_schema.tables where upper(table_name) = ?",
			tableName
		) > 0;
	}

	private boolean columnExists(String tableName, String columnName) {
		return countRows(
			"""
			select count(*)
			from information_schema.columns
			where upper(table_name) = ?
			  and upper(column_name) = ?
			""",
			tableName,
			columnName
		) > 0;
	}

	private int countRows(String sql, String... params) {
		try (var connection = DriverManager.getConnection(JDBC_URL, "sa", "");
			 var statement = connection.prepareStatement(sql)) {
			for (int index = 0; index < params.length; index++) {
				statement.setString(index + 1, params[index]);
			}
			try (ResultSet resultSet = statement.executeQuery()) {
				resultSet.next();
				return resultSet.getInt(1);
			}
		}
		catch (SQLException ex) {
			throw new IllegalStateException("Failed to query migrated schema", ex);
		}
	}
}
