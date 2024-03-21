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

import de.tud.tas.backend.app.exceptions.FeatureNotFoundException;
import de.tud.tas.backend.app.model.*;
import de.tud.tas.backend.app.model.*;
import de.tud.tas.backend.app.repository.AssistanceTypeRepository;
import de.tud.tas.backend.app.repository.CourseRepository;
import de.tud.tas.backend.app.repository.FeatureRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;

@Service
@RequiredArgsConstructor
public class FeatureServiceImpl implements FeatureService {
    private final AssistanceTypeRepository assistanceTypeRepository;
    private final AssistanceTypeService assistanceTypeService;
    private final CourseRepository courseRepository;
    private final FeatureRepository featureRepository;

    @Override
    public List<Feature> getAllFeatures() {
        return featureRepository.findAll();
    }

    @Override
    @Transactional
    public Feature addFeature(String key) {
        return featureRepository.save(new Feature(key));
    }

    @Override
    @Transactional
    public Feature deleteFeature(String key) {
        Feature featureFromDatabase = featureRepository.findById(key).orElseThrow(FeatureNotFoundException::new);

        // Delete feature as precondition from assistance types
        List<AssistanceType> assistanceTypesToRemove = new ArrayList<>();
        List<AssistanceType> assistanceTypesToUpdate = new ArrayList<>();
        assistanceTypeRepository.getAssistanceTypesByRequiredFeaturesContaining(featureFromDatabase)
                .forEach(assistanceType -> {
                    List<Feature> updatedRequiredFeaturesList = assistanceType.getRequiredFeatures()
                            .stream()
                            .filter(feature -> !feature.getKey().equals(key))
                            .toList();
                    if (updatedRequiredFeaturesList.isEmpty()) {
                        assistanceTypesToRemove.add(assistanceType);
                    } else {
                        assistanceType.setRequiredFeatures(updatedRequiredFeaturesList);
                        assistanceTypesToUpdate.add(assistanceType);
                    }
                });
        assistanceTypeRepository.deleteAll(assistanceTypesToRemove);
        assistanceTypeRepository.saveAll(assistanceTypesToUpdate);

        // Update requirements fulfilled of course assistance types
        List<Course> coursesToRemove = new ArrayList<>();
        List<Course> coursesToUpdate = new ArrayList<>();
        courseRepository.findCoursesByCourseFeaturesIsNotNull()
                .stream()
                .filter(course -> course.getCourseFeatures()
                        .stream()
                        .map(courseFeature -> courseFeature.getFeature().getKey())
                        .anyMatch(featureKey -> featureKey.equals(key)))
                .forEach(course -> {
                    List<CourseFeature> updatedFeaturesOfCourseList = course.getCourseFeatures()
                            .stream()
                            .filter(feature -> !feature.getFeature().getKey().equals(key))
                            .toList();
                    // If the course after the deletion of the feature does not contain any feature and no specific
                    // assistance types are disabled remove the course from the db
                    if ((updatedFeaturesOfCourseList.isEmpty())
                            && (course.getCourseAssistanceTypes()
                            .stream()
                            .allMatch(courseAssistanceType -> courseAssistanceType.isEnabled()
                                    && courseAssistanceType.isPreConditionFulfilled()))) {
                        coursesToRemove.add(course);
                    } else {
                        course.setCourseFeatures(updatedFeaturesOfCourseList);
                        coursesToUpdate.add(course);
                    }
                });
        courseRepository.deleteAll(coursesToRemove);
        courseRepository.saveAll(coursesToUpdate);

        featureRepository.delete(featureFromDatabase);

        for (Course course : coursesToUpdate) {
            assistanceTypeService.updateCourseAssistanceTypesPreconditionFulfilledByKeys(
                    course, course.getCourseAssistanceTypes().stream().map(CourseAssistanceType::getKey).toList());
        }

        return featureFromDatabase;
    }
}
