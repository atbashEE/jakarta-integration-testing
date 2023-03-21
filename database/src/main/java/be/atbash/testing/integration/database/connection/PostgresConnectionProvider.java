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
package be.atbash.testing.integration.database.connection;

import be.atbash.testing.integration.container.exception.UnexpectedException;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.DatabaseConnection;
import org.testcontainers.containers.JdbcDatabaseContainer;

import java.sql.SQLException;

public class PostgresConnectionProvider implements IDatabaseConnectionProvider {
    @Override
    public DatabaseConnection create(JdbcDatabaseContainer<?> jdbcDatabaseContainer) {
        try {
            // empty String means we don't pass any additional parameters to the creation of the connection
            return new PostgresqlConnection(jdbcDatabaseContainer.createConnection(""), jdbcDatabaseContainer.getDatabaseName());
        } catch (DatabaseUnitException | SQLException e) {
            throw new UnexpectedException("Error occurred during creation connection to database within Container", e);
        }
    }
}
