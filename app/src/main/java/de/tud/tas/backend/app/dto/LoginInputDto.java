/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski)
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
package de.tud.tas.backend.app.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import javax.validation.constraints.NotBlank;

@Getter
@Setter
@Schema(name = "login",
        title = "Login",
        description = "This represents the schema for encapsulating login credentials.")
public class LoginInputDto {
    @NotBlank
    @NonNull
    @Schema(name = "actorAccountName",
            title = "Actor Account Name",
            description = "This represents the account name of the user.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String actorAccountName;
    @Schema(name = "password",
            title = "Password",
            description = "This represents the password of the user.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private String password;
}
