/**
 * Copyright (c) 2011-2013, salesforce.com, inc.
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without modification, are permitted provided
 * that the following conditions are met:
 *
 *    Redistributions of source code must retain the above copyright notice, this list of conditions and the
 *    following disclaimer.
 *
 *    Redistributions in binary form must reproduce the above copyright notice, this list of conditions and
 *    the following disclaimer in the documentation and/or other materials provided with the distribution.
 *
 *    Neither the name of salesforce.com, inc. nor the names of its contributors may be used to endorse or
 *    promote products derived from this software without specific prior written permission.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND ANY EXPRESS OR IMPLIED
 * WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A
 * PARTICULAR PURPOSE ARE DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT NOT LIMITED
 * TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION)
 * HOWEVER CAUSED AND ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT (INCLUDING
 * NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS SOFTWARE, EVEN IF ADVISED OF THE
 * POSSIBILITY OF SUCH DAMAGE.
 */
package com.sfpage.canvas;

/**
 *
 * The canvas request is what is sent to the client on the very first request. In this canvas
 * request is information for authenticating and context about the user, organization and environment.
 * <p>
 * This class is serialized into JSON on then signed by the signature service to prevent tampering.
 */
@org.codehaus.jackson.annotate.JsonIgnoreProperties(ignoreUnknown=true)
public class CanvasRequest {

    private String  algorithm;
    private Integer issuedAt;
    private String  userId;
    private CanvasContext canvasContext;
    private CanvasClient client;

    /**
     * The algorithm used to sign the request. typically HMAC-SHA256
     * @see
     */
    @org.codehaus.jackson.annotate.JsonProperty("algorithm")
    public String getAlgorithm() {
        return algorithm;
    }

    @org.codehaus.jackson.annotate.JsonProperty("algorithm")
    public void setAlgorithm(String algorithm) {
        this.algorithm = algorithm;
    }

    /**
     * The unix time this request was issued at.
     */
    @org.codehaus.jackson.annotate.JsonProperty("issuedAt")
    public Integer getIssuedAt() {
        return issuedAt;
    }

    @org.codehaus.jackson.annotate.JsonProperty("issuedAt")
    public void setIssuedAt(Integer issuedAt) {
        this.issuedAt = issuedAt;
    }

    /**
     * The Salesforce unique id for this user.
     * @return
     */
    @org.codehaus.jackson.annotate.JsonProperty("userId")
    public String getUserId() {
        return userId;
    }

    @org.codehaus.jackson.annotate.JsonProperty("userId")
    public void setUserId(String userId) {
        this.userId = userId;
    }

    /**
     * Context information about the user, org and environment.
     */
    @org.codehaus.jackson.annotate.JsonProperty("context")
    public CanvasContext getContext() {
        return canvasContext;
    }

    /**
     * Client instance information required while using the Sfdc.canvas.client JavaScript API.
     */
    @org.codehaus.jackson.annotate.JsonProperty("context")
    public void setContext(CanvasContext canvasContext) {
        this.canvasContext = canvasContext;
    }

    /**
     * Client information passed from client to server. Contains authorization information
     * and instance information.
     */
    @org.codehaus.jackson.annotate.JsonProperty("client")
    public CanvasClient getClient() {
        return client;
    }

    @org.codehaus.jackson.annotate.JsonProperty("client")
    public void setClient(CanvasClient client) {
        this.client = client;
    }

}
