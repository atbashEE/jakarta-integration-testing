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
package be.atbash.testing.integration.wiremock.model;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.Assertions;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

public class MappingBuilder {

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private int status = 200;
    private String url;
    private String method = HttpMethod.GET;
    private String body;
    private String contentType = MediaType.TEXT_PLAIN;  // WireMock expects always a Content type

    public MappingBuilder forURL(String url) {
        this.url = url;
        return this;
    }

    public MappingBuilder withBody(String body) {
        this.body = body;
        contentType = MediaType.TEXT_PLAIN;
        return this;
    }

    public MappingBuilder withBody(Object body) {
        try {
            this.body = MAPPER.writeValueAsString(body);
        } catch (JsonProcessingException e) {
            Assertions.fail(e.getMessage());
        }
        contentType = MediaType.APPLICATION_JSON;
        return this;
    }

    public MappingBuilder withStatus(int status) {
        this.status = status;
        return this;
    }

    public MappingBuilder withMethod(String method) {
        this.method = method;
        return this;
    }

    public MappingBuilder withContentType(String contentType) {
        this.contentType = contentType;
        return this;
    }

    public String build() {
        Mapping result = new Mapping();

        Request request = new Request();
        request.setUrl(url);
        request.setMethod(method);
        result.setRequest(request);

        Response response = new Response();

        Headers headers = new Headers();
        headers.setContentType(contentType);

        response.setHeaders(headers);
        response.setBody(body);
        response.setStatus(status);

        result.setResponse(response);

        try {
            return MAPPER.writeValueAsString(result);
        } catch (JsonProcessingException e) {
            Assertions.fail(e.getMessage());
        }

        return null;
    }
}
