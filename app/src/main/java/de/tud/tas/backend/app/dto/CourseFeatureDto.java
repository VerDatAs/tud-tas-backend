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

@Getter
@Setter
@Schema(name = "courseFeature",
        title = "Course Feature",
        description = "This represents a feature that belongs to a specific course.")
public class CourseFeatureDto {
    @NonNull
    @Schema(name = "feature",
            title = "Feature",
            description = "This represents the actual feature.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private FeatureDto feature;
    @Schema(name = "enabled",
            title = "Enabled",
            description = "This indicates whether the feature is enabled or not. If not, it will not be used to evaluate course assistance types.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private boolean enabled;
}
