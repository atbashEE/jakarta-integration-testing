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
package be.atbash.testing.integration.test;


import org.junit.jupiter.api.extension.RegisterExtension;
import org.testcontainers.containers.GenericContainer;

/**
 * Abstract class for the Integration test written by developer.
 */
public abstract class AbstractContainerIntegrationTest {

    /*
     * FIXME This is not used as we set the container directly? So do we remove this
     *  or change the logic of assigning the container instance in the test?
     */
    abstract void setContainer(GenericContainer<?> container);

    @RegisterExtension
    private final ShowLogWhenFailedExceptionHandler showLogExceptionHandler = new ShowLogWhenFailedExceptionHandler();

}
