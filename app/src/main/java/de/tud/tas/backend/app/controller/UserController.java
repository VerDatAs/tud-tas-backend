/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Peine, Robert Schmidt, Sebastian Kucharski, Tommy Kubica)
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
package de.tud.tas.backend.app.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tud.tas.backend.app.dto.LongLivedTokenDto;
import de.tud.tas.backend.app.dto.UserDto;
import de.tud.tas.backend.app.dto.UserLanguageDto;
import de.tud.tas.backend.app.dto.UserRoleDto;
import de.tud.tas.backend.app.mapper.LongLivedTokenMapper;
import de.tud.tas.backend.app.mapper.UserMapper;
import de.tud.tas.backend.app.model.LongLivedToken;
import de.tud.tas.backend.app.model.UserLanguage;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.service.AuthService;
import de.tud.tas.backend.app.service.UserService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.messaging.simp.user.SimpUser;
import org.springframework.messaging.simp.user.SimpUserRegistry;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.UUID;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("api/v1/users")
@Tag(name = "User Controller", description = "The API for managing users and their attributes.")
@RequiredArgsConstructor
public class UserController {
    private final AuthService authService;
    private final SimpUserRegistry simpUserRegistry;
    private final UserService userService;
    private final UserMapper userMapper;

    private final LongLivedTokenMapper longLivedTokenMapper;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "getUsers",
            summary = "The function to retrieve all users as a list.")
    public List<UserDto> getAllUsers() {
        return userService.getAllUsers().stream().map(userMapper::toUserOutput).toList();
    }

    @GetMapping(path = "/ws-connections", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "getUsersWebSocketConnections",
            summary = "The function to retrieve all users WebSocket connections as a list.")
    public List<String> getAllUsersWebSocketConnections() {
        return this.simpUserRegistry
                .getUsers()
                .stream()
                .map(SimpUser::getName)
                .toList();
    }

    @GetMapping(path = "/{userId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') || #userId.toString() == authentication.name")
    @Operation(operationId = "getUserById",
            summary = "The function to get the user with a specified ID.")
    @Parameter(name = "userId", in = ParameterIn.PATH,
            required = true, description = "The ID of the user.")
    public UserDto getUser(@PathVariable UUID userId) {
        return userMapper.toUserOutput(userService.getUser(userId));
    }

    @GetMapping(path = "/me", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getAuthenticatedUser",
            summary = "The function to retrieve the currently authenticated user (based on his/her token).")
    public UserDto getAuthenticatedUser(Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        return userMapper.toUserOutput(userService.getUser(userId));
    }

    @PutMapping(path = "/{userId}/role", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "setRole",
            summary = "The function to set the role of the user with a specified ID.")
    @Parameter(name = "userId", in = ParameterIn.PATH,
            required = true, description = "The ID of the user.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The new role that updates the old one. It can be either ADMIN or STUDENT.", required = true)
    public UserRoleDto updateUserRole(@PathVariable UUID userId, @RequestBody UserRoleDto roleInput) {
        UserRole role = userMapper.toUserRole(roleInput);
        UserRole updatedRole = userService.updateUserRole(userId, role);
        return userMapper.toUserRoleDto(updatedRole);
    }

    @PutMapping(path = "/{userId}/password", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') || #userId.toString() == authentication.name")
    @Operation(operationId = "setPassword",
            summary = "The function to update the password of the user with a specified ID.")
    @Parameter(name = "userId", in = ParameterIn.PATH,
            required = true, description = "The ID of the user.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The new password that updates the old one.", required = true)
    public void updateUserPassword(@PathVariable UUID userId, @RequestBody String passwordInput) throws Exception {
        String rawPassword = new ObjectMapper().readValue(passwordInput, String.class);
        userService.updatePassword(userId, rawPassword);
    }

    @PutMapping(path = "/me/password", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "setPasswordOfAuthenticatedUser",
            summary = "The function to update the password of the currently authenticated user (based on his/her token).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The new password that updates the old one.", required = true)
    public void updateAuthenticatedUserPassword(@RequestBody String passwordInput,
                                                Authentication authentication) throws Exception {
        String rawPassword = new ObjectMapper().readValue(passwordInput, String.class);
        UUID userId = UUID.fromString(authentication.getName());
        userService.updatePassword(userId, rawPassword);
    }

    @PutMapping(path = "/{userId}/language", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN') || #userId.toString() == authentication.name")
    @Operation(operationId = "setLanguage",
            summary = "The function to set the language of the user with a specified ID.")
    @Parameter(name = "userId", in = ParameterIn.PATH,
            required = true, description = "The ID of the user.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The new language that updates the old one. It can be either either EN or DE.", required = true)
    public UserLanguageDto updateUserLanguage(@PathVariable UUID userId, @RequestBody UserLanguageDto languageInput) {
        UserLanguage language = userMapper.toUserLanguage(languageInput);
        UserLanguage updatedLanguage = userService.updateUserLanguage(userId, language);
        return userMapper.toUserLanguageDto(updatedLanguage);
    }

    @PutMapping(path = "/me/language", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "setLanguageOfAuthenticatedUser",
            summary = "The function to set the language of the currently authenticated user (based on his/her token).")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The new language that updates the old one. It can be either EN or DE.",
            required = true)
    public UserLanguageDto updateAuthenticatedUserLanguage(@RequestBody UserLanguageDto languageInput,
                                                           Authentication authentication) {
        UUID userId = UUID.fromString(authentication.getName());
        UserLanguage language = userMapper.toUserLanguage(languageInput);
        UserLanguage updatedLanguage = userService.updateUserLanguage(userId, language);
        return userMapper.toUserLanguageDto(updatedLanguage);
    }

    @PutMapping(path = "/me/token", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('STATEMENT_SENDER') || hasRole('ADMIN')")
    @Operation(operationId = "createLongLivedToken",
            summary = "The function to create a long-lived access token for authentication mechanisms that do not support renewal. " +
                    "If such a token has already been created for a user, only the token ID will be returned.")
    public LongLivedTokenDto generateLongLivedToken(Authentication authentication, HttpServletResponse response) {
        UUID userId = UUID.fromString(authentication.getName());
        LongLivedToken longLivedToken = authService.createLongLivedTokenOrGetTokenId(userId);

        response.setStatus(longLivedToken.getToken() == null ?
                HttpServletResponse.SC_OK : HttpServletResponse.SC_CREATED);

        return longLivedTokenMapper.toTokenOutput(longLivedToken);
    }

    @DeleteMapping(path = "/me/token")
    @PreAuthorize("hasRole('STATEMENT_SENDER') || hasRole('ADMIN')")
    @Operation(operationId = "revokeLongLivedToken", summary = "The function to revoke a long-lived access token.")
    public void revokeLongLivedToken(Authentication authentication) {
        userService.updateLongLivedTokenId(UUID.fromString(authentication.getName()), null);
    }
}
