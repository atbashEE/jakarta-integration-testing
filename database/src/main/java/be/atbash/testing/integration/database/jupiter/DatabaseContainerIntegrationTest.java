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
package be.atbash.testing.integration.database.jupiter;

import be.atbash.testing.integration.jupiter.ContainerIntegrationTest;
import org.junit.jupiter.api.extension.ExtendWith;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
@ExtendWith(DatabaseContainerIntegrationTestExtension.class)
public @interface DatabaseContainerIntegrationTest {

    ContainerIntegrationTest containerIntegrationTest();

    /**
     * A specific tag of the docker database image. When empty value, it uses the default tag name.
     * @return tag to be used for the docker database image.
     */
    String databaseContainerImageName() default "";

    /**
     * Is the database container started in parallel with the other containers or upfront.
     * @return default value tru for start in parallel.
     */
    boolean databaseContainerStartInParallel() default true;

    EnvironmentParametersForDatabase environmentParametersForDatabase() default @EnvironmentParametersForDatabase;

    DatabaseScriptFiles databaseScriptFiles() default @DatabaseScriptFiles;

    String jndiDatasourceName() default "java:jboss/datasources/defaultDataSource"; // Only for WildFly
}
