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
package be.atbash.testing.integration.database;

import be.atbash.testing.integration.database.connection.IDatabaseConnectionProvider;
import be.atbash.testing.integration.database.connection.MySQLConnectionProvider;
import be.atbash.testing.integration.database.connection.OracleConnectionProvider;
import be.atbash.testing.integration.database.connection.PostgresConnectionProvider;

public enum SupportedDatabase {

    MYSQL("org.testcontainers.containers.MySQLContainer", "mysql:5.7.34", "jdbc:mysql://%s:%d/test?useSSL=false", new MySQLConnectionProvider(), "com.mysql.jdbc")
    , POSTGRES("org.testcontainers.containers.PostgreSQLContainer", "postgres:9.6.12", "jdbc:postgresql://%s:%d/test", new PostgresConnectionProvider(), "org.postgresql")
    , MARIADB("org.testcontainers.containers.MariaDBContainer", "mariadb:10.3.6", "jdbc:mariadb://%s:%d/test", new MySQLConnectionProvider(), "org.mariadb.jdbc")  // We use the MySQL one here
    , ORACLE_XE("org.testcontainers.containers.OracleContainer", "gvenzl/oracle-xe:18.4.0-slim", "jdbc:oracle:thin:@//%s:%d", new OracleConnectionProvider(), "oracle.jdbc");

    private final String className;
    private final String dockerImageName;
    private final String jdbcURLTemplate;

    private final IDatabaseConnectionProvider connectionSupplier;

    private final String driverPackageName;

    SupportedDatabase(String className, String dockerImageName, String jdbcURLTemplate, IDatabaseConnectionProvider connectionSupplier, String driverPackageName) {

        this.className = className;
        this.dockerImageName = dockerImageName;
        this.jdbcURLTemplate = jdbcURLTemplate;
        this.connectionSupplier = connectionSupplier;
        this.driverPackageName = driverPackageName;
    }

    public String getClassName() {
        return className;
    }

    public String getDockerImageName() {
        return dockerImageName;
    }

    public String getJdbcURLTemplate() {
        return jdbcURLTemplate;
    }

    public IDatabaseConnectionProvider getConnectionSupplier() {
        return connectionSupplier;
    }

    public String getDriverPackageName() {
        return driverPackageName;
    }
}
