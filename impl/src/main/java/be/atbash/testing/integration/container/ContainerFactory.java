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

import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;

/**
 * Factory method that create a specific container for the runtime that is requested by the user.
 */
public class ContainerFactory {

    public AbstractIntegrationContainer<?> createContainer(ContainerAdapterMetaData metaData) {
        AbstractIntegrationContainer<?> result;
        switch (metaData.getSupportedRuntime()) {

            case PAYARA_MICRO:
                result = new PayaraMicroContainer(metaData);
                break;
            case OPEN_LIBERTY:
                result = new OpenLibertyContainer(metaData);
                break;
            case WILDFLY:
                result = new WildflyContainer(metaData);
                break;
            case GLASSFISH:
                result = new GlassfishContainer(metaData);
                break;
            default:
                throw new IllegalArgumentException(String.format("Unsupported value %s of SupportedRuntime", metaData.getSupportedRuntime()));
        }
        return result;
    }
}
