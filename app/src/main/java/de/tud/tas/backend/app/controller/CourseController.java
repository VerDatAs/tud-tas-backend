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

import de.tud.tas.backend.app.dto.CourseAssistanceTypeDto;
import de.tud.tas.backend.app.dto.CourseDto;
import de.tud.tas.backend.app.dto.CourseFeatureDto;
import de.tud.tas.backend.app.mapper.CourseAssistanceTypeMapper;
import de.tud.tas.backend.app.mapper.CourseFeatureMapper;
import de.tud.tas.backend.app.service.CourseService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.enums.ParameterIn;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.MediaType;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.validation.Valid;
import java.util.List;

@RestController
@SecurityRequirement(name = "bearerAuth")
@RequestMapping("api/v1/courses")
@Tag(name = "Course Controller", description = "The API for managing courses and their attributes.")
@RequiredArgsConstructor
public class CourseController {
    private final CourseAssistanceTypeMapper courseAssistanceTypeMapper;
    private final CourseFeatureMapper courseFeatureMapper;
    private final CourseService courseService;

    @GetMapping(path = "/{objectId}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getCourseById",
            summary = "The function to retrieve the course with a specified object ID.")
    @Parameter(name = "objectId", in = ParameterIn.PATH,
            required = true, description = "The object ID of the course.")
    public CourseDto getCourse(@PathVariable String objectId) {
        return courseService.getCourseDto(objectId);
    }

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "getCourses",
            summary = "The function to retrieve all courses as a list.")
    public List<CourseDto> getAllCourses() {
        return courseService.getAllCourseDtos();
    }

    @GetMapping(path = "/{objectId}/features", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getCourseFeatures",
            summary = "The function to retrieve the features of the course with a specified object ID.")
    @Parameter(name = "objectId", in = ParameterIn.PATH,
            required = true, description = "The object ID of the course.")
    public List<CourseFeatureDto> getCourseFeatures(@PathVariable String objectId) {
        return courseService.getCourseFeatureDtos(objectId);
    }

    @PutMapping(path = "/{objectId}/features", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "setCourseFeatures",
            summary = "The function to create or update the features of the course with a specified object ID.")
    @Parameter(name = "objectId", in = ParameterIn.PATH,
            required = true, description = "The object ID of the course.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The list of new course features that update the old ones.", required = true)
    public List<CourseFeatureDto> updateCourseFeatures(
            @PathVariable String objectId, @Valid @RequestBody List<CourseFeatureDto> courseFeatures) {
        return courseService.updateCourseFeatures(objectId, courseFeatures
                        .stream()
                        .map(courseFeatureMapper::toCourseFeature)
                        .toList())
                .stream()
                .map(courseFeatureMapper::toCourseFeatureDto)
                .toList();
    }

    @GetMapping(path = "/{objectId}/assistance-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getCourseAssistanceTypes",
            summary = "The function to retrieve the assistance types of the course with a specified object ID.")
    @Parameter(name = "objectId", in = ParameterIn.PATH,
            required = true, description = "The object ID of the course.")
    public List<CourseAssistanceTypeDto> getCourseAssistanceTypes(@PathVariable String objectId) {
        return courseService.getCourseAssistanceTypeDtos(objectId);
    }

    @PostMapping(path = "/{objectId}/assistance-types", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "configureCourseAssistanceTypes",
            summary = "The function to enable or disable specific CourseAssistanceTypes of a specific course. " +
                    "This method cannot be used to add assistance types to a course, as all assistance types are " +
                    "always added to all courses and can then only be enabled or disabled.")
    @Parameter(name = "objectId", in = ParameterIn.PATH,
            required = true, description = "The object ID of the course.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The new course assistance types with the configuration that update the old ones.", required = true)
    public List<CourseAssistanceTypeDto> configureCourseAssistanceTypes(
            @PathVariable String objectId,
            @Valid @RequestBody List<CourseAssistanceTypeDto> courseAssistanceTypeDtos) {
        return courseService.configureCourseAssistanceTypes(objectId, courseAssistanceTypeDtos
                        .stream()
                        .map(courseAssistanceTypeMapper::toCourseAssistanceType)
                        .toList())
                .stream()
                .map(courseAssistanceTypeMapper::toCourseAssistanceTypeDto)
                .toList();
    }
}
