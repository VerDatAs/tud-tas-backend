/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Schmidt, Sebastian Kucharski, Tommy Kubica)
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
import java.util.UUID;

@Getter
@Setter
@Schema(name = "user",
        title = "User",
        description = "This represents a user and his/her attributes.")
public class UserDto {
    @NotBlank
    @NonNull
    @Schema(name = "id",
            title = "Id",
            description = "This represents the ID of the user.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UUID id;
    @NotBlank
    @NonNull
    @Schema(name = "actorAccountName",
            title = "ActorAccountName",
            description = "This represents the name of the user.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String actorAccountName;
    @NotBlank
    @NonNull
    @Schema(name = "language",
            title = "Language",
            description = "This represents the language that the user is speaking.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UserLanguageDto language;
    @NotBlank
    @NonNull
    @Schema(name = "role",
            title = "Role",
            description = "This represents the role of the user.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private UserRoleDto role;
}
