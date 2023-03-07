/*
 * Copyright 2022-2023 Rudy De Busscher (https://www.atbash.be)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package be.atbash.test.integration.example.wiremock;

import be.atbash.testing.integration.jupiter.ContainerIntegrationTest;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;
import be.atbash.testing.integration.wiremock.WireMockContainer;
import be.atbash.testing.integration.wiremock.model.mappings.MappingBuilder;
import be.atbash.testing.integration.wiremock.model.requests.RequestInfo;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.testcontainers.junit.jupiter.Container;

import jakarta.ws.rs.WebApplicationException;


@ContainerIntegrationTest(runtime = SupportedRuntime.WILDFLY)
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class WireMockWildflyIT extends AbstractContainerIntegrationTest {
    // Should be public and not the JUnit 5 preferred scope package.

    @Container
    public static final WireMockContainer wireMockContainer = WireMockContainer.forHost("wire");

    @RestClient
    public TestService testService;

    @Test
    @Order(1)
    void testEndpoints() {
        MappingBuilder mappingBuilder = new MappingBuilder().forURL("/path").withBody(createTestDataObject());
        String mappingId = wireMockContainer.configureResponse(mappingBuilder);

        String value = testService.getValue();
        Assertions.assertThat(value).isEqualTo("Data[id=123, name='Atbash testing']");

        // Test if we actually called the WireMock endpoint.
        RequestInfo requestInfo = wireMockContainer.getRequestInfo(mappingId);
        Assertions.assertThat(requestInfo).isNotNull();

    }

    @Test
    @Order(2)
    void testReset() {
        // No mapping defined, and thus call should fail since we have done a reset.

        Assertions.assertThatThrownBy(() -> testService.getValue())
                .isInstanceOf(WebApplicationException.class)
                .hasMessage("HTTP 404 Not Found");

    }

    private Data createTestDataObject() {
        Data result = new Data();
        result.setId(123);
        result.setName("Atbash testing");
        return result;
    }
}