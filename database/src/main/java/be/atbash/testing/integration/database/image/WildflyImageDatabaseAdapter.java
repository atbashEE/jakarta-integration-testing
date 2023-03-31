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

import be.atbash.testing.integration.container.AdditionalEnvParameters;
import be.atbash.testing.integration.container.exception.UnexpectedException;
import be.atbash.testing.integration.container.image.DockerImageAdapter;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.database.jupiter.DatabaseContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.SupportedRuntime;

import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Arrays;
import java.util.List;

public class WildflyImageDatabaseAdapter extends AbstractImageDatabaseAdapter implements DockerImageAdapter {

    public static final String TEST_DS_XML = "test-ds.xml";
    public static final String ENTRYPOINT_SH = "entrypoint.sh";
    public static final String STANDALONE_DEPLOYMENTS = "/opt/jboss/wildfly/standalone/deployments";
    public static final String WILDFLY_BIN = "/opt/jboss/wildfly/bin";

    @Override
    public String adapt(String dockerFileContent, TestContext testContext) {
        ContainerAdapterMetaData containerMetaData = testContext.getInstance(ContainerAdapterMetaData.class);
        DatabaseContainerAdapterMetaData databaseMetaData = testContext.getInstance(DatabaseContainerAdapterMetaData.class);

        String fileName = copyJDBCDriver(testContext);
        AdditionalEnvParameters parameters = new AdditionalEnvParameters();
        parameters.add("driver_jar", fileName);
        parameters.add("jndi_name", databaseMetaData.getDatabaseContainerIntegrationTest().jndiDatasourceName());
        testContext.addInstance(parameters);

        List<String> dockerFileLines = Arrays.asList(dockerFileContent.split("\n"));

        dockerFileLines = updateBuildFileForDriver(dockerFileLines, STANDALONE_DEPLOYMENTS, fileName);

        Path tempDir = containerMetaData.getTempDir();
        copyFile(tempDir, TEST_DS_XML);
        copyFile(tempDir, ENTRYPOINT_SH);

        addInstallationFor_envsubst(dockerFileLines);

        int line = findLine(dockerFileLines, "ADD test.war");
        dockerFileLines.add(line, String.format("COPY --chown=1000:jboss %s %s", TEST_DS_XML, STANDALONE_DEPLOYMENTS));
        dockerFileLines.add(line + 1, String.format("COPY --chown=1000:jboss %s %s", ENTRYPOINT_SH, WILDFLY_BIN));
        dockerFileLines.add(line + 2, String.format("RUN chmod +x %s/%s", WILDFLY_BIN, ENTRYPOINT_SH));
        dockerFileLines.add(line + 3, "CMD [\"" + WILDFLY_BIN + "/entrypoint.sh\"]");

        return String.join("\n", dockerFileLines);
    }

    private void addInstallationFor_envsubst(List<String> dockerFileLines) {
        dockerFileLines.add(1, "USER root");
        dockerFileLines.add(2, "RUN yum -y install gettext && yum clean all");
        dockerFileLines.add(3, "USER 1000");

    }

    private void copyFile(Path tempDir, String name) {
        String content = readResourceFile(name);
        try {
            saveFileToTemp(content, tempDir, name);
        } catch (IOException e) {
            throw new UnexpectedException(String.format("Exception during write of file %s to temp directory", name), e);
        }
    }

    protected void saveFileToTemp(String content, Path tempDirWithPrefix, String name) throws IOException {
        Path dockerPath = tempDirWithPrefix.resolve(name);
        try (BufferedWriter writer = Files.newBufferedWriter(dockerPath)) {
            writer.write(content);
        }
    }

    private String readResourceFile(String name) {
        String text;
        try {
            text = new String(WildflyImageDatabaseAdapter.class.getResourceAsStream("/" + name).readAllBytes(), StandardCharsets.UTF_8);
        } catch (IOException | NullPointerException e) {
            throw new UnexpectedException(String.format("Exception during read of '%s' from classpath", name), e);
        }

        return text;
    }


    @Override
    public SupportedRuntime supportedRuntime() {
        return SupportedRuntime.WILDFLY;
    }
}
