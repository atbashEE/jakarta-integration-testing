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
package be.atbash.test.integration.example.hello;

import be.atbash.testing.integration.database.jupiter.DatabaseContainerIntegrationTest;
import be.atbash.testing.integration.database.test.AbstractDatabaseContainerIntegrationTest;
import be.atbash.testing.integration.jupiter.ContainerIntegrationTest;
import be.atbash.testing.integration.jupiter.SupportedRuntime;
import be.rubus.courses.payara.micro.jpa.model.Company;
import org.assertj.core.api.Assertions;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.ITable;
import org.eclipse.microprofile.rest.client.inject.RestClient;
import org.junit.jupiter.api.Test;

import java.math.BigInteger;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.Map;


@DatabaseContainerIntegrationTest(
        containerIntegrationTest = @ContainerIntegrationTest(runtime = SupportedRuntime.OPEN_LIBERTY)
)
public class DatabaseLibertyIT extends AbstractDatabaseContainerIntegrationTest {
    // Should be public and not the JUnit 5 preferred scope package.

    @RestClient
    public CompanyService companyService;

    @Test
    void getCompany() {
        Company company = companyService.getCompany(0L);
        Assertions.assertThat(company.getName()).isEqualTo("Atbash");
    }

    @Test
    void addCompany() throws SQLException, DataSetException {
        Company newCompany = new Company();
        newCompany.setName("JUnit");
        Company addedCompany = companyService.insertCompany(newCompany);
        Assertions.assertThat(addedCompany.getId()).isNotNull();

        int rowCount = databaseConnection.getRowCount("Company");
        Assertions.assertThat(rowCount).isEqualTo(3);

        ITable companyTable = databaseConnection.createQueryTable("company", "SELECT id, name FROM Company");
        Map<Long, String> rows = new HashMap<>();
        for (int idx = 0; idx < 3; idx++) {
            rows.put(((BigInteger) companyTable.getValue(idx, "id")).longValue(), (String) companyTable.getValue(idx, "name"));
        }
        Assertions.assertThat(rows).containsEntry(0L, "Atbash");
        Assertions.assertThat(rows).containsEntry(1L, "Jakarta");
        Assertions.assertThat(rows).containsEntry(addedCompany.getId(), "JUnit");
    }
}