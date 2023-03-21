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
import be.atbash.testing.integration.container.ContainerFactory;
import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;
import be.atbash.testing.integration.wiremock.WireMockContainer;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.BindMode;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.junit.jupiter.Container;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Control and manipulate all testContainers.
 */
public class TestcontainersController {

    protected final Logger LOGGER = LoggerFactory.getLogger(TestcontainersController.class);

    protected final Set<GenericContainer<?>> containers = new HashSet<>();
    protected final Set<WireMockContainer> wireMockContainers = new HashSet<>();
    private final Class<?> testClass;
    private Field runtimeContainerField;

    private AbstractIntegrationContainer<?> applicationTestContainer;

    public TestcontainersController(Class<?> testClass) {
        this.testClass = testClass;
        discoverContainers(testClass);

    }

    private void discoverContainers(Class<?> clazz) {

        for (Field containerField : AnnotationSupport.findAnnotatedFields(clazz, Container.class)) {
            if (!Modifier.isPublic(containerField.getModifiers())) {
                throw new ExtensionConfigurationException("@Container annotated fields must be public visibility");
            }
            if (!Modifier.isStatic(containerField.getModifiers())) {
                throw new ExtensionConfigurationException("@Container annotated fields must be static");
            }
            boolean isStartable = GenericContainer.class.isAssignableFrom(containerField.getType());
            if (!isStartable) {
                throw new ExtensionConfigurationException("@Container annotated fields must be a subclass of " + GenericContainer.class);
            }
            try {
                boolean generic = true;
                if (containerField.getDeclaringClass().equals(AbstractContainerIntegrationTest.class)) {
                    // Only a Container within AbstractIntegrationContainer is the one from the framework
                    // Other ones are 'generic' and defined by developer.
                    runtimeContainerField = containerField;
                    generic = false;
                }

                if (generic) {
                    // Some other container the developer uses in the test.
                    containerField.setAccessible(true);  // Why is this required? it is a public static field
                    GenericContainer<?> startableContainer = (GenericContainer<?>) containerField.get(null);
                    if (!(startableContainer instanceof WireMockContainer)) {
                        startableContainer.setNetwork(Network.SHARED);
                        startableContainer.withNetworkAliases(containerField.getName());  // Use variable name as host alias
                    } else {
                        wireMockContainers.add((WireMockContainer) startableContainer);
                    }
                    containers.add(startableContainer);

                }
            } catch (IllegalArgumentException | IllegalAccessException e) {
                LOGGER.warn("Unable to access field " + containerField, e);
            }
        }

    }

    public void config(ContainerAdapterMetaData metaData) {
        // Configure the container.
        // ContainerAdapterMetaData determine the container which will be used.
        applicationTestContainer = new ContainerFactory().createContainer(metaData);

        if (metaData.isLiveLogging()) {
            Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
            applicationTestContainer.followOutput(logConsumer);  // Show log of container in output.
        }
        defineVolumeMapping(metaData.getVolumeMapping());

        try {
            runtimeContainerField.set(null, applicationTestContainer);
            containers.add(applicationTestContainer);

        } catch (IllegalAccessException e) {
            Assertions.fail(e.getMessage());
        }

    }

    private void defineVolumeMapping(Map<String, String> volumeMapping) {
        volumeMapping
                .forEach((key, value) -> applicationTestContainer.addFileSystemBind(key, value, BindMode.READ_WRITE));
    }

    public AbstractIntegrationContainer<?> getApplicationTestContainer() {
        return applicationTestContainer;
    }

    public void start() {

        showContainerNames();

        long start = System.currentTimeMillis();
        startContainers();
        LOGGER.info("All containers started in " + (System.currentTimeMillis() - start) + "ms");
    }

    protected void startContainers() {
        containers.parallelStream().forEach(GenericContainer::start);
    }

    protected void showContainerNames(String... additionalContainerNames) {
        LOGGER.info("Starting containers in parallel for " + testClass);
        for (String name : additionalContainerNames) {
            LOGGER.info("  " + name);
        }
        for (GenericContainer<?> c : containers) {
            LOGGER.info("  " + c.getImage());
        }
    }


    public void stop() throws IllegalAccessException {
        // Stop all Containers in the AfterAll phase. Some containers can already be stopped by the AfterEach.
        long start = System.currentTimeMillis();
        stopContainers();
        LOGGER.info("All containers stopped in " + (System.currentTimeMillis() - start) + "ms");
        runtimeContainerField.set(null, null);

    }

    protected void stopContainers() throws IllegalAccessException {
        containers.parallelStream().forEach(GenericContainer::stop);
    }

    public void resetWireMock() {
        wireMockContainers.forEach(WireMockContainer::resetConfigAndDeleteRequests);
    }
}
