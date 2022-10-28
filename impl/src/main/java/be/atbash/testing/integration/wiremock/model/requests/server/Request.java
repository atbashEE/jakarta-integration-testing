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

package be.atbash.testing.integration.wiremock.model.requests.server;

import com.fasterxml.jackson.annotation.*;

import java.util.HashMap;
import java.util.Map;

@JsonInclude(JsonInclude.Include.NON_NULL)
@JsonPropertyOrder({
        "id",
        "request",
        "responseDefinition",
        "response",
        "wasMatched",
        "timing",
        "stubMapping"
})
public class Request {

    @JsonProperty("id")
    private String id;
    @JsonProperty("request")
    private ClientRequest request;
    @JsonProperty("responseDefinition")
    private ResponseDefinition responseDefinition;
    @JsonProperty("response")
    private Response response;
    @JsonProperty("wasMatched")
    private Boolean wasMatched;
    @JsonProperty("timing")
    private Timing timing;
    @JsonProperty("stubMapping")
    private StubMapping stubMapping;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public ClientRequest getRequest() {
        return request;
    }

    public void setRequest(ClientRequest request) {
        this.request = request;
    }

    public ResponseDefinition getResponseDefinition() {
        return responseDefinition;
    }

    public void setResponseDefinition(ResponseDefinition responseDefinition) {
        this.responseDefinition = responseDefinition;
    }

    public Response getResponse() {
        return response;
    }

    public void setResponse(Response response) {
        this.response = response;
    }

    public Boolean getWasMatched() {
        return wasMatched;
    }

    public void setWasMatched(Boolean wasMatched) {
        this.wasMatched = wasMatched;
    }

    public Timing getTiming() {
        return timing;
    }

    public void setTiming(Timing timing) {
        this.timing = timing;
    }

    public StubMapping getStubMapping() {
        return stubMapping;
    }

    public void setStubMapping(StubMapping stubMapping) {
        this.stubMapping = stubMapping;
    }

    @JsonAnyGetter
    public Map<String, Object> getAdditionalProperties() {
        return this.additionalProperties;
    }

    @JsonAnySetter
    public void setAdditionalProperty(String name, Object value) {
        this.additionalProperties.put(name, value);
    }

}
