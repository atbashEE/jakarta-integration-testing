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

import be.atbash.testing.integration.wiremock.model.mappings.MappingBuilder;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

import javax.ws.rs.HttpMethod;
import javax.ws.rs.core.MediaType;

class MappingBuilderTest {

    @Test
    void basicStringResponse() {
        String value = new MappingBuilder()
                .forURL("/path1")
                .withBody("Atbash Testing")
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path1\",\"method\":\"GET\"},\"response\":{\"status\":200,\"body\":\"Atbash Testing\",\"headers\":{\"Content-Type\":\"text/plain\"}}}");
    }

    @Test
    void basicJSONResponse() {
        Data data = new Data();
        data.setId(123);
        data.setName("Atbash Testing");
        String value = new MappingBuilder()
                .forURL("/path2")
                .withBody(data)
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path2\",\"method\":\"GET\"},\"response\":{\"status\":200,\"body\":\"{\\\"id\\\":123,\\\"name\\\":\\\"Atbash Testing\\\"}\",\"headers\":{\"Content-Type\":\"application/json\"}}}");
    }

    @Test
    void defineMethod() {
        String value = new MappingBuilder()
                .forURL("/path3")
                .withBody("POST result")
                .withMethod(HttpMethod.POST)
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path3\",\"method\":\"POST\"},\"response\":{\"status\":200,\"body\":\"POST result\",\"headers\":{\"Content-Type\":\"text/plain\"}}}");
    }

    @Test
    void defineContentType() {
        String value = new MappingBuilder()
                .forURL("/path4")
                .withBody("<root><tag>content</tag></root>")
                .withContentType(MediaType.APPLICATION_XML)
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path4\",\"method\":\"GET\"},\"response\":{\"status\":200,\"body\":\"<root><tag>content</tag></root>\",\"headers\":{\"Content-Type\":\"application/xml\"}}}");
    }

    @Test
    void bodyDefinesContentType() {
        String value = new MappingBuilder()
                .forURL("/path5")
                .withContentType(MediaType.APPLICATION_XML)
                .withBody("Atbash testing")
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path5\",\"method\":\"GET\"},\"response\":{\"status\":200,\"body\":\"Atbash testing\",\"headers\":{\"Content-Type\":\"text/plain\"}}}");
    }

    @Test
    void defineStatus() {
        String value = new MappingBuilder()
                .forURL("/path6")
                .withStatus(404)
                .build();
        Assertions.assertThat(value).isEqualTo("{\"request\":{\"url\":\"/path6\",\"method\":\"GET\"},\"response\":{\"status\":404,\"body\":null,\"headers\":{\"Content-Type\":\"text/plain\"}}}");
    }

}