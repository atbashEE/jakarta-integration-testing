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

import be.atbash.testing.integration.container.AbstractIntegrationContainer;
import be.atbash.testing.integration.container.exception.UnexpectedException;
import be.atbash.testing.integration.database.SupportedDatabase;
import be.atbash.testing.integration.database.exception.FileNotFoundException;
import be.atbash.testing.integration.jupiter.ContainerAdapterMetaData;
import be.atbash.testing.integration.jupiter.TestcontainersController;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.excel.XlsDataSet;
import org.dbunit.operation.DatabaseOperation;
import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.Network;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.ext.ScriptUtils;
import org.testcontainers.jdbc.JdbcDatabaseDelegate;

import javax.script.ScriptException;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URISyntaxException;
import java.net.URL;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

public class DatabaseTestcontainersController extends TestcontainersController {

    private final JdbcDatabaseContainer<?> jdbcDatabaseContainer;
    private final SupportedDatabase database;
    private final DatabaseContainerIntegrationTest containerIntegrationTest;

    private IDataSet dataSet;

    private IDatabaseConnection connection;

    private Field databaseConnectionField;
    private Field databaseContainerField;

    public DatabaseTestcontainersController(Class<?> testClass, JdbcDatabaseContainer<?> jdbcDatabaseContainer
            , SupportedDatabase database, DatabaseContainerIntegrationTest containerIntegrationTest) {
        super(testClass);
        this.jdbcDatabaseContainer = jdbcDatabaseContainer;
        this.database = database;
        this.containerIntegrationTest = containerIntegrationTest;
    }

    @Override
    public void config(ContainerAdapterMetaData metaData) {
        super.config(metaData);  // As it prepares the 'applicationTestContainer'
        String dbHostName = "db";

        jdbcDatabaseContainer.setNetwork(Network.SHARED);
        jdbcDatabaseContainer.withNetworkAliases(dbHostName);

        if (metaData.isLiveLogging()) {
            Slf4jLogConsumer logConsumer = new Slf4jLogConsumer(LOGGER);
            jdbcDatabaseContainer.followOutput(logConsumer);  // Show log of container in output.
        }

        // We define ourselves the JDBC URL as testContainers give the version to access it 'outside' the shared
        // network. But we need to URL to access it from 'inside' the network.
        String jdbcUrl = String.format(database.getJdbcURLTemplate(),
                dbHostName, jdbcDatabaseContainer.getExposedPorts().get(0));

        AbstractIntegrationContainer<?> applicationContainer = getApplicationTestContainer();
        applicationContainer.withEnv(containerIntegrationTest.environmentParametersForDatabase().jdbcURL(), jdbcUrl);
        applicationContainer.withEnv(containerIntegrationTest.environmentParametersForDatabase().username(), jdbcDatabaseContainer.getUsername());
        applicationContainer.withEnv(containerIntegrationTest.environmentParametersForDatabase().password(), jdbcDatabaseContainer.getPassword());

    }

    @Override
    public void start() {
        showContainerNames(jdbcDatabaseContainer.getImage().toString());
        long start = System.currentTimeMillis();

        CountDownLatch startSignal = new CountDownLatch(1);
        AtomicBoolean failure = new AtomicBoolean(false);
        try {
            manageStartDatabaseContainer(startSignal, failure);

            startContainers();
            try {
                boolean containerStarted = startSignal.await(1, TimeUnit.MINUTES);
                if (!containerStarted) {
                    throw new AssertionError("Test aborted since Database container does not start within a reasonable time");
                }
            } catch (InterruptedException e) {
                throw new UnexpectedException("Database Container start interrupted", e);
            }

            // We can only access the Database container after it is started.
            // But this start() s part of beforeAll and thus before PostProcessTestInstance needs it.
            connection = database.getConnectionSupplier().create(jdbcDatabaseContainer);

        } catch (Throwable e) {
            // The next statement handles the error.
        }
        if (failure.get()) {
            // Set by the callback in case of error.  This stops the execution of the test
            throw new AssertionError("Test aborted");
        }

        LOGGER.info("All containers started in " + (System.currentTimeMillis() - start) + "ms");
    }

    public void injectInstances(Object testInstance) {
        Class<?> testClass = testInstance.getClass();
        try {
            databaseConnectionField = testClass.getField("databaseConnection");
            databaseConnectionField.set(null, connection);

            databaseContainerField = testClass.getField("databaseContainer");
            databaseContainerField.set(null, jdbcDatabaseContainer);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    private void manageStartDatabaseContainer(CountDownLatch startSignal, AtomicBoolean failure) {
        FailureCallback callback = () -> failure.set(true);

        if (containerIntegrationTest.databaseContainerStartInParallel()) {
            Runnable task = () -> startAndPrepareDatabaseContainer(startSignal, callback);
            new Thread(task).start();
        } else {
            startAndPrepareDatabaseContainer(startSignal, callback);
        }
    }

    private void startAndPrepareDatabaseContainer(CountDownLatch startSignal, FailureCallback callback) {
        try {
            jdbcDatabaseContainer.start();

            JdbcDatabaseDelegate delegate = new JdbcDatabaseDelegate(jdbcDatabaseContainer, "");

            String createTables = containerIntegrationTest.databaseScriptFiles().createTables();
            String script = getClassPathFileContent(createTables);

            try {

                ScriptUtils.executeDatabaseScript(delegate, createTables, script);
            } catch (ScriptException e) {
                throw new RuntimeException(e);
            }

            String dataFile = containerIntegrationTest.databaseScriptFiles().initData();
            URL testDataFile = DatabaseTestcontainersController.class.getClassLoader().getResource(dataFile);
            if (testDataFile == null) {
                throw new FileNotFoundException(String.format("The file with name '%s' is not found on the class path", dataFile));
            }

            try {
                dataSet = new XlsDataSet(Paths.get(testDataFile.toURI()).toFile());
            } catch (IOException | DataSetException | URISyntaxException e) {
                throw new RuntimeException(e);
            }

        } catch (Throwable e) {
            // Report to the calling context that there was a problem.
            // Since this method can be executed in a Thread, throwing an Exception is not enough.
            callback.failed();
            throw new AssertionError("Test aborted due to :" + e.getMessage());
        } finally {
            // Always signal the method is finished.
            startSignal.countDown();  // Startup done
        }
    }

    public void uploadData() {
        try {

            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

        } catch (DatabaseUnitException | SQLException e) {
            throw new RuntimeException(e);
        }
    }

    private String getClassPathFileContent(String resourceName) {
        URL resource = DatabaseTestcontainersController.class.getClassLoader().getResource(resourceName);
        if (resource == null) {
            throw new FileNotFoundException(String.format("The file with name '%s' is not found on the class path", resourceName));
        }
        String script;
        try {
            script = Files.readString(
                    Paths.get(resource.toURI()), Charset.defaultCharset());
        } catch (IOException | URISyntaxException e) {
            throw new UnexpectedException(String.format("Unexpected Exception during reading of the the resource '%s'", resourceName), e);
        }
        return script;
    }

    @FunctionalInterface
    public interface FailureCallback {
        void failed();
    }

    public void clearData() {
        try {

            DatabaseOperation.DELETE_ALL.execute(connection, dataSet);

        } catch (DatabaseUnitException | SQLException e) {
            throw new RuntimeException(e);
        }

    }

    public void stopContainers() throws IllegalAccessException {
        super.stopContainers();

        try {
            connection.close();
        } catch (SQLException e) {
            LOGGER.warn("Unexpected SQLException during close of the connection " + e.getMessage());
        }

        jdbcDatabaseContainer.stop();

        databaseConnectionField.set(null, null);
        databaseContainerField.set(null, null);
    }
}