package com.example.fundraising;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Profile;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Map;
import java.util.stream.Collectors;

import static org.springframework.security.config.Customizer.withDefaults;


@SpringBootApplication
public class FundraisingApplication {

	@Value("${cors.allowedOrigins}")
	private String allowedOrigins;

	public static void main(String[] args) {
		SpringApplication.run(FundraisingApplication.class, args);
	}
	@Bean
	public CorsConfigurationSource corsConfigurationSource() {
		CorsConfiguration configuration = new CorsConfiguration();
		configuration.setAllowedOrigins(Arrays.asList(allowedOrigins));
		configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE"));
		configuration.setAllowedHeaders(Arrays.asList("*"));
		configuration.setExposedHeaders(Arrays.asList("Location"));
		configuration.setAllowCredentials(true);

		UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
		source.registerCorsConfiguration("/**", configuration);
		return source;
	}

	@Bean
	@Profile("test")
	public SecurityFilterChain securityFilterTestChain(HttpSecurity http) throws Exception {
		http
				.csrf().disable()
				.authorizeHttpRequests(authorize -> authorize
						.anyRequest().permitAll()
				);
		return http.build();
	}
	@Bean
	@Profile("!test")
	public SecurityFilterChain securityFilterChain(HttpSecurity http, CorsConfigurationSource corsConfigurationSource) throws Exception {
		return http
				.authorizeHttpRequests((authorize) -> authorize
						// webhook-controller -> permit all zodat stripe er aan kan
						.requestMatchers("/webhook").permitAll()
						// open api access
						.requestMatchers(HttpMethod.GET, "/swagger-ui/**").permitAll()
						.requestMatchers(HttpMethod.GET, "/api/**").permitAll()
						// event-controller
						.requestMatchers(HttpMethod.GET, "/events/history/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/events/**").permitAll()
						.requestMatchers(HttpMethod.GET,"/*/files/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/events/**").authenticated()
						// enkel authenticated, zodanig dat een event verwijderd kan worden als het niet gelukt is om de
						.requestMatchers(HttpMethod.DELETE, "/events/**").authenticated()

						.requestMatchers(HttpMethod.PUT, "/events/**").hasAuthority("ROLE_Admin")
						// donation-controller
						.requestMatchers(HttpMethod.GET, "/donation/history/**").authenticated()
						.requestMatchers(HttpMethod.GET, "/donation/export").hasAuthority("ROLE_Admin")
						.requestMatchers(HttpMethod.GET, "/donation/**").permitAll()

						.requestMatchers(HttpMethod.POST, "/donation/**").denyAll()
						.requestMatchers(HttpMethod.POST, "/*/donation/**").denyAll()
						.requestMatchers(HttpMethod.DELETE, "/donation/**").denyAll()

						// user-controller
						.requestMatchers(HttpMethod.GET, "/users").hasAuthority("ROLE_Admin")
						.requestMatchers(HttpMethod.GET, "/user/*/mail").hasAuthority("ROLE_Admin")

						// default deny
						.anyRequest().denyAll()
				)
				.csrf(csrf -> csrf
						.ignoringRequestMatchers("/webhook")
				)
				.cors(cors -> cors.configurationSource(corsConfigurationSource))
				.oauth2ResourceServer(oauth2 -> oauth2
								.jwt(jwt -> jwt.jwtAuthenticationConverter(jwtAuthenticationConverter("http://localhost/roles")))
						//.jwt(withDefaults())
				)
				.build();
	}

	private JwtAuthenticationConverter jwtAuthenticationConverter(String attribuut) {
		JwtAuthenticationConverter converter = new JwtAuthenticationConverter();
		converter.setJwtGrantedAuthoritiesConverter(jwt -> {
			Map<String, Object> claims = jwt.getClaims();
			if (claims.containsKey(attribuut)) {
				Collection<GrantedAuthority> authorities = ((Collection<String>) claims.get(attribuut)).stream()
						.map(role -> new SimpleGrantedAuthority("ROLE_" + role))
						.collect(Collectors.toList());
				return authorities;
			}
			return Collections.emptyList();
		});
		return converter;
	}

}