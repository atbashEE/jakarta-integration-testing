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
package be.atbash.testing.integration.jupiter;

import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Assertions;
import org.junit.platform.commons.support.AnnotationSupport;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * Data defined by user or discovered on project about the container.
 */
public class ContainerAdapterMetaData {

    private static final Logger LOGGER = LoggerFactory.getLogger(ContainerIntegrationTestExtension.class);

    private SupportedRuntime supportedRuntime;

    private int port;
    private String warFileLocation;
    private boolean debug;
    private boolean liveLogging;

    private Map<String, String> volumeMapping;

    private List<Field> restClientFields;

    private ContainerAdapterMetaData() {
    }

    public SupportedRuntime getSupportedRuntime() {
        return supportedRuntime;
    }

    public int getPort() {
        return port;
    }

    public String getWarFileLocation() {
        return warFileLocation;
    }

    public boolean isDebug() {
        return debug;
    }

    public boolean isLiveLogging() {
        return liveLogging;
    }

    public Map<String, String> getVolumeMapping() {
        return volumeMapping;
    }

    public List<Field> getRestClientFields() {
        return restClientFields;
    }

    public static ContainerAdapterMetaData create(Class<?> testClass) {
        ContainerAdapterMetaData result = new ContainerAdapterMetaData();

        ContainerIntegrationTest containerIntegrationTest = testClass.getAnnotation(ContainerIntegrationTest.class);
        result.debug = containerIntegrationTest.debug();
        result.liveLogging = containerIntegrationTest.liveLogging();


        result.supportedRuntime = determineRuntime(containerIntegrationTest.runtime());
        result.port = determinePort(result.supportedRuntime);
        result.warFileLocation = findAppFile().getAbsolutePath();
        result.volumeMapping = defineVolumeMappings(containerIntegrationTest.volumeMapping());

        result.restClientFields = AnnotationSupport.findAnnotatedFields(testClass, RestClient.class);

        return result;
    }

    private static int determinePort(SupportedRuntime supportedRuntime) {
        int port = 0;
        switch (supportedRuntime) {

            case PAYARA_MICRO:
            case WILDFLY:
            case GLASSFISH:
                port = 8080;
                break;
            case OPEN_LIBERTY:
                port = 9080;
                break;
        }
        return port;
    }

    private static SupportedRuntime determineRuntime(SupportedRuntime runtime) {

        SupportedRuntime result = runtime;
        if (result == SupportedRuntime.DEFAULT) {
            result = SupportedRuntime.valueFor(System.getProperty("be.atbash.test.runtime", "DEFAULT"));
        }
        if (result == SupportedRuntime.DEFAULT) {
            Assertions.fail("The runtime could not be determined from the annotation or the System property");
        }
        return result;
    }

    private static File findAppFile() {

        // Find a .war file in the target/ directories
        Set<File> matches = new HashSet<>(findAppFiles("target"));
        if (matches.size() == 0) {
            throw new IllegalStateException("No .war files found in target / output folders.");
        }
        if (matches.size() > 1) {
            throw new IllegalStateException("Found multiple application files in target output folders: " + matches +
                    " Expecting exactly 1 application file to be found.");
        }
        File appFile = matches.iterator().next();
        LOGGER.info("Found application file at: " + appFile.getAbsolutePath());
        return appFile;
    }

    /**
     * Find all .war files in a directory and subdirectories.
     *
     * @param path The top level directory to start the search
     * @return The set of files found matching the file type.
     */
    private static Set<File> findAppFiles(String path) {
        File dir = new File(path);
        if (dir.exists() && dir.isDirectory()) {
            try {
                Set<File> result;
                try (Stream<Path> pathStream = Files.walk(dir.toPath())) {
                    result = pathStream
                            .filter(Files::isRegularFile)
                            .filter(p -> p.toString().toLowerCase().endsWith(".war"))
                            .map(Path::toFile)
                            .collect(Collectors.toSet());
                }
                return result;
                //https://docs.oracle.com/javase/8/docs/api/java/nio/file/Files.html#walk-java.nio.file.Path-java.nio.file.FileVisitOption...-
            } catch (IOException ignore) {
            }
        }
        return Collections.emptySet();
    }

    private static Map<String, String> defineVolumeMappings(String[] volumeMapping) {
        Map<String, String> result = new HashMap<>();
        if (volumeMapping.length == 1) {
            if (!volumeMapping[0].isBlank()) {
                Assertions.fail("volumeMapping must be pairs of directories");
            }
            return result;  // a single blank item is allowed and results in empty map.
        }
        if (volumeMapping.length % 2 == 1) {
            Assertions.fail("volumeMapping must be pairs of directories");
        } else {
            for (int i = 0; i < volumeMapping.length; i = i + 2) {
                result.put(makeAbsolute(volumeMapping[i]), volumeMapping[i + 1]);
            }
        }
        return result;
    }

    private static String makeAbsolute(String path) {
        return new File(path).getAbsolutePath();
    }
}
