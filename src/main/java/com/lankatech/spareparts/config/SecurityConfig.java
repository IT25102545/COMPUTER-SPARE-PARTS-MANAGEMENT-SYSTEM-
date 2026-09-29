package com.lankatech.spareparts.config;

import com.lankatech.spareparts.auth.service.CustomUserDetailsService;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import org.springframework.security.authentication.AuthenticationProvider;
import org.springframework.security.authentication.dao.DaoAuthenticationProvider;

import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;

import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;

import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    /*
     * Password encoder used to securely store
     * and verify user passwords.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }


    /*
     * Connects our database UserDetailsService
     * with Spring Security authentication.
     */
    @Bean
    public AuthenticationProvider authenticationProvider(
            CustomUserDetailsService customUserDetailsService,
            PasswordEncoder passwordEncoder) {

        DaoAuthenticationProvider authenticationProvider =
                new DaoAuthenticationProvider(customUserDetailsService);

        authenticationProvider.setPasswordEncoder(passwordEncoder);

        return authenticationProvider;
    }


    /*
     * Main security configuration.
     */
    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            AuthenticationProvider authenticationProvider)
            throws Exception {

        http

                /*
                 * Use users stored in our database.
                 */
                .authenticationProvider(authenticationProvider)

                /*
                 * Keep the current JavaScript API requests working.
                 *
                 * Later we can add full CSRF-token support to the
                 * frontend and remove this API exception.
                 */
                .csrf(csrf -> csrf
                        .ignoringRequestMatchers(
                                "/login",
                                "/logout",
                                "/api/**"
                        )
                )

                /*
                 * Decide which URLs can be accessed
                 * without logging in.
                 */
                .authorizeHttpRequests(auth -> auth

                        /*
                         * Public homepage and static resources.
                         */
                        .requestMatchers(
                                "/",
                                "/index.html",
                                "/assets/**",
                                "/css/**",
                                "/images/**",
                                "/js/**",
                                "/favicon.ico",
                                "/error",
                                "/login",
                                "/api/auth/me"
                        ).permitAll()
                        .requestMatchers("/stock-transfer.html")
                        .hasAnyRole(
                                "BRANCH_SUPERVISOR",
                                "INVENTORY_SUPERVISOR"
                        )

                        .requestMatchers("/api/stock-transfers/**")
                        .hasAnyRole(
                                "BRANCH_SUPERVISOR",
                                "INVENTORY_SUPERVISOR"
                        )
                        /*
                         * Everything else requires login.
                         */
                        .anyRequest().authenticated()
                )

                /*
                 * Custom login processing.
                 *
                 * User enters EMAIL instead of username.
                 */
                .exceptionHandling(exception -> exception

                        .authenticationEntryPoint((request, response, authException) ->
                                response.sendRedirect("/?loginRequired=true")
                        )

                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                response.sendRedirect("/?accessDenied=true")
                        )
                )
                .formLogin(form -> form

                        /*
                         * Homepage contains our login UI.
                         */
                        .loginPage("/")

                        /*
                         * Spring Security receives POST /login.
                         */
                        .loginProcessingUrl("/login")

                        /*
                         * Name of login form fields.
                         */
                        .usernameParameter("email")
                        .passwordParameter("password")

                        /*
                         * After successful login.
                         */
                        .defaultSuccessUrl("/", true)

                        /*
                         * If email/password is incorrect.
                         */
                        .failureUrl("/?loginError=true")

                        .permitAll()
                )

                /*
                 * Logout configuration.
                 */
                .logout(logout -> logout
                        .logoutUrl("/logout")
                        .logoutSuccessUrl("/?logout=true")
                        .invalidateHttpSession(true)
                        .deleteCookies("JSESSIONID")
                        .permitAll()
                );

        return http.build();
    }
}