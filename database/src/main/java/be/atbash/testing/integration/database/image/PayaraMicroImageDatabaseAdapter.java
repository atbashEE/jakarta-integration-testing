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

import be.atbash.testing.integration.container.image.DockerImageAdapter;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.jupiter.SupportedRuntime;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class PayaraMicroImageDatabaseAdapter extends AbstractImageDatabaseAdapter implements DockerImageAdapter {
    @Override
    public String adapt(String dockerFileContent, TestContext testContext) {
        String fileName = copyJDBCDriver(testContext);
        List<String> dockerFileLines = Arrays.asList(dockerFileContent.split("\n"));

        dockerFileLines = updateBuildFileForDriver(dockerFileLines, "/opt/payara", fileName);

        int cmdLine = findLine(dockerFileLines, "--deploy");
        List<String> cmdParameters = getCmdParameters(dockerFileLines.get(cmdLine));
        cmdParameters.add("\"--addlibs\"");
        cmdParameters.add(String.format("\"/opt/payara/%s\"", fileName));
        dockerFileLines.set(cmdLine, assembleCMDCommand(cmdParameters));

        return String.join("\n", dockerFileLines);
    }

    private String assembleCMDCommand(List<String> cmdParameters) {
        return "CMD [" + String.join(", ", cmdParameters) + "]";
    }

    private List<String> getCmdParameters(String line) {
        int startIdx = line.indexOf("[");
        int endIdx = line.indexOf("]");
        return new ArrayList<>(Arrays.asList(line.substring(startIdx + 1, endIdx).split(",")));
    }

    @Override
    public SupportedRuntime supportedRuntime() {
        return SupportedRuntime.PAYARA_MICRO;
    }
}
