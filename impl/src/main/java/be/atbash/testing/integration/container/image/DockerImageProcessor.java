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
package be.atbash.testing.integration.container.image;

import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import org.testcontainers.images.builder.ImageFromDockerfile;

/**
 * A Helper class that generates the Dockerfile and dependencies within a temp directory.
 * Is required for OpenLiberty but also used for the other runtimes as MountableFile with .withCopyToContainer() and
 * TestExecutionExceptionHandler don't go well together (Broken pipe when previous run failed)
 */
public class DockerImageProcessor {
    public static ImageFromDockerfile getImage(SupportedRuntime supportedRuntime, ContainerAdapterMetaData metaData, TestContext testContext) {
        String version = System.getProperty("be.atbash.test.runtime.version", "");
        DockerImageProducer producer = retrieveProducer(supportedRuntime);

        return producer.getImage(metaData, version, testContext);
    }

    private static DockerImageProducer retrieveProducer(SupportedRuntime supportedRuntime) {
        DockerImageProducer result;
        switch (supportedRuntime) {

            case PAYARA_MICRO:
                result = new PayaraMicroDockerImageProducer();
                break;
            case OPEN_LIBERTY:
                result = new OpenLibertyDockerImageProducer();
                break;
            case WILDFLY:
                result = new WildFlyDockerImageProducer();
                break;
            case GLASSFISH:
                result = new GlassfishDockerImageProducer();
                break;
            default:
                throw new IllegalArgumentException(String.format("Unknown Supported runtime %s", supportedRuntime));
        }
        return result;
    }

}
