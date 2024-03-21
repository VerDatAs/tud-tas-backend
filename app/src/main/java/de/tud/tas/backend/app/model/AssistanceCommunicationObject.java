/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Max Schaible, Sebastian Kucharski)
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
package de.tud.tas.backend.app.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.fasterxml.jackson.annotation.JsonProperty;
import de.tud.tas.backend.tud_assistance_backbone_api_client.model.AssistanceParameter;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.ToString;
import org.springframework.data.annotation.Id;

import javax.validation.Valid;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Getter
@Setter
@ToString
@NoArgsConstructor
public class AssistanceCommunicationObject {
    @Id
    private UUID messageId;
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'")
    private LocalDateTime timestamp;
    private String userId;
    @JsonProperty("aId")
    private String aId;
    private String contextId;
    private String assistanceType;
    private String aoId;
    @Valid
    private List<@Valid AssistanceParameter> parameters;

    public AssistanceCommunicationObject(LocalDateTime timestamp, String userId, String aId, String assistanceType,
                                         String aoId, List<@Valid AssistanceParameter> parameters, String contextId) {
        this.messageId = UUID.randomUUID();
        this.timestamp = timestamp;
        this.userId = userId;
        this.aId = aId;
        this.contextId = contextId;
        this.assistanceType = assistanceType;
        this.aoId = aoId;
        this.parameters = parameters;
    }

    public AssistanceCommunicationObject(
            LocalDateTime timestamp, String userId, List<@Valid AssistanceParameter> parameters) {
        this.messageId = UUID.randomUUID();
        this.timestamp = timestamp;
        this.userId = userId;
        this.parameters = parameters;
    }

    public AssistanceCommunicationObject(List<@Valid AssistanceParameter> parameters) {
        this.parameters = parameters;
    }

}
