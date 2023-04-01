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
package be.rubus.courses.payara.micro.jpa.resource;

import be.rubus.courses.payara.micro.jpa.model.Company;
import be.rubus.courses.payara.micro.jpa.service.CompanyService;

import javax.enterprise.context.RequestScoped;
import javax.inject.Inject;
import javax.ws.rs.*;
import javax.ws.rs.core.MediaType;
import java.util.List;


@Path("/company")
@RequestScoped
public class CompanyResource {

    @Inject
    private CompanyService companyService;

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    public Company getCompany(@PathParam("id") Long id) {
        return companyService.findCompany(id);
    }

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    public List<Company> getAllCompany() {
        return companyService.allCompanies();
    }

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    public Company insertCompany(Company company) {
        return companyService.insertCompany(company);
    }

}
