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

import be.rubus.courses.payara.micro.jpa.model.Company;

import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;

@Path("/rest/company")
public interface CompanyService {

    @GET
    @Produces(MediaType.APPLICATION_JSON)
    @Path("/{id}")
    Company getCompany(@PathParam("id") Long id);

    @POST
    @Consumes(MediaType.APPLICATION_JSON)
    Company insertCompany(Company company);
}
