/*
 * Copyright 2022 Rudy De Busscher (https://www.atbash.be)
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
package be.atbash.testing.integration.wiremock.model.requests;

import be.atbash.testing.integration.wiremock.model.requests.server.ClientRequest;
import be.atbash.testing.integration.wiremock.model.requests.server.Response;

public class RequestInfo {

    private final ClientRequest request;
    private final Response response;

    public RequestInfo(ClientRequest request, Response response) {
        this.request = request;
        this.response = response;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public Response getResponse() {
        return response;
    }
}
