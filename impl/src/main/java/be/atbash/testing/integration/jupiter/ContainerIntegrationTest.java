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

import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(ContainerIntegrationTestExtension.class)
public @interface ContainerIntegrationTest {

    /**
     * When debug activated, the JVM option to start in debug mode (with suspended=y) is added and the timeout is increased to 120 seconds.
     * This gives the developer the time to connect a remote debugger to the Container process.
     * @return true when JVM needs to be started in debug mode.
     */
    boolean debug() default false;

    /**
     * When live logging (default is false) is activated, the server.log is send to the test run output.
     * @return true when live logging needs to be activated.
     */
    boolean liveLogging() default false;

    /**
     * Defines a volume mapping between the host and the container in read-write mode. The array most always contain
     * a multiple of 2 items. The first one is the directory on the host, the second in the container.
     * An exception on the multiple of 2 is when there is only 1 but is an empty String
     * @return Mapping pairs for the volume mapping.
     */
    String[] volumeMapping() default "";
}
