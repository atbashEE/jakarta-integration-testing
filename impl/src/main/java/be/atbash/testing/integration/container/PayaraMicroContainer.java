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
 * Specialised Container for Payara Micro.
 *
 */
public class PayaraMicroContainer extends AbstractIntegrationContainer<PayaraMicroContainer> {

    public PayaraMicroContainer(String warFileLocation, boolean debug) {
        super(DockerImageName.parse("payara/micro:5.2022.2-jdk11"));
        withExposedPorts(8080); // FIXME Reuse logic from ContainerAdapterMetaData.determinePort and/or kept within ContainerAdapterMetaData

        withCopyFileToContainer(MountableFile.forHostPath(warFileLocation, 0777), "/opt/payara/deployments/app.war");
        // Health point of Payara Micro based on MicroProfile Health
        waitingFor(Wait.forHttp("/health"));
        withCommand("--deploy /opt/payara/deployments/app.war --noCluster --contextRoot /");
        // Deploy app, no clustering = faster and define context root.

        // FIXME duplicated with the OpenLiberty Container.
        if (debug) {
            addFixedExposedPort(5005, 5005);
            withEnv("JVM_ARGS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
            withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
        }
    }
}
