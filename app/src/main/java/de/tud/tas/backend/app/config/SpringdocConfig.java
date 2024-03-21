/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Robert Peine, Sebastian Kucharski)
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
package de.tud.tas.backend.app.config;

import java.util.List;
import java.util.Optional;


import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.security.SecurityScheme;
import io.swagger.v3.oas.models.servers.Server;
import org.springdoc.core.customizers.OperationCustomizer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.prepost.PreAuthorize;

@Configuration
public class SpringdocConfig {
    @Value("${tas-backend.swagger.server.url}")
    private String serverUrl;

    @Bean
    public OperationCustomizer operationCustomizer() {
        return (operation, handlerMethod) -> {
            Optional.ofNullable(handlerMethod.getMethodAnnotation(PreAuthorize.class)).ifPresent((preAuthorizeExpr) -> {
                StringBuilder stringBuilder = new StringBuilder()
                        .append("**Security @PreAuthorize expression:** `")
                        .append(preAuthorizeExpr.value())
                        .append("`");

                Optional.ofNullable(operation.getDescription()).ifPresent((currentDescription) -> {
                    stringBuilder.append("\n\n");
                    stringBuilder.append(currentDescription);
                });

                operation.setDescription(stringBuilder.toString());
            });

            return operation;
        };
    }

    @Bean
    public OpenAPI customOpenAPI() {
        String securitySchemeName = "bearerAuth";

        Server server = new Server();
        server.setUrl(serverUrl);

        return new OpenAPI()
                .servers(List.of(server))
                .components(new Components().addSecuritySchemes(securitySchemeName, new SecurityScheme()
                        .name(securitySchemeName)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")));
    }
}