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

import de.tud.tas.backend.tud_assistance_backbone_api_client.model.LearningContentObject;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
@Schema(name = "course",
        title = "Course",
        description = "This represents a course and its attributes.")
public class CourseDto extends LearningContentObject {
    @Schema(name = "courseFeatures",
            title = "CourseFeatures",
            description = "This represents the features of the course which describe if a specific features is enabled or not for the course.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CourseFeatureDto> courseFeatures;
    @Schema(name = "courseAssistanceTypes",
            title = "CourseAssistanceTypes",
            description = "This represents the assistance types of the course, describing whether or not a particular assistance type is enabled for the course and the pre-condition that must be fulfilled.",
            requiredMode = Schema.RequiredMode.NOT_REQUIRED)
    private List<CourseAssistanceTypeDto> courseAssistanceTypes;

    public CourseDto courseFeatures(List<CourseFeatureDto> courseFeatures) {
        this.courseFeatures = courseFeatures;
        return this;
    }

    public CourseDto courseAssistanceTypes(List<CourseAssistanceTypeDto> courseAssistanceTypes) {
        this.courseAssistanceTypes = courseAssistanceTypes;
        return this;
    }
}
