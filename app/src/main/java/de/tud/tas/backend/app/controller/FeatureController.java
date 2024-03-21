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
package de.tud.tas.backend.app.controller;

import de.tud.tas.backend.app.dto.FeatureDto;
import de.tud.tas.backend.app.mapper.FeatureMapper;
import de.tud.tas.backend.app.service.FeatureService;
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
@RequestMapping("api/v1/features")
@Tag(name = "Feature Controller", description = "The API for managing features and their attributes.")
@RequiredArgsConstructor
public class FeatureController {
    private final FeatureMapper featureMapper;
    private final FeatureService featureService;

    @GetMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getFeatures",
            summary = "The function to retrieve all features as a list.")
    public List<FeatureDto> getAllFeatures() {
        return featureService.getAllFeatures().stream().map(featureMapper::toFeatureDto).toList();
    }

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "addFeature",
            summary = "The function to add a feature.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description = "The feature to be added.", required = true)
    public FeatureDto addFeature(@Valid @RequestBody FeatureDto featureDto) {
        return featureMapper.toFeatureDto(featureService.addFeature(featureDto.getKey().toLowerCase()));
    }

    @DeleteMapping(path = "/{featureKey}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "deleteFeature",
            summary = "The function to delete the feature with a specified feature key.")
    @Parameter(name = "featureKey", in = ParameterIn.PATH,
            required = true, description = "The key of the feature to be deleted. This key is not case-sensitive.")
    public FeatureDto deleteFeature(@PathVariable String featureKey) {
        return featureMapper.toFeatureDto(featureService.deleteFeature(featureKey.toLowerCase()));
    }
}
