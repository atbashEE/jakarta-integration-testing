/*
 * Copyright 2022 Rudy De Busscher (https://www.atbash.be)
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
package be.atbash.testing.integration.wiremock;

import be.atbash.testing.integration.wiremock.model.MappingBuilder;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.utility.DockerImageName;

import java.net.URI;
import java.util.List;

public class WireMockContainer extends GenericContainer<WireMockContainer> {

    private WireMockAdminService wireMockAdminService;

    private WireMockContainer(String hostName) {
        super(DockerImageName.parse("wiremock/wiremock:2.34.0"));
        setNetwork(Network.SHARED);
        setNetworkAliases(List.of(hostName));
        addExposedPorts(8080);
    }

    public void configureResponse(MappingBuilder mappingBuilder) {
        initWireMockAdminService();
        wireMockAdminService.submitMapping(mappingBuilder.build());
    }

    private void initWireMockAdminService() {
        if (wireMockAdminService != null) {
            return;
        }

        URI baseURI = URI.create(String.format("http://localhost:%s", getMappedPort(8080)));

        wireMockAdminService = RestClientBuilder.newBuilder().  // From MicroProfile Rest Client
                register(JacksonJsonProvider.class).  // Support JSON-B
                baseUri(baseURI).
                build(WireMockAdminService.class);

    }

    public void resetMapping() {
        initWireMockAdminService();
        wireMockAdminService.resetMapping();
    }

    public static WireMockContainer forHost(String hostName) {
        return new WireMockContainer(hostName);
    }
}
