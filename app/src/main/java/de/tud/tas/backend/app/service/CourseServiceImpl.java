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
package de.tud.tas.backend.app.service;

import de.tud.tas.backend.app.dto.AssistanceTypeDto;
import de.tud.tas.backend.app.dto.CourseAssistanceTypeDto;
import de.tud.tas.backend.app.dto.CourseDto;
import de.tud.tas.backend.app.dto.CourseFeatureDto;
import de.tud.tas.backend.app.exceptions.CourseNotFoundException;
import de.tud.tas.backend.app.exceptions.FeatureNotFoundException;
import de.tud.tas.backend.app.mapper.AssistanceTypeMapper;
import de.tud.tas.backend.app.mapper.CourseAssistanceTypeMapper;
import de.tud.tas.backend.app.mapper.CourseFeatureMapper;
import de.tud.tas.backend.app.mapper.CourseMapper;
import de.tud.tas.backend.app.model.Course;
import de.tud.tas.backend.app.model.CourseAssistanceType;
import de.tud.tas.backend.app.model.CourseFeature;
import de.tud.tas.backend.app.model.Feature;
import de.tud.tas.backend.app.repository.CourseRepository;
import de.tud.tas.backend.app.repository.FeatureRepository;
import de.tud.tas.backend.app.util.StreamHelper;
import de.tud.tas.backend.tud_assistance_backbone_api_client.api.ExpertModuleApi;
import de.tud.tas.backend.tud_assistance_backbone_api_client.api.ProvisioningApi;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.LearningContentObjectAttributeSearchParameter;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.LearningContentObjectList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
@RequiredArgsConstructor
public class CourseServiceImpl implements CourseService {
    private final AssistanceTypeMapper assistanceTypeMapper;
    private final AssistanceTypeService assistanceTypeService;
    private final CourseAssistanceTypeMapper courseAssistanceTypeMapper;
    private final CourseFeatureMapper courseFeatureMapper;
    private final CourseMapper courseMapper;
    private final CourseRepository courseRepository;
    private final ExpertModuleApi expertModuleApi;
    private final FeatureRepository featureRepository;
    private final ProvisioningApi provisioningApi;

    @Override
    @Transactional
    public void syncCourses() {
        courseRepository.deleteCoursesByObjectIdNotIn(expertModuleApi.searchForLearningContentObjects(
                        List.of(new LearningContentObjectAttributeSearchParameter()
                                .key("lcoType")
                                .value("ILIAS_COURSE")), null, null)
                .getLcos()
                .stream()
                .map(courseLco -> courseMapper.getObjectId(courseMapper.toCourseDto(courseLco)))
                .toList());
    }

    @Override
    public CourseDto getCourseDto(String objectId) {
        LearningContentObjectList courseLcosFromAssistanceSystem = expertModuleApi.searchForLearningContentObjects(
                List.of(new LearningContentObjectAttributeSearchParameter()
                        .key(courseMapper.OBJECT_ID_PARAMETER_KEY)
                        .value(objectId)), null, null);
        if (courseLcosFromAssistanceSystem.getTotalNumber() == 0) {
            throw new CourseNotFoundException("Course with object ID '" + objectId + "' not found!");
        }
        if (courseLcosFromAssistanceSystem.getTotalNumber() > 1) {
            throw new IllegalStateException(
                    "Ambiguous object ID. " + courseLcosFromAssistanceSystem.getTotalNumber() + " LCOs were found!");
        }

        List<CourseAssistanceTypeDto> courseAssistanceTypesFromAssistanceSystem =
                getCourseAssistanceTypeDtosFromAssistanceSystem();

        CourseDto courseLcoFromAssistanceSystem = courseMapper.toCourseDto(courseLcosFromAssistanceSystem.getLcos().get(0));
        Course courseFromDatabase = courseRepository.findById(objectId).orElse(null);
        if (courseFromDatabase == null) {
            courseLcoFromAssistanceSystem.setCourseAssistanceTypes(courseAssistanceTypesFromAssistanceSystem);
            return courseLcoFromAssistanceSystem;
        }

        List<Feature> featuresFromDatabase = featureRepository.findAll();
        Map<String, CourseFeature> courseFeaturesByKey = courseFromDatabase.getCourseFeatures()
                .stream()
                .collect(Collectors.toMap((courseFeature) -> courseFeature.getFeature().getKey(), Function.identity()));
        courseLcoFromAssistanceSystem.setCourseFeatures(featuresFromDatabase.stream().map(feature -> {
                    CourseFeature courseFeature = new CourseFeature();
                    courseFeature.setFeature(feature);
                    courseFeature.setEnabled(courseFeaturesByKey.containsKey(feature.getKey())
                            && courseFeaturesByKey.get(feature.getKey()).isEnabled());
                    return courseFeatureMapper.toCourseFeatureDto(courseFeature);
                })
                .toList());

        List<CourseAssistanceTypeDto> courseAssistanceTypesFromDatabase = courseFromDatabase.getCourseAssistanceTypes()
                .stream().map(courseAssistanceTypeMapper::toCourseAssistanceTypeDto).toList();
        courseLcoFromAssistanceSystem.setCourseAssistanceTypes(Stream.concat(
                courseAssistanceTypesFromDatabase.stream().filter(
                        StreamHelper.isContainedInCollection(courseAssistanceTypesFromAssistanceSystem,
                                CourseAssistanceTypeDto::getKey)),
                courseAssistanceTypesFromAssistanceSystem.stream().filter(
                        StreamHelper.isNotContainedInCollection(courseAssistanceTypesFromDatabase,
                                CourseAssistanceTypeDto::getKey))).toList());
        return courseLcoFromAssistanceSystem;
    }

    @Override
    public List<CourseDto> getAllCourseDtos() {
        LearningContentObjectList courseLcosFromAssistanceSystem = expertModuleApi.searchForLearningContentObjects(
                List.of(new LearningContentObjectAttributeSearchParameter()
                        .key("lcoType")
                        .value("ILIAS_COURSE")), null, null);
        Map<String, Course> coursesFromDatabaseById = courseRepository.findAll()
                .stream()
                .collect(Collectors.toMap(Course::getObjectId, Function.identity()));
        final List<CourseAssistanceTypeDto> courseAssistanceTypesFromAssistanceSystem =
                getCourseAssistanceTypeDtosFromAssistanceSystem();
        return courseLcosFromAssistanceSystem.getLcos()
                .stream()
                .map(courseMapper::toCourseDto)
                .peek(courseDto -> {
                    String objectId = courseMapper.getObjectId(courseDto);
                    if (!coursesFromDatabaseById.containsKey(objectId)) {
                        courseDto.setCourseAssistanceTypes(courseAssistanceTypesFromAssistanceSystem);
                        return;
                    }
                    List<Feature> featuresFromDatabase = featureRepository.findAll();
                    Map<String, CourseFeature> courseFeaturesByKey =
                            coursesFromDatabaseById.get(objectId).getCourseFeatures()
                                    .stream()
                                    .collect(Collectors.toMap((courseFeature) -> courseFeature.getFeature().getKey(),
                                            Function.identity()));
                    courseDto.setCourseFeatures(featuresFromDatabase.stream().map(feature -> {
                                CourseFeature courseFeature = new CourseFeature();
                                courseFeature.setFeature(feature);
                                courseFeature.setEnabled(courseFeaturesByKey.containsKey(feature.getKey())
                                        && courseFeaturesByKey.get(feature.getKey()).isEnabled());
                                return courseFeatureMapper.toCourseFeatureDto(courseFeature);
                            })
                            .toList());

                    List<CourseAssistanceTypeDto> courseAssistanceTypesFromDatabase =
                            coursesFromDatabaseById.get(objectId).getCourseAssistanceTypes()
                                    .stream().map(courseAssistanceTypeMapper::toCourseAssistanceTypeDto).toList();
                    courseDto.setCourseAssistanceTypes(Stream.concat(courseAssistanceTypesFromDatabase.stream().filter(
                                    StreamHelper.isContainedInCollection(courseAssistanceTypesFromAssistanceSystem,
                                            CourseAssistanceTypeDto::getKey)),
                            courseAssistanceTypesFromAssistanceSystem.stream().filter(
                                    StreamHelper.isNotContainedInCollection(courseAssistanceTypesFromDatabase,
                                            CourseAssistanceTypeDto::getKey))).toList());
                })
                .toList();
    }

    @Override
    public List<CourseFeatureDto> getCourseFeatureDtos(String objectId) {
        return getCourseDto(objectId).getCourseFeatures();
    }

    @Override
    @Transactional
    public List<CourseFeature> updateCourseFeatures(String objectId, List<CourseFeature> courseFeatures) {
        if (courseFeatures.stream().anyMatch(f -> !featureRepository.existsById(f.getFeature().getKey()))) {
            throw new FeatureNotFoundException();
        }

        Course course = courseMapper.toCourse(getCourseDto(objectId));
        course.setCourseFeatures(courseFeatures);
        courseRepository.save(course);

        assistanceTypeService.updateCourseAssistanceTypesPreconditionFulfilledByKeys(course,
                course.getCourseAssistanceTypes().stream().map(CourseAssistanceType::getKey).toList());
        return courseFeatures;
    }

    @Override
    public List<CourseAssistanceTypeDto> getCourseAssistanceTypeDtos(String objectId) {
        return getCourseDto(objectId).getCourseAssistanceTypes();
    }

    @Override
    @Transactional
    public List<CourseAssistanceType> configureCourseAssistanceTypes(
            String objectId, List<CourseAssistanceType> courseAssistanceTypes) {
        Course course = courseMapper.toCourse(getCourseDto(objectId));

        List<CourseAssistanceType> courseAssistanceTypesToSet = course
                .getCourseAssistanceTypes()
                .stream()
                .peek(setCourseAssistanceType -> {
                    courseAssistanceTypes
                            .stream()
                            .filter(courseAssistanceTypeToSet ->
                                    courseAssistanceTypeToSet.getKey().equals(setCourseAssistanceType.getKey()))
                            .findAny()
                            .ifPresent(courseAssistanceType -> setCourseAssistanceType
                                    .setEnabled(courseAssistanceType.isEnabled()));

                })
                .filter(courseAssistanceType -> !courseAssistanceType.isEnabled()
                        || !courseAssistanceType.isPreConditionFulfilled())
                .toList();

        course.setCourseAssistanceTypes(courseAssistanceTypesToSet);
        courseRepository.save(course);
        return courseAssistanceTypesToSet;
    }

    private List<CourseAssistanceTypeDto> getCourseAssistanceTypeDtosFromAssistanceSystem() {
        return provisioningApi.getSupportedAssistanceTypes(null, null, null).getTypes()
                .stream().map(assistanceType -> {
                    AssistanceTypeDto assistanceTypeDto = assistanceTypeMapper.toAssistanceTypeDto(assistanceType);
                    CourseAssistanceTypeDto courseAssistanceTypeDto = new CourseAssistanceTypeDto();
                    courseAssistanceTypeDto.setKey(assistanceTypeDto.getKey());
                    courseAssistanceTypeDto.setEnabled(true);
                    return courseAssistanceTypeDto;
                }).toList();
    }
}
