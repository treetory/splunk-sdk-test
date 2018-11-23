package com.treetory.test.common.util.splunk;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.splunk.SSLSecurityProtocol;
import com.splunk.Service;
import com.splunk.ServiceArgs;

public class SplunkClient {

    private static final Logger LOG = LoggerFactory.getLogger(SplunkClient.class);
    
    public Service splunkService;
    
    public void connect(String hostIp, String username, String password, int port, String scheme) {
        ServiceArgs connArgs = new ServiceArgs();
        connArgs.setHost(hostIp);
        connArgs.setUsername(username);
        connArgs.setPassword(password);
        connArgs.setPort(port);
        connArgs.setScheme(scheme);
                
        Service.setSslSecurityProtocol(SSLSecurityProtocol.TLSv1_2);
        this.splunkService = Service.connect(connArgs);
        
        StringBuffer sb = new StringBuffer();
        sb.append(System.lineSeparator());
        sb.append(System.lineSeparator());
        sb.append(String.format("APP = %s", this.splunkService.getApp()));
        
        LOG.debug("{}", sb.toString());
    }
    
    public void disconnect() {
        this.splunkService.logout();
        this.splunkService = null;
    }
    
}
