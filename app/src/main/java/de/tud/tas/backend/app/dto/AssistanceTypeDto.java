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

import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceType;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(name = "assistanceType",
        title = "Assistance Type",
        description = "This represents an assistance type that can be used for a course assistance type.")
public class AssistanceTypeDto extends AssistanceType {
    @NonNull
    @Schema(name = "requiredFeatures",
            title = "Required Features",
            description = "This represents the features that have to be set before the assistance type is enabled.",
            requiredMode = Schema.RequiredMode.REQUIRED)
    private List<FeatureDto> requiredFeatures;

    public AssistanceTypeDto requiredFeatures(List<FeatureDto> requiredFeatures) {
        this.requiredFeatures = requiredFeatures;
        return this;
    }
}