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
@Schema(name = "courseAssistanceType",
        title = "Course Assistance Type",
        description = "This represents an assistance type that belongs to a specific course and defines whether or not that assistance type is enabled for that course.")
public class CourseAssistanceTypeDto {
    @NotBlank
    @NonNull
    @Schema(name = "key",
            title = "Key",
            description = "This represents the key of the assistance type.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private String key;
    @Schema(name = "enabled",
            title = "Enabled",
            description = "This indicates whether the assistance type is enabled or not. The default value is `true`.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private boolean enabled;
}
