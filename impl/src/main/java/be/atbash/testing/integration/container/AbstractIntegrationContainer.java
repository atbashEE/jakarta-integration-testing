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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.utility.DockerImageName;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.Future;

/**
 * Abstract Super class for the Containers of the supported Runtimes. No real common functionality for the moment,
 * just a possibility to hold a supported custom container.
 *
 * @param <SELF>
 */
public abstract class AbstractIntegrationContainer<SELF extends AbstractIntegrationContainer<SELF>> extends GenericContainer<SELF> {

    protected final Logger LOGGER = LoggerFactory.getLogger(getClass());

    private final boolean liveLogging;

    protected AbstractIntegrationContainer(Future<String> image, boolean liveLogging) {
        super(image);
        this.liveLogging = liveLogging;
        setNetwork(Network.SHARED);
    }

    protected AbstractIntegrationContainer(DockerImageName dockerImageName) {
        // FIXME Is this constructor still needed. Since child constructors aren't used.
        super(dockerImageName);
        this.liveLogging = false;
        setNetwork(Network.SHARED);
    }

    protected void prepareForRemoteDebug(boolean debug) {
        if (debug) {
            addFixedExposedPort(5005, 5005);
            withEnv("JVM_ARGS", "-agentlib:jdwp=transport=dt_socket,server=y,suspend=y,address=*:5005");
            withStartupTimeout(Duration.of(60, ChronoUnit.SECONDS));
        }
    }

    @Override
    protected void doStart() {
        super.doStart();
        if (liveLogging) {
            Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
            followOutput(logConsumer);  // Show log of container in output.
        }

    }
}
