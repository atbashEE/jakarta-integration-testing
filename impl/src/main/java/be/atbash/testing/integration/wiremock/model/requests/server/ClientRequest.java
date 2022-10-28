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
        "url",
        "absoluteUrl",
        "method",
        "clientIp",
        "headers",
        "cookies",
        "browserProxyRequest",
        "loggedDate",
        "bodyAsBase64",
        "body",
        "protocol",
        "scheme",
        "host",
        "port",
        "loggedDateString",
        "queryParams"
})
public class ClientRequest {

    @JsonProperty("url")
    private String url;
    @JsonProperty("absoluteUrl")
    private String absoluteUrl;
    @JsonProperty("method")
    private String method;
    @JsonProperty("clientIp")
    private String clientIp;
    @JsonProperty("headers")
    private Headers headers;
    @JsonProperty("cookies")
    private Cookies cookies;
    @JsonProperty("browserProxyRequest")
    private Boolean browserProxyRequest;
    @JsonProperty("loggedDate")
    private Long loggedDate;
    @JsonProperty("bodyAsBase64")
    private String bodyAsBase64;
    @JsonProperty("body")
    private String body;
    @JsonProperty("protocol")
    private String protocol;
    @JsonProperty("scheme")
    private String scheme;
    @JsonProperty("host")
    private String host;
    @JsonProperty("port")
    private Integer port;
    @JsonProperty("loggedDateString")
    private String loggedDateString;
    @JsonProperty("queryParams")
    private QueryParams queryParams;
    @JsonIgnore
    private final Map<String, Object> additionalProperties = new HashMap<>();

    public String getUrl() {
        return url;
    }

    public void setUrl(String url) {
        this.url = url;
    }

    public String getAbsoluteUrl() {
        return absoluteUrl;
    }

    public void setAbsoluteUrl(String absoluteUrl) {
        this.absoluteUrl = absoluteUrl;
    }

    public String getMethod() {
        return method;
    }

    public void setMethod(String method) {
        this.method = method;
    }

    public String getClientIp() {
        return clientIp;
    }

    public void setClientIp(String clientIp) {
        this.clientIp = clientIp;
    }

    public Headers getHeaders() {
        return headers;
    }

    public void setHeaders(Headers headers) {
        this.headers = headers;
    }

    public Cookies getCookies() {
        return cookies;
    }

    public void setCookies(Cookies cookies) {
        this.cookies = cookies;
    }

    public Boolean getBrowserProxyRequest() {
        return browserProxyRequest;
    }

    public void setBrowserProxyRequest(Boolean browserProxyRequest) {
        this.browserProxyRequest = browserProxyRequest;
    }

    public Long getLoggedDate() {
        return loggedDate;
    }

    public void setLoggedDate(Long loggedDate) {
        this.loggedDate = loggedDate;
    }

    public String getBodyAsBase64() {
        return bodyAsBase64;
    }

    public void setBodyAsBase64(String bodyAsBase64) {
        this.bodyAsBase64 = bodyAsBase64;
    }

    public String getBody() {
        return body;
    }

    public void setBody(String body) {
        this.body = body;
    }

    public String getProtocol() {
        return protocol;
    }

    public void setProtocol(String protocol) {
        this.protocol = protocol;
    }

    public String getScheme() {
        return scheme;
    }

    public void setScheme(String scheme) {
        this.scheme = scheme;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public Integer getPort() {
        return port;
    }

    public void setPort(Integer port) {
        this.port = port;
    }

    public String getLoggedDateString() {
        return loggedDateString;
    }

    public void setLoggedDateString(String loggedDateString) {
        this.loggedDateString = loggedDateString;
    }

    public QueryParams getQueryParams() {
        return queryParams;
    }

    public void setQueryParams(QueryParams queryParams) {
        this.queryParams = queryParams;
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
