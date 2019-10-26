package com.sfpage.salesforcepagebuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import com.sfpage.canvas.CanvasAuthentication;
import com.sfpage.canvas.CanvasRequest;
import com.sfpage.canvas.SignedRequest;
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
    private static String SESSION_ID = null;
    public static String INSTANCE_URL = null;

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
            config.setServiceEndpoint(INSTANCE_URL + partnerURL);
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

        if (signedRequest == null) {
            return new ResponseEntity<>("signed_request missing", HttpStatus.BAD_REQUEST);
        }

        try {
            CanvasRequest cr = SignedRequest.verifyAndDecode(signedRequest, System.getenv("SFDC_SECRET"));

            if(cr.getClient() != null) {
                SESSION_ID = cr.getClient().getOAuthToken();
                INSTANCE_URL = cr.getClient().getInstanceUrl();
                return new ResponseEntity<>("/index", HttpStatus.SEE_OTHER);
            }

        } catch (final Exception e) {
            e.printStackTrace();
            return new ResponseEntity<>("signed_request invalid:" + e.getMessage(), HttpStatus.BAD_REQUEST);
        }

        // If we got here - it failed!
        return new ResponseEntity<>("Authorization failed", HttpStatus.UNAUTHORIZED);

    }
}
