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
package be.atbash.testing.integration.jupiter;

import be.atbash.testing.integration.container.AbstractIntegrationContainer;
import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.*;

import java.lang.reflect.Field;
import java.net.URI;

public abstract class AbstractContainerIntegrationTestExtension  implements BeforeAllCallback, BeforeEachCallback,  AfterAllCallback, TestInstancePostProcessor, AfterEachCallback {

    protected ContainerAdapterMetaData metaData;

    protected void checkTestClass(Class<?> testClass) {
        if (!hasAbstractClass(testClass.getSuperclass())) {
            Assertions.fail(String.format("The class '%s' annotated with @ContainerIntegrationTest must extend from '%s'", testClass.getName(), AbstractContainerIntegrationTest.class.getName()));
        }
    }

    private boolean hasAbstractClass(Class<?> testClass) {
        boolean result = getRequiredSuperClassForTest().isAssignableFrom(testClass);
        if (!result && !testClass.equals(Object.class)) {
            return hasAbstractClass(testClass.getSuperclass());
        }
        return result;
    }

    protected void prepareClients(Object testInstance, AbstractIntegrationContainer<?> container) throws IllegalAccessException {
        String root = "";

        if (metaData.getSupportedRuntime() == SupportedRuntime.WILDFLY) {
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

    protected abstract Class<?> getRequiredSuperClassForTest();
}
