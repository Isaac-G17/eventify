package com.eventify.eventify;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;

// BD en memoria limpia: Flyway aplica V1, V2 y V3 desde cero en cada ejecución
@SpringBootTest(properties = "spring.datasource.url=jdbc:h2:mem:eventify-test;DB_CLOSE_DELAY=-1")
class EventifyApplicationTests {

	@Test
	void contextLoads() {
	}

}
