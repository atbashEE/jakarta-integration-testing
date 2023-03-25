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
package be.atbash.testing.integration.container;

import be.atbash.testing.integration.container.image.DockerImageProcessor;
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

/**
 * Specialised Container for Wildfly.
 */
public class GlassfishContainer extends AbstractIntegrationContainer<GlassfishContainer> {

    public GlassfishContainer(ContainerAdapterMetaData metaData, TestContext testContext) {
        super(DockerImageProcessor.getImage(SupportedRuntime.GLASSFISH, metaData, testContext), metaData.isLiveLogging());
        withExposedPorts(metaData.getPort());

        // Check if application is deployed
        waitingFor(Wait.forLogMessage(".*_MessageID=NCLS-DEPLOYMENT-02035.*", 1));

        if (metaData.isDebug()) {
            throw new UnsupportedOperationException("Debug is not supported with Glassfish");
        }
    }

    public GlassfishContainer(DockerImageName dockerImageName) {
        super(dockerImageName);

        withExposedPorts(ContainerAdapterMetaData.determinePort(SupportedRuntime.WILDFLY));

        // Check if application is deployed
        waitingFor(Wait.forLogMessage(".*_MessageID=NCLS-DEPLOYMENT-02035.*", 1));

    }
}
