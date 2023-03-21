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
package be.atbash.testing.integration.database.jupiter;

import be.atbash.testing.integration.ConfigurationException;
import be.atbash.testing.integration.container.exception.UnexpectedException;
import be.atbash.testing.integration.container.image.CustomBuildFile;
import be.atbash.testing.integration.database.SupportedDatabase;
import be.atbash.testing.integration.database.test.AbstractDatabaseContainerIntegrationTest;
import be.atbash.testing.integration.jupiter.AbstractContainerIntegrationTestExtension;
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.extension.ExtensionContext;
import org.junit.platform.commons.support.AnnotationSupport;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.lang.reflect.Constructor;
import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * The JUnit5 extension that orchestrates the logic for the Integration with the Jakarta Runtime and Database.
 */
public class DatabaseContainerIntegrationTestExtension extends AbstractContainerIntegrationTestExtension {

    private DatabaseTestcontainersController controller;

    private SupportedDatabase database;

    @Override
    public void beforeAll(ExtensionContext extensionContext) throws Exception {
        Class<?> testClass = extensionContext.getRequiredTestClass();
        checkTestClass(testClass);

        DatabaseContainerIntegrationTest containerIntegrationTest = testClass.getAnnotation(DatabaseContainerIntegrationTest.class);
        CustomBuildFile customBuildFileAnnotation = testClass.getAnnotation(CustomBuildFile.class);
        List<Field> restClientFields = AnnotationSupport.findAnnotatedFields(testClass, RestClient.class);
        metaData = ContainerAdapterMetaData.create(containerIntegrationTest.containerIntegrationTest(), restClientFields, customBuildFileAnnotation);

        JdbcDatabaseContainer<?> jdbcDatabaseContainer = determineDatabase(containerIntegrationTest);
        controller = new DatabaseTestcontainersController(testClass, jdbcDatabaseContainer, database, containerIntegrationTest);

        controller.config(metaData);

        controller.start();
    }

    private JdbcDatabaseContainer<?> determineDatabase(DatabaseContainerIntegrationTest containerIntegrationTest) {
        List<SupportedDatabase> foundDatabases = Arrays.stream(SupportedDatabase.values())
                .filter(sd -> checkClass(sd.getClassName()) != null)
                .collect(Collectors.toList());

        if (foundDatabases.size() != 1) {
            throw new ConfigurationException("None or multiple database containers found. Exactly one supported database container must be on classpath");
        }

        database = foundDatabases.get(0);

        String databaseImageName = determineDatabaseImageName(containerIntegrationTest);
        JdbcDatabaseContainer<?> jdbcDatabaseContainer;
        try {
            Class<?> databaseContainerClassName = Class.forName(database.getClassName());
            Constructor<?> databaseContainerConstructor = databaseContainerClassName.getDeclaredConstructor(String.class);
            jdbcDatabaseContainer = (JdbcDatabaseContainer<?>) databaseContainerConstructor.newInstance(databaseImageName);

        } catch (InstantiationException | IllegalAccessException | InvocationTargetException | NoSuchMethodException |
                 ClassNotFoundException e) {
            throw new UnexpectedException("Unexpected Exception during instantiation of Database Container", e);
        }

        return jdbcDatabaseContainer;
    }

    private String determineDatabaseImageName(DatabaseContainerIntegrationTest containerIntegrationTest) {
        String result = containerIntegrationTest.databaseContainerImageName();
        if (result == null || result.trim().isBlank()) {
            result = database.getDockerImageName();
        }
        return result;
    }


    private static Class<?> checkClass(String className) {
        Class<?> result = null;
        try {
            result = Class.forName(className);
        } catch (ClassNotFoundException e) {
            ; // Just a test, lets continue
        }
        return result;
    }

    @Override
    public void afterAll(ExtensionContext extensionContext) throws Exception {
        controller.stop();
    }

    @Override
    public void afterEach(ExtensionContext extensionContext) throws Exception {
        controller.resetWireMock();
        controller.clearData();
    }

    @Override
    public void postProcessTestInstance(Object testInstance, ExtensionContext extensionContext) throws Exception {
        controller.injectInstances(testInstance);
        prepareClients(testInstance, controller.getApplicationTestContainer());
    }

    @Override
    public void beforeEach(ExtensionContext context) throws Exception {
        controller.uploadData();
    }

    @Override
    protected Class<?> getRequiredSuperClassForTest() {
        return AbstractDatabaseContainerIntegrationTest.class;
    }
}
