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

import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;
import org.testcontainers.utility.MountableFile;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Specialised Container for Wildfly.
 */
public class WildflyContainer extends AbstractIntegrationContainer<WildflyContainer> {

    public WildflyContainer(String warFileLocation, boolean debug) {
        super(DockerImageName.parse("quay.io/wildfly/wildfly:26.1.1.Final"));
        withExposedPorts(8080, 9990); // FIXME Reuse logic from ContainerAdapterMetaData.determinePort and/or kept within ContainerAdapterMetaData
        // port 9990 for the management where health is

        withCopyToContainer(MountableFile.forHostPath(warFileLocation, 0777), "/opt/jboss/wildfly/standalone/deployments/app.war");
        // Health point
        //waitingFor(Wait.forHttp("/health").forPort(9990));  // FIXME Test out why this isn't working
        waitingFor(Wait.forLogMessage(".*WFLYSRV0010: Deployed \"app.war\".*", 1));

        // FIXME duplicated with the OpenLiberty Container.
        if (debug) {
            addFixedExposedPort(5005, 5005);
            withEnv("JVM_ARGS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
            withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
        }
    }
}
