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
package be.atbash.testing.integration.container;

import be.atbash.testing.integration.container.image.DockerImageProcessor;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;


class DockerImageProcessorTest {

    private static final String VERSION_PROPERTY = "be.atbash.test.runtime.version";

    @AfterEach
    public void cleanup() {
        System.clearProperty(VERSION_PROPERTY);

    }

    @Test
    void getImage() throws IOException {
        ImageFromDockerfile image = DockerImageProcessor.getImage(SupportedRuntime.PAYARA_MICRO, "src/test/resources/test.war", null);

        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getDockerfile()).isPresent();

        // Is the DockerFile created in a directory starting with atbash.test
        Path dockerFileLocation = image.getDockerfile().get().getParent();
        Assertions.assertThat(dockerFileLocation.getFileName().toString()).startsWith("atbash.test.");

        // In temp Directory?
        String tempLocation = Files.createTempDirectory(null).getParent().toString();
        Assertions.assertThat(dockerFileLocation.toString()).startsWith(tempLocation);

        // Is war also copied in same directory?
        Path warPath = dockerFileLocation.resolve("test.war");
        Assertions.assertThat(warPath.toFile().exists()).isTrue();

        // Check content
        String dockerFileContent = String.join("\n", Files.readAllLines(image.getDockerfile().get()));

        Assertions.assertThat(dockerFileContent).isEqualTo("FROM payara/micro:5.2022.2-jdk11\n" +
                "CMD [\"--deploy\", \"/opt/payara/deployments/test.war\", \"--noCluster\",  \"--contextRoot\", \"/\"]\n" +
                "ADD test.war /opt/payara/deployments ");
    }

    @Test
    void getImage_versionSpecified() throws IOException {
        System.setProperty(VERSION_PROPERTY, "1234");
        ImageFromDockerfile image = DockerImageProcessor.getImage(SupportedRuntime.PAYARA_MICRO, "src/test/resources/test.war", null);

        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getDockerfile()).isPresent();

        String dockerFileContent = String.join("\n", Files.readAllLines(image.getDockerfile().get()));

        Assertions.assertThat(dockerFileContent).isEqualTo("FROM payara/micro:1234\n" +
                "CMD [\"--deploy\", \"/opt/payara/deployments/test.war\", \"--noCluster\",  \"--contextRoot\", \"/\"]\n" +
                "ADD test.war /opt/payara/deployments ");

    }

    @Test
    void getImage_imageSpecified() throws IOException {
        System.setProperty(VERSION_PROPERTY, "myImage:4321");
        ImageFromDockerfile image = DockerImageProcessor.getImage(SupportedRuntime.PAYARA_MICRO, "src/test/resources/test.war", null);

        Assertions.assertThat(image).isNotNull();
        Assertions.assertThat(image.getDockerfile()).isPresent();

        String dockerFileContent = String.join("\n", Files.readAllLines(image.getDockerfile().get()));

        Assertions.assertThat(dockerFileContent).isEqualTo("FROM myImage:4321\n" +
                "CMD [\"--deploy\", \"/opt/payara/deployments/test.war\", \"--noCluster\",  \"--contextRoot\", \"/\"]\n" +
                "ADD test.war /opt/payara/deployments ");

    }
}