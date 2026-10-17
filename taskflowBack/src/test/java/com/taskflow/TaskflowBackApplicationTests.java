package com.taskflow;


import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.request.RequestPostProcessor;

import java.util.Arrays;
import java.util.stream.Collectors;

import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest                       // ①
@AutoConfigureMockMvc                 // ②
@ActiveProfiles("test")
class TaskflowBackApplicationTests {
	@Autowired                        // ④
	MockMvc mvc;

	// ⑤ Fabrique un faux jeton Keycloak
	private static RequestPostProcessor token(String sub, String username, String... roles) {
		return jwt()
				.jwt(j -> j.subject(sub)
						.claim("preferred_username", username)
						.claim("email", username + "@example.com"))
				.authorities(Arrays.stream(roles).map(r -> new SimpleGrantedAuthority("ROLE_" + r)).collect(Collectors.toList()));
	}

	private static RequestPostProcessor alice() { return token("alice-id", "alice", "user"); }
	private static RequestPostProcessor bob()   { return token("bob-id", "bob", "user", "admin"); }

	// ⑥ ---------- 401 : non authentifié ----------

	@Test
	void sansJeton_401() throws Exception {
		mvc.perform(get("/api/me"))
				.andExpect(status().isUnauthorized())
				.andExpect(jsonPath("$.status").value(401));
	}

	@Test
	void jetonIllisible_401() throws Exception {
		mvc.perform(get("/api/me").header("Authorization", "Bearer ceci-n-est-pas-un-jwt"))
				.andExpect(status().isUnauthorized());
	}

	// ⑦ ---------- 403 : droits insuffisants ----------

	@Test
	void userSurRouteAdmin_403() throws Exception {
		mvc.perform(get("/api/admin/tasks").with(alice()))
				.andExpect(status().isForbidden())
				.andExpect(jsonPath("$.status").value(403));
	}

	@Test
	void jetonSansRole_403() throws Exception {
		mvc.perform(get("/api/tasks").with(token("x-id", "x")))
				.andExpect(status().isForbidden());
	}

	// ⑧ ---------- 200 : cas qui marchent ----------

	@Test
	void me_200() throws Exception {
		mvc.perform(get("/api/me").with(bob()))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.roles", containsInAnyOrder("user", "admin")));
	}

	@Test
	void adminListe_200() throws Exception {
		mvc.perform(get("/api/admin/tasks").with(bob()))
				.andExpect(status().isOk());
	}

	@Test
	 void health_200_sansJeton() throws Exception {
		mvc.perform(get("/actuator/health"))
				.andExpect(status().isOk());
	}
}
