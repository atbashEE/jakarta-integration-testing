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

import be.atbash.testing.integration.container.image.CustomBuildFile;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;
import com.fasterxml.jackson.jakarta.rs.json.JacksonJsonProvider;
import org.eclipse.microprofile.rest.client.RestClientBuilder;
import org.junit.jupiter.api.Assertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.extension.*;
import org.junit.platform.commons.support.AnnotationSupport;

import java.lang.reflect.Field;
import java.util.List;

/**
 * The JUnit5 extension that orchestrates the logic for the Integration with the Jakarta Runtime.
 */
public class ContainerIntegrationTestExtension extends AbstractContainerIntegrationTestExtension {

    private TestcontainersController controller;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {

        Class<?> testClass = extensionContext.getRequiredTestClass();
        checkTestClass(testClass);
        ContainerIntegrationTest containerIntegrationTest = testClass.getAnnotation(ContainerIntegrationTest.class);
        CustomBuildFile customBuildFileAnnotation = testClass.getAnnotation(CustomBuildFile.class);

        List<Field> restClientFields = AnnotationSupport.findAnnotatedFields(testClass, RestClient.class);
        metaData = ContainerAdapterMetaData.create(containerIntegrationTest, restClientFields, customBuildFileAnnotation);
        TestContext testContext = new TestContext();
        testContext.addInstance(metaData);
        controller = new TestcontainersController(testClass);
        controller.config(metaData, testContext);
        controller.start();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext context) throws Exception {
        prepareClients(testInstance, controller.getApplicationTestContainer());
    }

    @Override
    public void afterEach(ExtensionContext context) throws Exception {
        controller.resetWireMock();
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        controller.stop();
    }

    @Override
    protected Class<?> getRequiredSuperClassForTest() {
        return AbstractContainerIntegrationTest.class;
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        // No-op
    }
}
