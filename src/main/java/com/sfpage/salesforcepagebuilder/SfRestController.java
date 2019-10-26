package com.sfpage.salesforcepagebuilder;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.sforce.soap.partner.*;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SfRestController {

    private static final String SESSION_ID = "***";
    public static final String INSTANCE_URL = "**";

    private PartnerConnection partnerConnection = null;

    @Value("${toolingURL}")
    volatile String toolingURL;

    @Value("${partnerURL}")
    volatile String partnerURL;

    @Value("${serviceURL}")
    volatile String serviceURL;

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
}
