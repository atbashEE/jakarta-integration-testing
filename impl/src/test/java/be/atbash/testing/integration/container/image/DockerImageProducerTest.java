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
import org.assertj.core.api.Assertions;
import org.jetbrains.annotations.NotNull;
import org.junit.jupiter.api.Test;
import org.testcontainers.images.builder.ImageFromDockerfile;

class DockerImageProducerTest {

    @Test
    void postProcessDockerFileContent_Payara() {
        DockerImageProducer producer = createProducer();
        String content = producer.postProcessDockerFileContent("Main Content", SupportedRuntime.PAYARA_MICRO, new TestContext());
        Assertions.assertThat(content).isEqualTo("Main Content\n" +
                "Payara Adapter\n" +
                "Generic Adapter");
    }

    @Test
    void postProcessDockerFileContent_Default() {
        DockerImageProducer producer = createProducer();
        String content = producer.postProcessDockerFileContent("Main Content", SupportedRuntime.OPEN_LIBERTY, new TestContext());
        Assertions.assertThat(content).isEqualTo("Main Content\n" +
                "Generic Adapter");
    }

    @NotNull
    private static DockerImageProducer createProducer() {
        return new DockerImageProducer() {
            @Override
            ImageFromDockerfile getImage(ContainerAdapterMetaData metaData, String version, TestContext testContext) {
                return null;
            }
        };
    }
}