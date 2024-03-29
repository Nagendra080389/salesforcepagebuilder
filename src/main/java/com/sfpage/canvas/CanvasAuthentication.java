/** ========================================================================= *
 * Copyright (C)  2017, 2018 Salesforce Inc ( http://www.salesforce.com/      *
 *                            All rights reserved.                            *
 *                                                                            *
 *  @author     Stephan H. Wissel (stw) <swissel@salesforce.com>              *
 *                                       @notessensei                         *
 * @version     1.0                                                           *
 * ========================================================================== *
 *                                                                            *
 * Licensed under the  Apache License, Version 2.0  (the "License").  You may *
 * not use this file except in compliance with the License.  You may obtain a *
 * copy of the License at <http://www.apache.org/licenses/LICENSE-2.0>.       *
 *                                                                            *
 * Unless  required  by applicable  law or  agreed  to  in writing,  software *
 * distributed under the License is distributed on an  "AS IS" BASIS, WITHOUT *
 * WARRANTIES OR  CONDITIONS OF ANY KIND, either express or implied.  See the *
 * License for the  specific language  governing permissions  and limitations *
 * under the License.                                                         *
 *                                                                            *
 * ========================================================================== *
 */
package com.sfpage.canvas;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.userdetails.User;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Date;
import java.util.UUID;


public class CanvasAuthentication implements Authentication {

    private static final long serialVersionUID = 1L;

    /**
     * Adds a JWT Token to Session and as Cookie
     *
     * @param session
     *            HTTP Session
     * @param request
     *            HTTP Request
     * @param response
     *            HTTP Response
     * @param token
     *            the JWT information
     */
    public static void addJwtCookie(final HttpSession session, final HttpServletRequest request,
                                    final HttpServletResponse response, final String token) {

        if ((token == null) || "".equals(token)) {
            return;
        }

        final Cookie jwtCookie = new Cookie(SecurityConstants.COOKIE_NAME, token);
        // Limit cookies lifetime
        jwtCookie.setMaxAge(Config.PARAMS.getCookieLifespan());
        jwtCookie.setPath("/");
        jwtCookie.setVersion(1);
        // Ensure nobody tampers with the cookie using JavaScript
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
        // Capture in the session for faster access
        session.setAttribute(SecurityConstants.COOKIE_ATTRIBUTE, token);
    }

    public static void addSessionIdCookie(final HttpSession session, final HttpServletRequest request,
                                    final HttpServletResponse response, final String token) {

        if ((token == null) || "".equals(token)) {
            return;
        }

        final Cookie jwtCookie = new Cookie(SecurityConstants.SESSION_ID, token);
        // Limit cookies lifetime
        jwtCookie.setMaxAge(Config.PARAMS.getCookieLifespan());
        jwtCookie.setPath("/");
        jwtCookie.setVersion(1);
        // Ensure nobody tampers with the cookie using JavaScript
        jwtCookie.setHttpOnly(true);
        response.addCookie(jwtCookie);
        // Capture in the session for faster access
        session.setAttribute(SecurityConstants.SESSION_ID_ATTRIBUTE, token);
    }

    public static void addInstanceURL(final HttpSession session, final HttpServletRequest request,
                                    final HttpServletResponse response, final String instanceURL) {

        if ((instanceURL == null) || "".equals(instanceURL)) {
            return;
        }

        final Cookie instanceURLCookie = new Cookie(SecurityConstants.INSTANCE_URL_NAME, instanceURL);
        // Limit cookies lifetime
        instanceURLCookie.setMaxAge(Config.PARAMS.getCookieLifespan());
        instanceURLCookie.setPath("/");
        instanceURLCookie.setVersion(1);
        // Ensure nobody tampers with the cookie using JavaScript
        instanceURLCookie.setHttpOnly(true);
        response.addCookie(instanceURLCookie);
        // Capture in the session for faster access
        session.setAttribute(SecurityConstants.INSTANCE_URL_ATTRIBUTE, instanceURL);

    }

    public static CanvasAuthentication create(final HttpServletRequest request, final String signedRequest)
            throws Exception {
        if ((signedRequest == null) || "".equals(signedRequest)) {
            throw new SecurityException("Canvas request is missing");
        }

        // Get the request as JsonNode or throw an exception
        final JsonNode json = SignedRequest.verifyAndDecodeAsJson(request, signedRequest,
                Config.PARAMS.getSfdcSecret());
        final CanvasAuthentication result = new CanvasAuthentication(json);

        return result;
    }

    public static CanvasAuthentication createAdminAccess(final String userName, final String password) {
        if (!Config.PARAMS.adminIsValid(userName, password)) {
            throw new SecurityException("Username or password missing");
        }
        return new CanvasAuthentication(userName);
    }

    private final JsonNode sfdcRequest;
    private final String                       name;
    private final Collection<GrantedAuthority> grantedAuthorities = new ArrayList<>();

    private boolean isAuthenticated;

    private CanvasAuthentication(final JsonNode theRequest) {
        this.sfdcRequest = theRequest;
        this.name = this.getRequestParam("context/user/userName", "Anonymous");
        this.isAuthenticated = !"Anonymous".equalsIgnoreCase(this.name);
        // A user is always a user
        this.grantedAuthorities.add(new CanvasGrantedAuthority("ROLE_USER"));
    }

    private CanvasAuthentication(final String userName) {
        this.sfdcRequest = new ObjectMapper().createObjectNode();
        this.name = userName;
        this.isAuthenticated = true;
        // Admin hase more roles
        this.grantedAuthorities.add(new CanvasGrantedAuthority("ROLE_USER"));
        this.grantedAuthorities.add(new CanvasGrantedAuthority("ROLE_ADMIN"));
        this.grantedAuthorities.add(new CanvasGrantedAuthority("ROLE_ACTUATOR"));
    }

    public CanvasAuthentication addAuthority(final String authorityName) {
        this.grantedAuthorities.add(new CanvasGrantedAuthority(authorityName));
        return this;
    }

    /**
     * @see org.springframework.security.core.Authentication#getAuthorities()
     */
    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return this.grantedAuthorities;
    }

    /**
     * @see org.springframework.security.core.Authentication#getCredentials()
     */
    @Override
    public Object getCredentials() {
        return this.getDetails();
    }

    /**
     * @see org.springframework.security.core.Authentication#getDetails()
     */
    @Override
    public Object getDetails() {
        final ObjectMapper om = new ObjectMapper();
        String json = null;
        try {
            json = om.writeValueAsString(this.sfdcRequest);
        } catch (final JsonProcessingException e) {
            e.printStackTrace();
            json = String.valueOf(this.sfdcRequest);
        }
        return json;
    }

    /**
     * Adds a JWT Header and cookie to the servlet response
     *
     */
    public String getJwtToken() {
        final Claims claims = Jwts.claims();
        // Capture roles
        this.getAuthorities().forEach(auth -> {
            claims.put(SecurityConstants.ROLE_PREFIX + auth.getAuthority(), auth.getAuthority());
        });
        // Finally capture user name
        claims.put(SecurityConstants.USER_NAME_CLAIM, this.getPrincipal().getUsername());

        // Insert here: other claims

        // Build Token
        final Date expDate = Config.PARAMS.getExpirationTime();
        final String token = Jwts.builder()
                .setClaims(claims)
                .setExpiration(expDate)
                .signWith(SignatureAlgorithm.HS512, Config.PARAMS.getSecret()).compact();

        return token;
    }

    /**
     * @see java.security.Principal#getName()
     */
    @Override
    public String getName() {
        return this.name;
    }

    /**
     * @see org.springframework.security.core.Authentication#getPrincipal()
     */
    @Override
    public User getPrincipal() {
        final User result = new User(this.getName(), UUID.randomUUID().toString(), this.getAuthorities());
        return result;
    }

    public String getRequestParam(final String request) {
        return this.getRequestParam(request, null);
    }

    /**
     * Fetches a value from the JSON path specified with slashes
     *
     * @param request
     *            the path requested e.g. context/user/userName
     * @param defaultValue
     * @return
     */
    public String getRequestParam(final String request, final String defaultValue) {
        final String[] paraChain = request.split("/");
        JsonNode curNode = this.sfdcRequest;
        // When chain starts with a slash, ignore the first segment
        for (int i = (paraChain[0] == "" ? 1 : 0); i < paraChain.length; i++) {
            curNode = curNode.get(paraChain[i]);
            if (curNode == null) {
                break;
            }
        }
        final String result = (curNode == null) ? defaultValue : curNode.asText();
        return result;
    }

    /**
     * @return the sfdcRequest
     */
    public final JsonNode getSfdcRequest() {
        return this.sfdcRequest;
    }

    /**
     * @see org.springframework.security.core.Authentication#isAuthenticated()
     */
    @Override
    public boolean isAuthenticated() {
        return this.isAuthenticated;
    }

    /**
     * @see org.springframework.security.core.Authentication#setAuthenticated(boolean)
     */
    @Override
    public void setAuthenticated(final boolean isAuthenticated) throws IllegalArgumentException {
        this.isAuthenticated = isAuthenticated;

    }

}
