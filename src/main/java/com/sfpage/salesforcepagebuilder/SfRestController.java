package com.sfpage.salesforcepagebuilder;

import com.sforce.soap.partner.Connector;
import com.sforce.soap.partner.DescribeGlobalResult;
import com.sforce.soap.partner.PartnerConnection;
import com.sforce.ws.ConnectionException;
import com.sforce.ws.ConnectorConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

@RestController
public class SfRestController {

    @Value("${toolingURL}")
    volatile String toolingURL;

    @Value("${partnerURL}")
    volatile String partnerURL;

    @Value("${serviceURL}")
    volatile String serviceURL;

    @RequestMapping(value = "/getAllApexClasses", method = RequestMethod.GET)
    public String getAllApexClasses(HttpServletResponse response, HttpServletRequest request) throws IOException, ConnectionException {

        ConnectorConfig config = new ConnectorConfig();
        config.setSessionId("00D7F00000027wN!AQ4AQDmVGPo0TNKd68wlIFZ4EwybXqkef8BR14_98uDUHUVBpJjXxnnvI1RN9bZd1iaHYuWISKkypZznAeCNsFH4cgD_7Um6");
        PartnerConnection partnerConnection = null;
        try {
            config.setServiceEndpoint("https://nagesingh-dev-ed.my.salesforce.com" + partnerURL);
            partnerConnection = Connector.newConnection(config);
        } catch (Exception e){

        }

        DescribeGlobalResult describeGlobalResult = partnerConnection.describeGlobal();

        System.out.println(describeGlobalResult);

        return "";
    }
}
