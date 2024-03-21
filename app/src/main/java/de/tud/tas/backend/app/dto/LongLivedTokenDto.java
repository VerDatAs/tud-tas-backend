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
import lombok.RequiredArgsConstructor;

import javax.validation.constraints.NotBlank;

@Getter
@RequiredArgsConstructor
@Schema(name = "token",
        title = "Token",
        description = "This represents the schema for encapsulating a long-lived token and its attributes.")
public class LongLivedTokenDto {
    @NotBlank
    @Schema(name = "token",
            title = "Token",
            description = "This represents the actual long-lived token.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final String token;
    @NotBlank
    @NonNull
    @Schema(name = "tokenId",
            title = "TokenId",
            description = "This represents the ID of the token.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private final String tokenId;
}
