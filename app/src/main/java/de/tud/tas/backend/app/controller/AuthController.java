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

import de.tud.tas.backend.app.dto.LoginInputDto;
import de.tud.tas.backend.app.dto.LoginOutputDto;
import de.tud.tas.backend.app.service.AuthService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;

@RestController
@RequestMapping("api/v1/auth")
@RequiredArgsConstructor
@Tag(name = "Auth Controller", description = "The API to receive a JWT token as a user.")
public class AuthController {
    private final AuthService authService;

    @PostMapping(path = "/login", produces = MediaType.APPLICATION_JSON_VALUE,
            consumes = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("permitAll()")
    @ResponseStatus(HttpStatus.CREATED)
    @Operation(operationId = "userLogin",
            summary = "The function to retrieve a JWT and create a user if it does not exist.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The input with a username and an optional password of the user.", required = true)
    public LoginOutputDto login(@Valid @RequestBody LoginInputDto loginInputDto) {
        String token = authService.login(loginInputDto.getActorAccountName(), loginInputDto.getPassword());
        return new LoginOutputDto(token);
    }
}
