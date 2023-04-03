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

import be.atbash.testing.integration.ConfigurationException;
import be.atbash.testing.integration.database.SupportedDatabase;

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

public class DatabaseContainerAdapterMetaData {

    private DatabaseContainerIntegrationTest databaseContainerIntegrationTest;
    private SupportedDatabase database;

    private DatabaseContainerAdapterMetaData() {
    }

    public DatabaseContainerIntegrationTest getDatabaseContainerIntegrationTest() {
        return databaseContainerIntegrationTest;
    }

    public SupportedDatabase getDatabase() {
        return database;
    }

    public static DatabaseContainerAdapterMetaData create(DatabaseContainerIntegrationTest databaseContainerIntegrationTest) {
        DatabaseContainerAdapterMetaData result = new DatabaseContainerAdapterMetaData();
        result.databaseContainerIntegrationTest = databaseContainerIntegrationTest;
        result.database = determineDatabase();
        return result;
    }

    private static SupportedDatabase determineDatabase() {
        List<SupportedDatabase> foundDatabases = Arrays.stream(SupportedDatabase.values())
                .filter(sd -> checkClass(sd.getClassName()) != null)
                .collect(Collectors.toList());

        if (foundDatabases.size() != 1) {
            throw new ConfigurationException("None or multiple database containers found. Exactly one supported database container must be on classpath");
        }

        return foundDatabases.get(0);
    }

    private static Class<?> checkClass(String className) {
        Class<?> result = null;
        try {
            result = Class.forName(className);
        } catch (ClassNotFoundException e) {
            // Just a test, lets continue
        }
        return result;
    }

}
