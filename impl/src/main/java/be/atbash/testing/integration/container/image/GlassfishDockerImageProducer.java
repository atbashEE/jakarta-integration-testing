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

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class GlassfishDockerImageProducer extends DockerImageProducer {

    @Override
    public ImageFromDockerfile getImage(String warFileLocation, String version, String location) {
        String fromImage = defineFromImageName("airhacks/glassfish", version, "5.1.0");
        String dockerFileContext = defineDockerfileContent(fromImage, location);

        try {
            // Temporary directory where we assemble all required files to build the custom image
            Path tempDirWithPrefix = Files.createTempDirectory("atbash.test.");

            if (location != null) {
                copyLocationContentToTempFile(location, tempDirWithPrefix);
            }

            // Create the Dockerfile
            Path dockerPath = saveDockerFile(dockerFileContext, tempDirWithPrefix);

            // Copy the WAR File
            String name = copyWARFile(warFileLocation, tempDirWithPrefix);

            return new ImageFromDockerfile("atbash-glassfish/" + name)
                    .withDockerfile(dockerPath);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return null;
    }

    private String defineDockerfileContent(String fromVersion, String location) {
        String content = loadOptionalDockerFile(location);

        if (content == null) {
            // Default content for DockerFile
            content = "FROM " + fromVersion;
        }
        return content + "\n" +
                "ADD test.war ${DEPLOYMENT_DIR} \n";
    }

}
