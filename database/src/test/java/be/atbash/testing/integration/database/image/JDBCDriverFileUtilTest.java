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
package be.atbash.testing.integration.database.image;

import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.database.SupportedDatabase;
import be.atbash.testing.integration.database.exception.FileNotFoundException;
import be.atbash.testing.integration.database.jupiter.DatabaseContainerAdapterMetaData;
import be.atbash.testing.integration.database.jupiter.DatabaseContainerIntegrationTest;
import be.atbash.testing.integration.database.jupiter.JDBCDriverArtifact;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtensionConfigurationException;
import org.mockito.Mockito;

class JDBCDriverFileUtilTest {

    @Test
    void getDriverFile() {
        // This assumes that the MySQL Driver 5.1.49 is inn the local maven repo!
        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("", ""));
        String driverFile = JDBCDriverFileUtil.getDriverFile(testContext);
        Assertions.assertThat(driverFile).endsWith("/mysql/mysql-connector-java/5.1.49/mysql-connector-java-5.1.49.jar");
    }

    @Test
    void getDriverFile_customMavenArtifact() {
        // The maven string doesnt' point a JDBC driver, but we just check if it exists.
        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("", "be.atbash.test:integration-testing:1.1.0"));
        String driverFile = JDBCDriverFileUtil.getDriverFile(testContext);
        Assertions.assertThat(driverFile).endsWith("/be/atbash/test/integration-testing/1.1.0/integration-testing-1.1.0.jar");
    }

    @Test
    void getDriverFile_customFile() {
        // src/test/resources/driver.jar is just an empty file, but will do for passing the existing file test we perform.
        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("src/test/resources/driver.jar", "groupId:artifactId:1.2.3"));
        String driverFile = JDBCDriverFileUtil.getDriverFile(testContext);
        Assertions.assertThat(driverFile).endsWith("src/test/resources/driver.jar");
    }

    @Test
    void getDriverFile_customFile_NotFound() {

        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("/path/to/none_existing/file.jar", "groupId:artifactId:1.2.3"));
        Assertions.assertThatThrownBy(() ->
                        JDBCDriverFileUtil.getDriverFile(testContext))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("The JDBC Driver file is not found. This was the resolved path /path/to/none_existing/file.jar");

    }

    @Test
    void getDriverFile_customMavenArtifact_NotFound() {
        // src/test/resources/driver.jar is just an empty file, but will do for passing the existing file test we perform.
        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("", "groupId:artifactId:1.2.3"));
        Assertions.assertThatThrownBy(() ->
                        JDBCDriverFileUtil.getDriverFile(testContext))
                .isInstanceOf(FileNotFoundException.class)
                .hasMessage("The JDBC Driver file is not found. This was the resolved path /Users/rubus/mvn_repo/groupId/artifactId/1.2.3/artifactId-1.2.3.jar");
    }

    @Test
    void getDriverFile_customMavenArtifact_WrongFormat() {
        // src/test/resources/driver.jar is just an empty file, but will do for passing the existing file test we perform.
        TestContext testContext = new TestContext();
        testContext.addInstance(defineDatabaseMetaData("", "something"));
        Assertions.assertThatThrownBy(() ->
                        JDBCDriverFileUtil.getDriverFile(testContext))
                .isInstanceOf(ExtensionConfigurationException.class)
                .hasMessage("The maven coordinates must be defined as 'groupId:artifactId:version'");
    }

    private DatabaseContainerAdapterMetaData defineDatabaseMetaData(String driverJarFile, String mavenArtifact) {
        DatabaseContainerAdapterMetaData mock = Mockito.mock(DatabaseContainerAdapterMetaData.class);
        DatabaseContainerIntegrationTest databaseContainerIntegrationTestMock = Mockito.mock(DatabaseContainerIntegrationTest.class);
        Mockito.when(mock.getDatabaseContainerIntegrationTest()).thenReturn(databaseContainerIntegrationTestMock);
        Mockito.when(mock.getDatabase()).thenReturn(SupportedDatabase.MYSQL);

        JDBCDriverArtifact driverArtifactMock = Mockito.mock(JDBCDriverArtifact.class);
        Mockito.when(databaseContainerIntegrationTestMock.jdbcDriverArtifact()).thenReturn(driverArtifactMock);

        Mockito.when(driverArtifactMock.driverJarFile()).thenReturn(driverJarFile);
        Mockito.when(driverArtifactMock.mavenArtifact()).thenReturn(mavenArtifact);
        return mock;
    }
}