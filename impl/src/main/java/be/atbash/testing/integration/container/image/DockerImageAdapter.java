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

import be.atbash.testing.integration.jupiter.SupportedRuntime;

/**
 * A 'Adapter' adjusts the Docker File content by adding for example additional logic
 * for database. There can be multiple adapters that modify the content
 * and they are ordered by defining @Priority on the class. An implementation can indicate
 * it will only operate on a DockerFile specific for a certain SupportedRuntime.
 * Instances are loaded through the Service Loader mechanism.
 */
public interface DockerImageAdapter {

    String adapt(String dockerFileContent, TestContext testContext);

    default SupportedRuntime supportedRuntime() {
        return SupportedRuntime.DEFAULT;
    }
}
