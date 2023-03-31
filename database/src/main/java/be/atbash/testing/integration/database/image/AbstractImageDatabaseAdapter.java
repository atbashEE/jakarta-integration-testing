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

import be.atbash.testing.integration.container.exception.UnexpectedException;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static java.nio.file.StandardCopyOption.REPLACE_EXISTING;

public abstract class AbstractImageDatabaseAdapter {

    protected List<String> updateBuildFileForDriver(List<String> dockerFileLines, String imageLocationForDriver, String fileName) {
        int line = findLine(dockerFileLines, "ADD test.war");  // This is a marker
        List<String> result = new ArrayList<>(dockerFileLines);
        // add a new line before the 'ADD test.war'
        result.add(line, String.format("ADD %s %s", fileName, imageLocationForDriver));
        return result;
    }

    /**
     * Find the line in the Docker build script that contains a certain text
     *
     * @param dockerFileLines All lines of the Docker build file
     * @param dataOnLine      Text to be found.
     * @return line number (0 based) or -1 when not found.
     */
    protected int findLine(List<String> dockerFileLines, String dataOnLine) {
        return IntStream.range(0, dockerFileLines.size())
                .filter(i -> dockerFileLines.get(i).contains(dataOnLine))
                .findFirst()
                .orElse(-1);
    }

    protected String copyJDBCDriver(TestContext testContext) {
        // Get the JDBC Driver JAR file on the machine.
        Path driverFile = Path.of(JDBCDriverFileUtil.getDriverFile(testContext));
        // Get filename, without path.
        String fileName = driverFile.getFileName().toString();
        ContainerAdapterMetaData containerMetaData = testContext.getInstance(ContainerAdapterMetaData.class);
        // Copy the JAR file to the temp directory where we assemble Docker build resources/
        Path tempDir = containerMetaData.getTempDir();
        try {
            Files.copy(driverFile, tempDir.resolve(fileName), REPLACE_EXISTING);
        } catch (IOException e) {
            throw new UnexpectedException("Unexpected Exception during copy of JDBC Driver to Temp directory", e);
        }
        return fileName;
    }

}
