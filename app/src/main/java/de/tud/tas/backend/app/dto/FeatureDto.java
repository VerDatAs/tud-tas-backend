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
@Schema(name = "feature",
        title = "Feature",
        description = "This represents a feature that can be used for course features.")
public class FeatureDto {
    @NotBlank
    @NonNull
    @Schema(name = "key",
            title = "Key",
            description = "This represents the key of the feature. The key is not case-sensitive.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
}
