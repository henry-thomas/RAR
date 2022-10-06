/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;
import com.mypower24.smd.rar.api.out.JcConnection;
import com.mypower24.smd.rar.api.out.JcConnectionFactory;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import javax.naming.NamingException;
import javax.naming.Reference;

/**
 *
 * @author henry
 */
public class JcConnectionFactoryImpl implements JcConnectionFactory {

    private static final Logger log = Logger.getLogger("JcConnectionFactoryImpl");
    private final ConnectionManager cmanager;
    private final JcManagedConnectionFactory mcfactory;
    private JcConnection brokerConnection;
    private Reference reference;

    JcConnectionFactoryImpl(JcManagedConnectionFactory mcfactory,
            ConnectionManager cmanager) {
        this.mcfactory = mcfactory;
        this.cmanager = cmanager;
    }

    /* Applications call this method, which delegates on the container's
     * connection manager to obtain a connection instance through
     * TradeManagedConnectionFactory */
    @Override
    public JcConnection getConnection(String host, int port) throws ResourceException {
        log.info("[JcConnectionFactoryImpl] getConnection()");

        mcfactory.setHost(host);
        mcfactory.setPort(String.valueOf(port));
        

        return (JcConnection) cmanager.allocateConnection(mcfactory, null);
    }

    @Override
    public void setReference(Reference reference) {
        this.reference = reference;
    }

    @Override
    public Reference getReference() throws NamingException {
        return this.reference;
    }


}
