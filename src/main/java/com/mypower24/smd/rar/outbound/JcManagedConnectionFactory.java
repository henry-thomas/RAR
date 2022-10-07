package com.mypower24.smd.rar.outbound;

import com.mypower24.smd.rar.api.out.JcConnection;
import com.mypower24.smd.rar.api.out.JcConnectionFactory;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.logging.Logger;
import javax.naming.Reference;
import javax.resource.ResourceException;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.ConnectionDefinition;
import javax.resource.spi.ConnectionManager;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionFactory;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterAssociation;
import javax.security.auth.Subject;

/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
/**
 *
 * @author henry
 */

/* The container's connection manager uses this class to create a pool
 * of managed connections, which are associated at times with physical ones */

 /* Define classes and interfaces for the EIS physical connection */
@ConnectionDefinition(
        connectionFactory = JcConnectionFactory.class,
        connectionFactoryImpl = JcConnectionFactoryImpl.class,
        connection = JcConnection.class,
        connectionImpl = JcConnectionImpl.class
)
public class JcManagedConnectionFactory implements ManagedConnectionFactory, ResourceAdapterAssociation {

    private static final Logger log = Logger.getLogger("JcBrokerManagedConnectionFactory");
    private static final long serialVersionUID = 7918855339952421358L;
    private ResourceAdapter ra;
    private Reference reference;
    private PrintWriter logWriter;
    private String host;
    private String port;
    private final Map<String, JcManagedConnection> serverIdManagedConnMap;

    public JcManagedConnectionFactory() {
        this.serverIdManagedConnMap = new HashMap<>();
    }

    @ConfigProperty(defaultValue = "localhost")
    public void setHost(String host) {
        this.host = host;
    }

    public String getHost() {
        return host;
    }

    @ConfigProperty(type = String.class, defaultValue = "4004")
    public void setPort(String port) {
        this.port = String.valueOf(port);
    }

    public String getPort() {
        return port;
    }

    @Override
    public Object createConnectionFactory(ConnectionManager cxManager) throws ResourceException {
        log.info("[JcBrokerManagedConnectionFactory] createConnectionFactory()");
        return new JcConnectionFactoryImpl(this, cxManager);
    }

    @Override
    public Object createConnectionFactory() throws ResourceException {
        log.info("[JcBrokerManagedConnectionFactory] createConnectionFactory()-NM");
        /* Non-managed connections not supported */
        return null;
    }

    @Override
    public ManagedConnection createManagedConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        log.info("[JcBrokerManagedConnectionFactory] createManagedConnection()");
        try {
            JcManagedConnection jcManagedConnection = new JcManagedConnection(getHost(), getPort(), ra, this);
            jcManagedConnection.addConnectionEventListener(new JcConnectionEventListener());
            
//            serverIdManagedConnMap.put(host + "-" + port, jcManagedConnection);
            
            return jcManagedConnection;
        } catch (IOException e) {
            throw new ResourceException(e.getCause());
        }
    }

    @Override
    public ManagedConnection matchManagedConnections(Set connectionSet, Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        /* This resource adapter does not use security (Subject) */
        JcManagedConnection match = null;
        /* This resource adapter has no additional parameters for connections,
         * so any open connection can be used by an application */
        for (Object mco : connectionSet) {
            if (mco != null) {
                match = (JcManagedConnection) mco;
//                log.info("Connection match!");
                break;
            }
        }
        return match;
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logWriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logWriter;
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        this.ra = ra;
    }

}
