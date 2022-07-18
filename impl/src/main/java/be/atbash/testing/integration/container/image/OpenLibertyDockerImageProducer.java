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
package be.atbash.testing.integration.container.image;

import org.junit.jupiter.api.Assertions;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class OpenLibertyDockerImageProducer extends DockerImageProducer {

    @Override
    public ImageFromDockerfile getImage(String warFileLocation, String version) {
        String fromImage = defineFromImageName("openliberty/open-liberty", version, "22.0.0.6-full-java11-openj9-ubi");
        String dockerFileContext = defineDockerfileContent(fromImage);

        try {
            // Temporary directory where we assemble all required files to build the custom image
            Path tempDirWithPrefix = Files.createTempDirectory("atbash.test.");

            // Create the Dockerfile
            Path dockerPath = saveDockerFile(dockerFileContext, tempDirWithPrefix);

            // Copy the WAR File
            String name = copyWARFile(warFileLocation, tempDirWithPrefix);

            // Copy the server.xml file
            File configFile = new File("src/main/liberty/config/server.xml");
            Files.copy(configFile.toPath(), tempDirWithPrefix.resolve("server.xml"));

            return new ImageFromDockerfile("atbash-openliberty/" + name)
                    .withDockerfile(dockerPath);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return null;
    }

    private String defineDockerfileContent(String fromVersion) {

        return "FROM " + fromVersion + "\n" +
                "EXPOSE 5005 \n" +
                "ADD server.xml /config/server.xml \n" +
                "RUN configure.sh \n" +
                "ADD test.war /config/apps \n";
    }
}
