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
package be.atbash.test.integration.example.hello;

import be.atbash.testing.integration.container.image.CustomBuildFile;
import be.atbash.testing.integration.jupiter.ContainerIntegrationTest;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;
import org.assertj.core.api.Assertions;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;


@ContainerIntegrationTest(runtime = SupportedRuntime.PAYARA_MICRO)
@CustomBuildFile(location = "custom/payara")
public class CustomPayaraIT extends AbstractContainerIntegrationTest {
    // Should be public and not the JUnit 5 preferred scope package.

    @RestClient
    public CustomService customService;

    @Test
    void getFileContent() {
        String value = customService.getFileContent();
        Assertions.assertThat(value).isEqualTo("Payara Micro content file");
    }
}