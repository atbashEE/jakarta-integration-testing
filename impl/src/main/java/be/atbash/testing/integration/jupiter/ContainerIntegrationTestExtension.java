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
package be.atbash.testing.integration.jupiter;

import be.atbash.testing.integration.container.AbstractIntegrationContainer;
import com.fasterxml.jackson.jaxrs.json.JacksonJsonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.extension.AfterAllCallback;
import org.junit.jupiter.api.extension.BeforeAllCallback;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.jupiter.api.extension.TestInstancePostProcessor;

import java.lang.reflect.Field;
import java.net.URI;

/**
 * The JUnit5 extension that orchestrates the logic for the Integration with the Jakarta Runtime.
 */
public class ContainerIntegrationTestExtension implements BeforeAllCallback, AfterAllCallback, TestInstancePostProcessor {

    private TestcontainersController controller;

    private ContainerAdapterMetaData metaData;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        Class<?> testClass = extensionContext.getRequiredTestClass();
        metaData = ContainerAdapterMetaData.create(testClass);
        controller = new TestcontainersController(testClass);
        controller.config(metaData);
        controller.start();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        AbstractIntegrationContainer<?> container = controller.getApplicationTestContainer();

        String root = "";
        if (metaData.getSupportedRuntime() == SupportedRuntime.WILDFLY || metaData.getSupportedRuntime() == SupportedRuntime.GLASSFISH) {
            // putting apps in /deployments directory of Wildfly uses the file name as root.
            // TODO Check if this is a problem for cross runtime compatibility. (for example when having multiple microservices)
            root = "/test";
        }
        URI baseURI = URI.create(String.format("http://localhost:%s%s", container.getMappedPort(metaData.getPort()), root));

        for (Field field : metaData.getRestClientFields()) {
            Object restClient = RestClientBuilder.newBuilder().  // From MicroProfile Rest Client
                    register(JacksonJsonProvider.class).  // Support JSON-B
                    baseUri(baseURI).
                    build(field.getType());  // Create proxy based on the interface and information of the endpoints.

            field.setAccessible(true);  // TODO Why is this required
            field.set(testInstance, restClient);
        }

    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        controller.stop();
    }


}
