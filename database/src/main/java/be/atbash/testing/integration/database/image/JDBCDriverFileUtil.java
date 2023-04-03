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
package be.atbash.testing.integration.database.image;

import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.database.SupportedDatabase;
import be.atbash.testing.integration.database.exception.DriverNotFoundException;
import be.atbash.testing.integration.database.jupiter.DatabaseContainerAdapterMetaData;

import java.security.CodeSource;
import java.security.ProtectionDomain;
import java.sql.Driver;
import java.util.ServiceLoader;

public final class JDBCDriverFileUtil {

    private JDBCDriverFileUtil() {
    }

    public static String getDriverFile(TestContext testContext) {
        DatabaseContainerAdapterMetaData databaseMetaData = testContext.getInstance(DatabaseContainerAdapterMetaData.class);

        SupportedDatabase database = databaseMetaData.getDatabase();

        return resolveDriverFile(database);
    }

    private static String resolveDriverFile(SupportedDatabase database) {
        Driver driver = getDriverFromClasspath(database);
        ProtectionDomain protectionDomain = driver.getClass().getProtectionDomain();
        CodeSource codeSource = protectionDomain.getCodeSource();
        return codeSource.getLocation().getPath();
    }

    private static Driver getDriverFromClasspath(SupportedDatabase database) {
        return ServiceLoader.load(Driver.class)
                .stream().map(ServiceLoader.Provider::get)
                .filter(d -> d.getClass().getName().startsWith(database.getDriverPackageName()))
                .findAny().orElseThrow(() ->
                        new DriverNotFoundException(database)
                );
    }
}