package com.substring.auth.auth_app_backend.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.substring.auth.auth_app_backend.security.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.authentication.configuration.AuthenticationConfiguration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

import java.util.Map;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Autowired
    private JwtAuthenticationFilter  jwtAuthenticationFilter;

    // 1. Basic Authentication
    // 2. converted in JWT Authentication
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception{
        http    .csrf(AbstractHttpConfigurer::disable)
                .cors(Customizer.withDefaults())
                .sessionManagement(sm->sm.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests((authorize) ->
                        authorize.requestMatchers("/api/v1/auth/register").permitAll()
                                 .requestMatchers("/api/v1/auth/login").permitAll()
                                 .anyRequest().authenticated()
                         ) //.httpBasic(Customizer.withDefaults());
                .exceptionHandling(ex->ex.authenticationEntryPoint((request, response, authException) ->{
                    authException.printStackTrace();
                    response.setStatus(401);
                    response.setContentType("application/json;charset=utf-8");
                    String message = "Unauthorized Access "+ authException.getMessage();
                    Map<String,String> errorMap = Map.of("message",message,"status",String.valueOf(401),"statusCode", new Integer (401).toString());
                    var objectMapper = new ObjectMapper();
                    response.getWriter().write(objectMapper.writeValueAsString(errorMap));
                } ))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);
        return http.build();
    }

    @Bean
    public PasswordEncoder passwordEncoder(){
        return new BCryptPasswordEncoder();
    }

    @Bean
    public AuthenticationManager authenticationManager(AuthenticationConfiguration configuration) throws Exception {
        return configuration.getAuthenticationManager();
    }

    // Sample Code for UserDetailsService
    /*
    @Bean
    public UserDetailsService users(){
        User.UserBuilder userbuilder = User.withDefaultPasswordEncoder();
        UserDetails user1 = userbuilder.username("admin").password("admin").roles("ADMINS").build();

        return new InMemoryUserDetailsManager(user1);
    }*/
}
