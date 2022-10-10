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

import be.atbash.testing.integration.container.exception.FileReadingException;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.stream.Stream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class DockerImageProducer {

    public static final String DOCKERFILE = "Dockerfile";

    /**
     * Create a Docker Image. A temporary directory s created with all content required. When a location is specificaed,
     * the Dockerfile is loaded from that location if it is found. If not, a default DockerFile is created
     * according the supported Runtime.  Some additional statements are added if a Docker file is supplied in
     * the location.
     *
     * @param warFileLocation The location of the war file.
     * @param version         The version if a default DockerFile is created
     * @param location        an optional location for additional files and a supplied DockerFile.
     * @return The DockerFile that will be used within the test.
     */
    abstract ImageFromDockerfile getImage(String warFileLocation, String version, String location);

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
        Path dockerPath = tempDirWithPrefix.resolve(DOCKERFILE);
        try (BufferedWriter writer = Files.newBufferedWriter(dockerPath)) {
            writer.write(dockerFileContext);
        }
        return dockerPath;
    }

    protected String loadOptionalDockerFile(String location) {

        Path path = Path.of(location, DOCKERFILE);
        if (path.toFile().exists()) {

            try {
                return Files.readString(path);
            } catch (IOException e) {
                throw new FileReadingException(path.toFile().getAbsolutePath(), e);
            }
        }

        return null;
    }

    protected void copyLocationContentToTempFile(String source, Path tempDir) {
        try {
            try (Stream<Path> pathStream = Files.find(Path.of(source),
                    Integer.MAX_VALUE,
                    (filePath, fileAttr) -> fileAttr.isRegularFile()
                            && !filePath.getFileName().endsWith("Dockerfile"))) {
                // For each of the found files, copy it to the temporary directory.
                pathStream.forEach(p -> copyToTemp(source, p, tempDir));
            }
        } catch (IOException e) {
            // FIXME Show the file with the problem
            throw new FileReadingException(source, e);
        }

    }

    private void copyToTemp(String startPath, Path path, Path tempDirWithPrefix) {

        Path targetPath = determineTargetPath(startPath, path, tempDirWithPrefix);
        File parentDirectory = targetPath.getParent().toFile();
        if (!parentDirectory.exists()) {
            boolean success = parentDirectory.mkdirs();
            if (!success) {
                Assertions.fail(String.format("Unable to create directory %s", parentDirectory));
            }
        }
        try {
            Files.copy(path, targetPath, REPLACE_EXISTING);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
    }

    private Path determineTargetPath(String startPath, Path path, Path tempDirWithPrefix) {
        int length = startPath.length();
        String endPath = path.toString().substring(length);

        return tempDirWithPrefix.resolve(endPath);
    }

}
