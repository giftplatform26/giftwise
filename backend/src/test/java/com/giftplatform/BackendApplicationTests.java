
package com.giftplatform;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class BackendApplicationTests {

	@Container
	static final PostgreSQLContainer<?> postgres =
			new PostgreSQLContainer<>("postgres:17")
					.withDatabaseName("giftplatform_test")
					.withUsername("testuser")
					.withPassword("testpassword");

	@DynamicPropertySource
	static void configureDatabase(DynamicPropertyRegistry registry) {
		registry.add("spring.datasource.url", postgres::getJdbcUrl);
		registry.add("spring.datasource.username", postgres::getUsername);
		registry.add("spring.datasource.password", postgres::getPassword);
	}

	@Autowired
	private JdbcTemplate jdbcTemplate;

	@Test
	void contextLoads() {
		Integer result = jdbcTemplate.queryForObject(
				"SELECT 1", Integer.class
		);

		assertThat(result).isEqualTo(1);
	}
}
