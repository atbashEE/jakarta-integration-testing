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
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.testcontainers.containers.wait.strategy.Wait;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

/**
 * Specialised Container for Payara Micro.
 */
public class PayaraMicroContainer extends AbstractIntegrationContainer<PayaraMicroContainer> {

    public PayaraMicroContainer(String warFileLocation, boolean debug) {
        super(DockerImageProcessor.getImage(SupportedRuntime.PAYARA_MICRO, warFileLocation));
        withExposedPorts(8080); // FIXME Reuse logic from ContainerAdapterMetaData.determinePort and/or kept within ContainerAdapterMetaData

        // Health point of Payara Micro based on MicroProfile Health
        waitingFor(Wait.forHttp("/health"));

        // FIXME duplicated with the OpenLiberty Container.
        if (debug) {
            addFixedExposedPort(5005, 5005);
            withEnv("JVM_ARGS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
            withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
        }
    }
}
