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
package be.atbash.testing.integration.jupiter;

import be.atbash.testing.integration.test.AbstractContainerIntegrationTest;

public enum SupportedRuntime {

    PAYARA_MICRO, OPEN_LIBERTY, WILDFLY;

    public static SupportedRuntime determineRuntime(Class<? extends AbstractContainerIntegrationTest> clazz) {
        SupportedRuntime result = null;
        if (clazz.getSimpleName().startsWith("PayaraMicro")) {
            result = PAYARA_MICRO;
        }
        if (clazz.getSimpleName().startsWith("OpenLiberty")) {
            result = OPEN_LIBERTY;
        }
        if (clazz.getSimpleName().startsWith("Wildfly")) {
            result = WILDFLY;
        }
        return result;
    }
}
