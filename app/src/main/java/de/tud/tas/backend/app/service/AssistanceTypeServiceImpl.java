/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Schmidt, Sebastian Kucharski)
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
import de.tud.tas.backend.app.exceptions.AssistanceTypeNotFoundException;
import de.tud.tas.backend.app.mapper.AssistanceTypeMapper;
import de.tud.tas.backend.app.mapper.FeatureMapper;
import de.tud.tas.backend.app.model.*;
import de.tud.tas.backend.app.model.*;
import de.tud.tas.backend.app.repository.AssistanceTypeRepository;
import de.tud.tas.backend.app.repository.CourseRepository;
import de.tud.tas.backend.tud_assistance_backbone_api_client.api.ProvisioningApi;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceTypeList;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AssistanceTypeServiceImpl implements AssistanceTypeService {
    private final AssistanceTypeRepository assistanceTypeRepository;
    private final AssistanceTypeMapper assistanceTypeMapper;
    private final CourseRepository courseRepository;
    private final FeatureMapper featureMapper;
    private final ProvisioningApi provisioningApi;

    @Override
    @Transactional
    public void syncAssistanceTypes() {
        AssistanceTypeList assistanceTypeList = provisioningApi.getSupportedAssistanceTypes(
                null, null, null);
        if (assistanceTypeList.getProvidedNumber() != 0) {
            assistanceTypeRepository.deleteAssistanceTypesByKeyNotIn(
                    assistanceTypeList
                            .getTypes()
                            .stream()
                            .map(de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceType::getKey)
                            .toList());
        }

        courseRepository.findCoursesByCourseAssistanceTypesIsNotNull()
                .forEach(course -> updateCourseAssistanceTypesPreconditionFulfilledByKeys(course,
                        course.getCourseAssistanceTypes().stream().map(CourseAssistanceType::getKey).toList()));
    }

    @Override
    public List<AssistanceTypeDto> getAssistanceTypeDtos() {
        AssistanceTypeList assistanceTypesFromAssistanceSystem =
                provisioningApi.getSupportedAssistanceTypes(null, null, null);
        Map<String, AssistanceType> assistanceTypesFromDatabaseByKey = assistanceTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(AssistanceType::getKey, Function.identity()));
        return assistanceTypesFromAssistanceSystem.getTypes()
                .stream()
                .map(assistanceTypeMapper::toAssistanceTypeDto)
                .peek(assistanceTypeDto -> {
                    if (!assistanceTypesFromDatabaseByKey.containsKey(assistanceTypeDto.getKey())) {
                        return;
                    }
                    assistanceTypeDto.setRequiredFeatures(
                            assistanceTypesFromDatabaseByKey.get(assistanceTypeDto.getKey()).getRequiredFeatures()
                                    .stream()
                                    .map(featureMapper::toFeatureDto)
                                    .toList());
                })
                .toList();
    }

    @Override
    @Transactional
    public List<AssistanceType> setAssistanceTypes(List<AssistanceType> assistanceTypes) {
        List<String> persistedAssistanceTypeKeys = getAssistanceTypeDtos()
                .stream().map(AssistanceTypeDto::getKey).toList();
        if (assistanceTypes.stream().map(AssistanceType::getKey).anyMatch(k -> !persistedAssistanceTypeKeys.contains(k))) {
            throw new AssistanceTypeNotFoundException();
        }

        assistanceTypeRepository.deleteAll();
        assistanceTypeRepository.saveAll(assistanceTypes
                .stream()
                .filter(assistanceType -> assistanceType.getRequiredFeatures() != null
                        && !assistanceType.getRequiredFeatures().isEmpty())
                .toList());
        courseRepository.findAll().forEach(course -> updateCourseAssistanceTypesPreconditionFulfilled(course, assistanceTypes));

        return assistanceTypes;
    }

    @Override
    @Transactional
    public void updateCourseAssistanceTypesPreconditionFulfilledByKeys(
            Course course, List<String> assistanceTypeKeysToSet) {
        Map<String, AssistanceType> existingAssistanceTypesByKey = getAssistanceTypeDtos()
                .stream()
                .map(assistanceTypeMapper::toAssistanceType)
                .collect(Collectors.toMap(AssistanceType::getKey, Function.identity()));
        Map<String, AssistanceType> assistanceTypesFromDatabaseByKey = assistanceTypeRepository.findAll()
                .stream()
                .collect(Collectors.toMap(AssistanceType::getKey, Function.identity()));

        List<AssistanceType> assistanceTypesToDelete = new ArrayList<>();
        List<AssistanceType> assistanceTypesToSet = assistanceTypeKeysToSet
                .stream()
                .map(assistanceTypeKey -> {
                    if (!existingAssistanceTypesByKey.containsKey(assistanceTypeKey)) {
                        if (assistanceTypesFromDatabaseByKey.containsKey(assistanceTypeKey)) {
                            assistanceTypesToDelete.add(assistanceTypesFromDatabaseByKey.get(assistanceTypeKey));
                            return null;
                        }
                        throw new AssistanceTypeNotFoundException(assistanceTypeKey + " not found!");
                    }
                    return existingAssistanceTypesByKey.get(assistanceTypeKey);
                })
                .filter(Objects::nonNull)
                .toList();

        assistanceTypeRepository.deleteAll(assistanceTypesToDelete);
        updateCourseAssistanceTypesPreconditionFulfilled(course, assistanceTypesToSet);
    }

    private void updateCourseAssistanceTypesPreconditionFulfilled(
            Course course, List<AssistanceType> assistanceTypesToSet) {
        List<Feature> enabledFeatures = course.getCourseFeatures() == null ? Collections.emptyList() :
                course.getCourseFeatures()
                        .stream()
                        .filter(CourseFeature::isEnabled)
                        .map(CourseFeature::getFeature)
                        .toList();

        Map<String, CourseAssistanceType> previousAssistanceTypeKeysToAssistanceTypes =
                course.getCourseAssistanceTypes()
                        .stream()
                        .collect(Collectors.toMap(CourseAssistanceType::getKey, Function.identity()));
        List<CourseAssistanceType> courseAssistanceTypes = assistanceTypesToSet
                .stream()
                .map(assistanceType -> {
                    boolean enabled = !previousAssistanceTypeKeysToAssistanceTypes.containsKey(assistanceType.getKey())
                            || previousAssistanceTypeKeysToAssistanceTypes.get(assistanceType.getKey()).isEnabled();
                    if ((assistanceType.getRequiredFeatures() == null || assistanceType.getRequiredFeatures().isEmpty())
                            && enabled) {
                        return null;
                    }
                    boolean preConditionFulfilled = assistanceType.getRequiredFeatures() == null ||
                            new HashSet<>(enabledFeatures).containsAll(assistanceType.getRequiredFeatures());
                    return new CourseAssistanceType(assistanceType.getKey(), enabled, preConditionFulfilled);
                })
                .filter(Objects::nonNull)
                .toList();

        if ((course.getCourseFeatures() == null || course.getCourseFeatures().isEmpty())
                && courseAssistanceTypes
                .stream()
                .allMatch(courseAssistanceType -> courseAssistanceType.isEnabled()
                        && courseAssistanceType.isPreConditionFulfilled())) {
            courseRepository.delete(course);
        } else {
            course.setCourseAssistanceTypes(courseAssistanceTypes);
            courseRepository.save(course);
        }
    }
}
