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

import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public abstract class DockerImageProducer {

    abstract ImageFromDockerfile getImage(String warFileLocation, String version);

    protected String defineFromImageName(String imageName, String suppliedVersion, String defaultTagName) {
        if (suppliedVersion.contains("/") || suppliedVersion.contains(":")) {
            // It looks like the user specified a imageName with version number, so use that one.
            return suppliedVersion;
        }
        if (suppliedVersion.isBlank()) {
            // We use the default as user did not specify version/tag name
            return imageName + ":" + defaultTagName;
        }
        // Use the version/tag name specified by the user
        return imageName + ":" + suppliedVersion;
    }

    protected String copyWARFile(String warFileLocation, Path tempDirWithPrefix) throws IOException {
        File warFile = new File(warFileLocation);
        String name = warFile.getName();

        Path source = warFile.toPath();
        Files.copy(source, tempDirWithPrefix.resolve("test.war"));
        return name;
    }

    protected Path saveDockerFile(String dockerFileContext, Path tempDirWithPrefix) throws IOException {
        Path dockerPath = tempDirWithPrefix.resolve("Dockerfile");
        try (BufferedWriter writer = Files.newBufferedWriter(dockerPath)) {
            writer.write(dockerFileContext);
        }
        return dockerPath;
    }
}
