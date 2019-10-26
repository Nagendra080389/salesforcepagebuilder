package com.sfpage.salesforcepagebuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sfpage.canvas.CanvasAuthentication;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SfRestController {

    org.slf4j.Logger LOGGER = LoggerFactory.getLogger(SfRestController.class);
    private static final String SESSION_ID = "***";
    public static final String INSTANCE_URL = "**";

    private PartnerConnection partnerConnection = null;

    @Value("${toolingURL}")
    volatile String toolingURL;

    @Value("${partnerURL}")
    volatile String partnerURL;

    @RequestMapping(value = "/getObjectNames", method = RequestMethod.GET)
    public String getObjectNames(HttpServletResponse response, HttpServletRequest request) throws ConnectionException {

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(SESSION_ID);
        try {
            config.setServiceEndpoint(INSTANCE_URL + partnerURL);
            partnerConnection = Connector.newConnection(config);
        } catch (Exception e) {

        }

        DescribeGlobalResult describeGlobalResult = partnerConnection.describeGlobal();

        System.out.println(describeGlobalResult);
        List<String> lstObjectsName = new ArrayList<>();
        for (DescribeGlobalSObjectResult sobject : describeGlobalResult.getSobjects()) {
            if (sobject.getLayoutable() && sobject.isUpdateable()) {
                lstObjectsName.add(sobject.getName());
            }
        }


        Gson gson = new GsonBuilder().create();

        return gson.toJson(lstObjectsName);
    }

    @RequestMapping(value = "/getSobjectFields", method = RequestMethod.GET, params = {"strObjectName"})
    public String getSobjectFields(@RequestParam String strObjectName, HttpServletResponse response, HttpServletRequest request) throws ConnectionException {

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId(SESSION_ID);
        try {
            config.setServiceEndpoint("https://nagesingh-dev-ed.my.salesforce.com" + partnerURL);
            partnerConnection = Connector.newConnection(config);
        } catch (Exception e) {

        }

        DescribeSObjectResult describeGlobalResult = partnerConnection.describeSObject(strObjectName);

        List<String> lstObjectFieldNames = new ArrayList<>();
        for (Field sobjectField : describeGlobalResult.getFields()) {
            lstObjectFieldNames.add(sobjectField.getName());
        }


        Gson gson = new GsonBuilder().create();

        return gson.toJson(lstObjectFieldNames);
    }

    @RequestMapping(value = "/sfdcauth/{endpoint}", method = RequestMethod.POST)
    public ResponseEntity<String> canvasPost(@PathVariable(name = "endpoint", required = false) final String endPoint,
                                             final HttpSession session, final HttpServletRequest request,
                                             final HttpServletResponse response) {

        final String signedRequest = request.getParameter("signed_request");
        LOGGER.info("signedRequest - > "+signedRequest);
        final String redirectTo = ((endPoint == null) || "".equals(endPoint)) ? "/" : "/" + endPoint;

        if (signedRequest == null) {
            return new ResponseEntity<>("signed_request missing", HttpStatus.BAD_REQUEST);
        }

        try {
            final CanvasAuthentication auth = CanvasAuthentication.create(request, signedRequest);
            if ((auth != null) && auth.isAuthenticated()) {
                // The canvas request was valid, we add Header and Token
                final String token = auth.getJwtToken();
                CanvasAuthentication.addJwtCookie(session, request, response, token);

                // Prepare the header for the redirect to actual payload
                final HttpHeaders headers = new HttpHeaders();
                headers.add("Location", redirectTo);
                return new ResponseEntity<String>(redirectTo, headers, HttpStatus.SEE_OTHER);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("signed_request invalid:" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // If we got here - it failed!
        return new ResponseEntity<>("Authorization failed", HttpStatus.UNAUTHORIZED);

    }
}
