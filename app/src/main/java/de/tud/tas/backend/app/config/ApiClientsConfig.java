/****************************************************************************************
 *  TUD TAS Backend for the assistance system developed as part of the VerDatAs project
 *  Copyright (C) 2022-2024 TU Dresden (Sebastian Kucharski)
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

import com.fasterxml.jackson.databind.ObjectMapper;
import de.tud.tas.backend.app.model.User;
import de.tud.tas.backend.app.model.UserLanguage;
import de.tud.tas.backend.app.model.UserRole;
import de.tud.tas.backend.app.service.AuthService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.web.client.RestTemplateBuilder;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.BufferingClientHttpRequestFactory;
import org.springframework.http.converter.HttpMessageConverter;
import org.springframework.http.converter.json.MappingJackson2HttpMessageConverter;
import org.springframework.web.client.RestTemplate;
import org.springframework.web.util.DefaultUriBuilderFactory;

import java.time.Duration;
import java.util.UUID;

@Configuration
@RequiredArgsConstructor
@ComponentScan(basePackages = {"de.tud.tas.backend.learning_locker_api_client.api",
        "de.tud.tas.backend.tud_assistance_backbone_api_client.api"})
public class ApiClientsConfig {
    @Value("${learning-locker.url}")
    private String learningLockerUrl;
    @Value("${learning-locker.username}")
    private String learningLockerUsername;
    @Value("${learning-locker.password}")
    private String learningLockerPassword;
    @Value("${tud-assistance-backbone.url}")
    private String tudAssistanceBackboneUrl;

    private final AuthService authService;

    @Bean
    public de.tud.tas.backend.learning_locker_api_client.ApiClient learningLockerApiClient(
            ObjectMapper objectMapper) {
        final RestTemplate restTemplate = getRestTemplate(objectMapper);

        de.tud.tas.backend.learning_locker_api_client.ApiClient apiClient =
                new de.tud.tas.backend.learning_locker_api_client.ApiClient(restTemplate);
        apiClient.setBasePath(learningLockerUrl + "/data/xAPI");
        apiClient.addDefaultHeader("X-Experience-API-Version", "1.0.3");
        de.tud.tas.backend.learning_locker_api_client.auth.HttpBasicAuth basicAuth =
                (de.tud.tas.backend.learning_locker_api_client.auth.HttpBasicAuth) apiClient
                        .getAuthentication("basicAuth");
        basicAuth.setUsername(learningLockerUsername);
        basicAuth.setPassword(learningLockerPassword);

        return apiClient;
    }

    @Bean
    public de.tud.tas.backend.tud_assistance_backbone_api_client.ApiClient tudAssistanceBackboneApiClient(
            ObjectMapper objectMapper) {
        de.tud.tas.backend.tud_assistance_backbone_api_client.ApiClient apiClient =
                new de.tud.tas.backend.tud_assistance_backbone_api_client.ApiClient(
                        getRestTemplate(objectMapper));
        apiClient.setBasePath(tudAssistanceBackboneUrl);

        de.tud.tas.backend.tud_assistance_backbone_api_client.auth.HttpBearerAuth bearerAuth =
                (de.tud.tas.backend.tud_assistance_backbone_api_client.auth.HttpBearerAuth) apiClient
                        .getAuthentication("bearerAuth");
        bearerAuth.setBearerToken(authService.createLongLivedJwt(
                new User(UUID.randomUUID(), "tud_tas_backend", UserLanguage.EN, UserRole.ADMIN),
                UUID.randomUUID()));
        return apiClient;
    }

    private static RestTemplate getRestTemplate(ObjectMapper objectMapper) {
        RestTemplate restTemplate = new RestTemplateBuilder()
                .setConnectTimeout(Duration.ofSeconds(8))
                .setReadTimeout(Duration.ofSeconds(8))
                .build();
        // This allows us to read the response more than once - Necessary for debugging.
        restTemplate.setRequestFactory(new BufferingClientHttpRequestFactory(restTemplate.getRequestFactory()));

        // disable default URL encoding
        DefaultUriBuilderFactory uriBuilderFactory = new DefaultUriBuilderFactory();
        uriBuilderFactory.setEncodingMode(DefaultUriBuilderFactory.EncodingMode.VALUES_ONLY);
        restTemplate.setUriTemplateHandler(uriBuilderFactory);

        HttpMessageConverter<Object> httpMessageConverter = new MappingJackson2HttpMessageConverter(objectMapper);
        restTemplate.getMessageConverters().add(0, httpMessageConverter);
        return restTemplate;
    }
}
