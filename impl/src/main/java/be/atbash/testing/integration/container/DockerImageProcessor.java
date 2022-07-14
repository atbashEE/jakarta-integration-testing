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

import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Assertions;
import org.testcontainers.images.builder.ImageFromDockerfile;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * A Helper class that generates the Dockerfile and dependencies within a temp directory.
 * Is required for OpenLiberty but also used for the other runtimes as MountableFile with .withCopyToContainer() and
 * TestExecutionExceptionHandler don't go well together (Broken pipe when previous run failed)

 * TODO Allow to provide the version  container.
 * TODO Refactor with Factory and specific implementation classes for the 3 runtimes
 */
public class DockerImageProcessor {
    public static ImageFromDockerfile getImage(SupportedRuntime supportedRuntime, String warFileLocation) {
        ImageFromDockerfile result;
        switch (supportedRuntime) {

            case PAYARA_MICRO:
                result = getPayaraMicroImage(warFileLocation);
                break;
            case OPEN_LIBERTY:
                result = getOpenLibertyImage(warFileLocation);
                break;
            case WILDFLY:
                result = getWildflyImage(warFileLocation);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown Supported runtime %s", supportedRuntime));
        }
        return result;
    }

    private static ImageFromDockerfile getPayaraMicroImage(String warFileLocation) {
        String dockerFileContext = definePayaraMicroDockerfileContent("payara/micro:5.2022.2-jdk11");

        try {
            // Temporary directory where we assemble all required files to build the custom image
            Path tempDirWithPrefix = Files.createTempDirectory("atbash.test.");

            // Create the Dockerfile
            Path dockerPath = saveDockerFile(dockerFileContext, tempDirWithPrefix);

            // Copy the WAR File
            String name = copyWARFile(warFileLocation, tempDirWithPrefix);

            return new ImageFromDockerfile("atbash-payara/" + name)
                    .withDockerfile(dockerPath);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return null;
    }

    private static ImageFromDockerfile getOpenLibertyImage(String warFileLocation) {
        String dockerFileContext = defineOpenLibertyDockerfileContent("openliberty/open-liberty:22.0.0.6-full-java11-openj9-ubi");

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

    private static ImageFromDockerfile getWildflyImage(String warFileLocation) {
        String dockerFileContext = defineWildflyDockerfileContent("quay.io/wildfly/wildfly:26.1.1.Final");

        try {
            // Temporary directory where we assemble all required files to build the custom image
            Path tempDirWithPrefix = Files.createTempDirectory("atbash.test.");

            // Create the Dockerfile
            Path dockerPath = saveDockerFile(dockerFileContext, tempDirWithPrefix);

            // Copy the WAR File
            String name = copyWARFile(warFileLocation, tempDirWithPrefix);

            return new ImageFromDockerfile("atbash-wildfly/" + name)
                    .withDockerfile(dockerPath);
        } catch (IOException e) {
            Assertions.fail(e.getMessage());
        }
        return null;
    }

    @NotNull
    private static String copyWARFile(String warFileLocation, Path tempDirWithPrefix) throws IOException {
        File warFile = new File(warFileLocation);
        String name = warFile.getName();

        Path source = warFile.toPath();
        Files.copy(source, tempDirWithPrefix.resolve("test.war"));
        return name;
    }

    @NotNull
    private static Path saveDockerFile(String dockerFileContext, Path tempDirWithPrefix) throws IOException {
        Path dockerPath = tempDirWithPrefix.resolve("Dockerfile");
        try (BufferedWriter writer = Files.newBufferedWriter(dockerPath)) {
            writer.write(dockerFileContext);
        }
        return dockerPath;
    }

    private static String defineOpenLibertyDockerfileContent(String fromVersion) {

        return "FROM " + fromVersion + "\n" +
                "EXPOSE 5005 \n" +
                "ADD server.xml /config/server.xml \n" +
                "RUN configure.sh \n" +
                "ADD test.war /config/apps \n";
    }

    private static String definePayaraMicroDockerfileContent(String fromVersion) {

        return "FROM " + fromVersion + "\n" +
                "CMD [\"--deploy\", \"/opt/payara/deployments/test.war\", \"--noCluster\",  \"--contextRoot\", \"/\"]\n" +
                "ADD test.war /opt/payara/deployments \n";
    }

    private static String defineWildflyDockerfileContent(String fromVersion) {

        return "FROM " + fromVersion + "\n" +
                "ADD test.war //opt/jboss/wildfly/standalone/deployments \n";
    }


}
