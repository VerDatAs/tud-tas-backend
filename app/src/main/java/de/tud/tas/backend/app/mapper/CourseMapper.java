/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Peine, Robert Schmidt, Sebastian Kucharski)
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
package de.tud.tas.backend.app.mapper;

import de.tud.tas.backend.app.dto.CourseDto;
import de.tud.tas.backend.app.model.Course;
import de.tud.tas.backend.app.model.CourseAssistanceType;
import de.tud.tas.backend.app.repository.CourseRepository;
import org.mapstruct.Mapper;
import org.mapstruct.ReportingPolicy;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.function.Function;
import java.util.Map;
import java.util.stream.Collectors;

@SuppressWarnings("SpringJavaAutowiredFieldsWarningInspection")
@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE)
public abstract class CourseMapper {
    @Autowired
    private CourseAssistanceTypeMapper courseAssistanceTypeMapper;
    @Autowired
    private CourseFeatureMapper courseFeatureMapper;
    @Autowired
    private CourseRepository courseRepository;

    public final String OBJECT_ID_PARAMETER_KEY = "objectId";

    public abstract CourseDto toCourseDto(de.tud.tas.backend.tud_assistance_backbone_api_client.model.LearningContentObject value);

    public Course toCourse(CourseDto value) {
        if (value == null) {
            return null;
        }

        String objectId = getObjectId(value);
        Course storedCourse = courseRepository.findById(objectId).orElse(null);

        if (storedCourse == null) {
            return new Course(
                    objectId,
                    value.getCourseAssistanceTypes()
                            .stream().map(courseAssistanceTypeMapper::toCourseAssistanceType).toList(),
                    value.getCourseFeatures().stream().map(courseFeatureMapper::toCourseFeature).toList());
        }

        Map<String, CourseAssistanceType> storedCourseAssistanceTypesByKey = storedCourse.getCourseAssistanceTypes()
                .stream()
                .collect(Collectors.toMap(CourseAssistanceType::getKey, Function.identity()));
        return new Course(
                objectId,
                value.getCourseAssistanceTypes()
                        .stream()
                        .map(courseAssistanceTypeMapper::toCourseAssistanceType)
                        .peek(courseAssistanceType -> {
                            if (!storedCourseAssistanceTypesByKey.containsKey(courseAssistanceType.getKey())) {
                                return;
                            }
                            courseAssistanceType.setPreConditionFulfilled(
                                    storedCourseAssistanceTypesByKey.get(courseAssistanceType.getKey())
                                            .isPreConditionFulfilled());
                        })
                        .toList(),
                value.getCourseFeatures().stream().map(courseFeatureMapper::toCourseFeature).toList());
    }

    public String getObjectId(CourseDto courseDto) {
        return courseDto.getObjectId();
    }
}
