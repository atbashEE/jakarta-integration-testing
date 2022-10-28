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
        "addedDelay",
        "processTime",
        "responseSendTime",
        "serveTime",
        "totalTime"
})
public class Timing {

    @JsonProperty("addedDelay")
    private Integer addedDelay;
    @JsonProperty("processTime")
    private Integer processTime;
    @JsonProperty("responseSendTime")
    private Integer responseSendTime;
    @JsonProperty("serveTime")
    private Integer serveTime;
    @JsonProperty("totalTime")
    private Integer totalTime;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public Integer getAddedDelay() {
        return addedDelay;
    }

    public void setAddedDelay(Integer addedDelay) {
        this.addedDelay = addedDelay;
    }

    public Integer getProcessTime() {
        return processTime;
    }

    public void setProcessTime(Integer processTime) {
        this.processTime = processTime;
    }

    public Integer getResponseSendTime() {
        return responseSendTime;
    }

    public void setResponseSendTime(Integer responseSendTime) {
        this.responseSendTime = responseSendTime;
    }

    public Integer getServeTime() {
        return serveTime;
    }

    public void setServeTime(Integer serveTime) {
        this.serveTime = serveTime;
    }

    public Integer getTotalTime() {
        return totalTime;
    }

    public void setTotalTime(Integer totalTime) {
        this.totalTime = totalTime;
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
