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

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.RUNTIME)
public @interface EnvironmentParametersForDatabase {

    /**
     * Name of the environment variable to receive the JDBC URL.
     *
     * @return default is {@code ds_url} or user defined name.
     */
    String jdbcURL() default "ds_url";

    /**
     * Name of the environment variable to receive the username for the database connection.
     *
     * @return default is {@code ds_username} or user defined name.
     */
    String username() default "ds_username";

    /**
     * Name of the environment variable to receive the password for the database connection.
     *
     * @return default is {@code ds_password} or user defined name.
     */
    String password() default "ds_password";
}
