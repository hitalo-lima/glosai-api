package com.hitalo.glosai;

import org.junit.jupiter.api.Test;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;

@SpringBootTest
@TestPropertySource(properties = {"groq.api.key=dummy-key-for-tests"})
class GlosaiApplicationTests {

	@Test
	void contextLoads() {
	}

}
