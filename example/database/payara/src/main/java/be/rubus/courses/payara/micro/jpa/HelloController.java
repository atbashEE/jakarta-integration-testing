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
package be.rubus.courses.payara.micro.jpa;

import org.eclipse.microprofile.config.inject.ConfigProperty;

import javax.inject.Inject;
import javax.ws.rs.GET;
import javax.ws.rs.Path;

@Path("/hello")
public class HelloController {

    @Inject
    @ConfigProperty(name = "ds_url")
    private String url;

    @Inject
    @ConfigProperty(name = "ds_username")
    private String user;

    @Inject
    @ConfigProperty(name = "ds_password")
    private String password;


    @GET

    public String getParameters() {
        return String.format("config \n%s\n%s\n%s", url, user, password);
    }
}
