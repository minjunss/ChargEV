package ChargEV.ChargEV.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.csrf.HttpSessionCsrfTokenRepository;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;

import java.util.Collections;
import java.util.List;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    CorsConfigurationSource corsConfigurationSource() {
        return request -> {
            CorsConfiguration config = new CorsConfiguration();
            config.setAllowCredentials(true);
            config.setAllowedOriginPatterns(List.of("http://localhost:8080")); // Restrict to your frontend origin
            config.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE")); // Allow common REST methods
            config.setAllowedHeaders(List.of("Content-Type", "X-CSRF-TOKEN")); // Allow necessary headers
            return config;
        };
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        http
                .cors(corsConfig -> corsConfig.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.csrfTokenRepository(new HttpSessionCsrfTokenRepository())) // Use HttpSessionCsrfTokenRepository
                .httpBasic(AbstractHttpConfigurer::disable)
                .formLogin(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.ALWAYS))
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers("/", "/login", "/signup", "/css/**", "/js/**", "/img/**").permitAll()
                                .requestMatchers("/api/auth/**", "/api/localSearch/**", "/health", "/api/chargingStation/range", "/api/chargingStation/detail").permitAll()
                                .anyRequest().authenticated())
                .logout(logout -> logout
                        .logoutUrl("/api/auth/logout")
                        .logoutSuccessUrl("/")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID"))
        ;

        return http.build();
    }
}


