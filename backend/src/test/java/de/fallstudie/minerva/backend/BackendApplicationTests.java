package de.fallstudie.minerva.backend;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest(classes = ActiveProfiles.class)
@ActiveProfiles({"test"})
class BackendApplicationTests {

	@Test
	void contextLoads() {
	}

	@Test
	void verifyModules() {
		ApplicationModules.of(BackendApplication.class).verify();
	}
}
