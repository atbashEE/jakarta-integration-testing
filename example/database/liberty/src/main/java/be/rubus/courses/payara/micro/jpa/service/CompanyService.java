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
package be.rubus.courses.payara.micro.jpa.service;

import be.rubus.courses.payara.micro.jpa.model.Company;

import javax.enterprise.context.ApplicationScoped;
import javax.persistence.EntityManager;
import javax.persistence.PersistenceContext;
import javax.transaction.Transactional;
import java.util.List;

/**
 *
 */
@ApplicationScoped  // Commit at end of method or rollback in case of Exception
@Transactional
public class CompanyService {

    @PersistenceContext(name = "TestUnit")
    private EntityManager em;

    public Company findCompany(Long id) {
        return em.find(Company.class, id);
    }

    public List<Company> allCompanies() {
        return em.createQuery("SELECT c FROM Company c", Company.class).getResultList();
    }

    public Company insertCompany(Company company) {
        em.persist(company);
        return company;
    }
}
