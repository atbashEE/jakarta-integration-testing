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
public @interface JDBCDriverArtifact {

    /**
     * Maven artifact containing the JDBC Driver that needs to used by runtime. The format is
     * groupid:artifactid:version and it must be already available in the local repository.
     * @return default value defined by internal framework values.
     */
    String mavenArtifact() default "";

    /**
     * If defined, overrules the mavenArtifact, default value or user defined one, and points
     * to the JAR file containing the JDBC Driver that needs to be used for the runtime.
     * @return default value is blank and ignored.
     */
    String driverJarFile() default "";
}
