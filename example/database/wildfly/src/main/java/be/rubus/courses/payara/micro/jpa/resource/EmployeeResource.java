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

import be.rubus.courses.payara.micro.jpa.model.Employee;
import be.rubus.courses.payara.micro.jpa.service.EmployeeService;

import jakarta.enterprise.context.RequestScoped;
import jakarta.inject.Inject;
import jakarta.ws.rs.*;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.util.List;

@Path("/employee")
@RequestScoped
    @Produces(MediaType.APPLICATION_JSON)
public class EmployeeResource {

    @Inject
    private EmployeeService employeeService;

    @GET
    public List<Employee> getAllEmployees() {
        return employeeService.getAllEmployees();
    }

    @Path("/{employeeId}")
    @GET
    public Response getEmployee(@PathParam("employeeId")Long employeeId) {
        Employee employee = employeeService.getEmployee(employeeId);
        if (employee == null) {
            // Employee with Id not found so the URL doesn't exist.
            return Response.status(Response.Status.NOT_FOUND).build();
        } else {
            return Response.ok(employee).build();
        }
    }

    @Path("/{employeeId}")
    @Consumes(MediaType.APPLICATION_JSON)
    @POST
    public void updateEmployee(@PathParam("employeeId")Long employeeId, Employee employee) {
        employeeService.updateEmployee(employee);
    }

    @Path("/company/{companyId}")
    @GET
    public List<Employee> getEmployeesOfCompany(@PathParam("companyId")Long companyId) {
        return employeeService.getEmployeesOfCompany(companyId);
    }

}
