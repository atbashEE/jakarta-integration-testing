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
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Specialised Generic Container for OpenLiberty.
 */
public class OpenLibertyContainer extends AbstractIntegrationContainer<OpenLibertyContainer> {

    public OpenLibertyContainer(ContainerAdapterMetaData metaData) {

        super(DockerImageProcessor.getImage(SupportedRuntime.OPEN_LIBERTY, metaData.getWarFileLocation(), metaData.getCustomBuildDirectory()));
        withExposedPorts(metaData.getPort());

        // Health point of Payara Micro based on MicroProfile Health
        waitingFor(Wait.forHttp("/health"));

        prepareForRemoteDebug(metaData.isDebug());
    }

    public OpenLibertyContainer(DockerImageName dockerImageName) {
        super(dockerImageName);

        withExposedPorts(ContainerAdapterMetaData.determinePort(SupportedRuntime.OPEN_LIBERTY));

        // Health point of Payara Micro based on MicroProfile Health
        waitingFor(Wait.forHttp("/health"));

    }
}
