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
package de.tud.tas.backend.app.controller;

import de.tud.tas.backend.app.dto.AssistanceTypeDto;
import de.tud.tas.backend.app.exceptions.AssistanceTypeNotFoundException;
import de.tud.tas.backend.app.mapper.AssistanceTypeMapper;
import de.tud.tas.backend.app.service.AssistanceTypeService;
import de.tud.tas.backend.app.service.TudAssistanceBackboneService;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceBundle;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceInitiationRequest;
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
@RequestMapping("api/v1/assistance")
@RequiredArgsConstructor
@Tag(name = "Assistance Controller", description = "The API responsible for assistance related functionalities.")
public class AssistanceController {
    private final AssistanceTypeMapper assistanceTypeMapper;
    private final AssistanceTypeService assistanceTypeService;
    private final TudAssistanceBackboneService tudAssistanceBackboneService;

    @PostMapping(produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "initiateAssistance",
            summary = "The function to initiate assistance by invoking the TUD Assistance Backbone.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "The generation request that specifies which assistance to generate", required = true)
    public AssistanceBundle initiateAssistance(@Valid @RequestBody AssistanceInitiationRequest assistanceInitiationRequest) {
        return tudAssistanceBackboneService.initiateAssistance(assistanceInitiationRequest.getType(),
                assistanceInitiationRequest.getParameters());
    }

    @GetMapping(path = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getAssistanceTypes",
            summary = "Get all assistance types as a list")
    public List<AssistanceTypeDto> getAllAssistanceTypes() {
        return assistanceTypeService.getAssistanceTypeDtos();
    }

    @GetMapping(path = "/types/{key}", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("isAuthenticated()")
    @Operation(operationId = "getAssistanceTypeByKey",
            summary = "Get the assistance type with the specified key.")
    @Parameter(name = "key", in = ParameterIn.PATH,
            required = true, description = "The key of the assistance type.")
    public AssistanceTypeDto getAssistanceType(@PathVariable String key) {
        return assistanceTypeService.getAssistanceTypeDtos()
                .stream()
                .filter(assistanceTypeDto -> assistanceTypeDto.getKey().equals(key))
                .findAny()
                .orElseThrow(AssistanceTypeNotFoundException::new);
    }

    @PutMapping(path = "/types", produces = MediaType.APPLICATION_JSON_VALUE)
    @PreAuthorize("hasRole('ADMIN')")
    @Operation(operationId = "setAssistanceTypes",
            summary = "The function to set the assistance types to the provided types by overwriting the existing ones.")
    @io.swagger.v3.oas.annotations.parameters.RequestBody(description =
            "List of assistance types to set", required = true)
    public List<AssistanceTypeDto> updateAssistanceType(
            @Valid @RequestBody List<AssistanceTypeDto> assistanceTypeInput) {
        return assistanceTypeService.setAssistanceTypes(assistanceTypeInput
                        .stream()
                        .map(assistanceTypeMapper::toAssistanceType)
                        .toList())
                .stream()
                .map(assistanceTypeMapper::toAssistanceTypeDto)
                .toList();
    }
}
