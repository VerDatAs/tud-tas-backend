/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Peine, Robert Schmidt, Sebastian Kucharski)
 *
 *  This program is free software: you can redistribute it and/or modify
 *  it under the terms of the GNU General Public License as published by
 *  the Free Software Foundation, either version 3 of the License, or
 *  (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU General Public License for more details.
 *
 *  You should have received a copy of the GNU General Public License
 *  along with this program.  If not, see <http://www.gnu.org/licenses/>.
 ***************************************************************************************/
package de.tud.tas.backend.app.config;

import de.tud.tas.backend.app.exceptions.UserNotFoundException;
import de.tud.tas.backend.app.filter.LongLivedJwtFilter;
import de.tud.tas.backend.app.service.UserService;
import de.tud.tas.backend.app.model.UserRole;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableGlobalMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.annotation.web.configurers.oauth2.server.resource.OAuth2ResourceServerConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.server.resource.web.BearerTokenAuthenticationFilter;
import org.springframework.security.provisioning.InMemoryUserDetailsManager;
import org.springframework.security.web.SecurityFilterChain;

@Configuration
@EnableWebSecurity
@EnableGlobalMethodSecurity(prePostEnabled = true)
@RequiredArgsConstructor
public class WebSecurityConfig {
    @Value("${tas-backend.statement.sender.username}")
    private String statementSenderUsername;

    @Value("${tas-backend.statement.sender.password}")
    private String statementSenderPassword;

    private final PasswordEncoder passwordEncoder;
    private final JwtDecoder jwtDecoder;
    private final UserService userService;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        LongLivedJwtFilter longLivedJwtFilter = new LongLivedJwtFilter(userService, jwtDecoder);

        return http.csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .oauth2ResourceServer(OAuth2ResourceServerConfigurer::jwt)
                .addFilterAfter(longLivedJwtFilter, BearerTokenAuthenticationFilter.class)
                .httpBasic().and()
                .build();
    }

    @Bean
    public UserDetailsService userDetailsService() {
        UserDetails statementSender = User.builder()
                .username(statementSenderUsername)
                .password(passwordEncoder.encode(statementSenderPassword))
                .roles(UserRole.STATEMENT_SENDER.name())
                .build();
        try {
            userService.getUserByActorAccountName(statementSenderUsername);
            userService.deleteUser(statementSenderUsername);
        } catch (UserNotFoundException e){
            // Nothing to do here.
        }
        userService.addUser(statementSenderUsername, UserRole.STATEMENT_SENDER, statementSenderPassword);

        return new InMemoryUserDetailsManager(statementSender);
    }
}
