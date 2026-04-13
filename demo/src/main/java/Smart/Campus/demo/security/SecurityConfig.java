package Smart.Campus.demo.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

/**
 * Spring Security configuration.
 * - Stateless session (JWT-based, no cookies)
 * - CORS enabled for React frontend (localhost:5173)
 * - Public endpoints: /api/v1/auth/**, /uploads/**
 * - All other endpoints require authentication
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        // Public endpoints
                        .requestMatchers("/api/v1/auth/**").permitAll()
                        .requestMatchers("/uploads/**").permitAll()

                        // Resource management - read access for all authenticated, write for ADMIN
                        .requestMatchers(HttpMethod.GET, "/api/v1/resources/**").authenticated()
                        .requestMatchers(HttpMethod.POST, "/api/v1/resources/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/resources/**").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.DELETE, "/api/v1/resources/**").hasRole("ADMIN")

                        // Booking management - approve/reject for ADMIN
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/approve").hasRole("ADMIN")
                        .requestMatchers(HttpMethod.PUT, "/api/v1/bookings/*/reject").hasRole("ADMIN")

                        // Ticket management - update (assign, status) for ADMIN/TECHNICIAN
                        .requestMatchers(HttpMethod.PUT, "/api/v1/tickets/**").hasAnyRole("ADMIN", "TECHNICIAN")

                        // User role management - ADMIN only
                        .requestMatchers(HttpMethod.PUT, "/api/v1/auth/users/*/role").hasRole("ADMIN")

                        // All other endpoints require authentication
                        .anyRequest().authenticated()
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(List.of("http://localhost:5173", "http://localhost:5174", "http://localhost:3000"));
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(List.of("*"));
        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
