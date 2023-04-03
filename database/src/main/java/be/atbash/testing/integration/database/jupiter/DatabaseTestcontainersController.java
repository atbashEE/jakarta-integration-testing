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
import be.atbash.testing.integration.container.image.TestContext;
import be.atbash.testing.integration.database.exception.DataScriptException;
import be.atbash.testing.integration.database.exception.DatabaseScriptException;
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

    private final DatabaseContainerAdapterMetaData databaseContainerMetaData;

    private IDataSet dataSet;

    private IDatabaseConnection connection;

    private Field databaseConnectionField;
    private Field databaseContainerField;

    public DatabaseTestcontainersController(Class<?> testClass, JdbcDatabaseContainer<?> jdbcDatabaseContainer
            , DatabaseContainerAdapterMetaData databaseContainerMetaData) {
        super(testClass);
        this.jdbcDatabaseContainer = jdbcDatabaseContainer;
        this.databaseContainerMetaData = databaseContainerMetaData;
    }

    @Override
    public void config(ContainerAdapterMetaData metaData, TestContext testContext) {
        super.config(metaData, testContext);  // As it prepares the 'applicationTestContainer'
        String dbHostName = "db";

        jdbcDatabaseContainer.setNetwork(Network.SHARED);
        jdbcDatabaseContainer.withNetworkAliases(dbHostName);

        // We define ourselves the JDBC URL as testContainers give the version to access it 'outside' the shared
        // network. But we need to URL to access it from 'inside' the network.
        String jdbcUrl = String.format(databaseContainerMetaData.getDatabase().getJdbcURLTemplate(),
                dbHostName, jdbcDatabaseContainer.getExposedPorts().get(0));

        DatabaseContainerIntegrationTest containerIntegrationTest = databaseContainerMetaData.getDatabaseContainerIntegrationTest();

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
            awaitStartup(startSignal);

            // We can only access the Database container after it is started.
            // But this start() s part of beforeAll and thus before PostProcessTestInstance needs it.
            connection = databaseContainerMetaData.getDatabase().getConnectionSupplier().create(jdbcDatabaseContainer);

        } catch (Throwable e) {
            // The next statement handles the error.
        }
        if (failure.get()) {
            // Set by the callback in case of error.  This stops the execution of the test
            throw new AssertionError("Test aborted");
        }

        LOGGER.info(String.format("All containers started in %s ms", System.currentTimeMillis() - start));
    }

    private static void awaitStartup(CountDownLatch startSignal) {
        try {
            boolean containerStarted = startSignal.await(1, TimeUnit.MINUTES);
            if (!containerStarted) {
                throw new AssertionError("Test aborted since Database container does not start within a reasonable time");
            }
        } catch (InterruptedException e) {
            throw new UnexpectedException("Database Container start interrupted", e);
        }
    }

    public void injectInstances(Object testInstance) {
        Class<?> testClass = testInstance.getClass();
        try {
            databaseConnectionField = testClass.getField("databaseConnection");
            databaseConnectionField.set(null, connection);

            databaseContainerField = testClass.getField("databaseContainer");
            databaseContainerField.set(null, jdbcDatabaseContainer);
        } catch (Exception e) {
            throw new UnexpectedException("Unexpected exception happened during injection of istances in database test fields", e);
        }
    }

    private void manageStartDatabaseContainer(CountDownLatch startSignal, AtomicBoolean failure) {
        FailureCallback callback = () -> failure.set(true);

        if (databaseContainerMetaData.getDatabaseContainerIntegrationTest().databaseContainerStartInParallel()) {
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

            String createTables = databaseContainerMetaData.getDatabaseContainerIntegrationTest().databaseScriptFiles().createTables();
            String script = getClassPathFileContent(createTables);

            try {

                ScriptUtils.executeDatabaseScript(delegate, createTables, script);
            } catch (ScriptException e) {
                throw new DatabaseScriptException("failure during execution of script : " + script, e);
            }

            String dataFile = databaseContainerMetaData.getDatabaseContainerIntegrationTest().databaseScriptFiles().initData();
            URL testDataFile = DatabaseTestcontainersController.class.getClassLoader().getResource(dataFile);
            if (testDataFile == null) {
                throw new FileNotFoundException(String.format("The file with name '%s' is not found on the class path", dataFile));
            }

            createDataSet(testDataFile);

        } catch (Throwable e) {  // We really want to have all exceptions captured here for proper Thread handling.
            // Report to the calling context that there was a problem.
            // Since this method can be executed in a Thread, throwing an Exception is not enough.
            callback.failed();
            throw new AssertionError("Test aborted due to :" + e.getMessage());
        } finally {
            // Always signal the method is finished.
            startSignal.countDown();  // Startup done
        }
    }

    private void createDataSet(URL testDataFile) {
        try {
            dataSet = new XlsDataSet(Paths.get(testDataFile.toURI()).toFile());
        } catch (IOException | DataSetException | URISyntaxException e) {
            throw new UnexpectedException("Unexpected exception happened during creation of Dataset from " + testDataFile, e);
        }
    }

    public void uploadData() {
        try {

            DatabaseOperation.CLEAN_INSERT.execute(connection, dataSet);

        } catch (DatabaseUnitException | SQLException e) {
            throw new DataScriptException("Exception during execution of data insert", e);
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
            throw new DataScriptException("Exception during execution of data delete", e);

        }

    }

    @Override
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
