/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import com.mypower24.smd.rar.api.out.SmdConnection;
import com.mypower24.smd.rar.api.out.SmdConnectionFactory;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionManager;

/**
 *
 * @author henry
 */
public class SmdConnectionFactoryImpl implements SmdConnectionFactory {

    private static final Logger log = Logger.getLogger("SmdConnectionFactoryImpl");
    private final ConnectionManager cmanager;
    private final SmdManagedConnectionFactory mcfactory;

    SmdConnectionFactoryImpl(SmdManagedConnectionFactory mcfactory,
                               ConnectionManager cmanager) {
        this.mcfactory = mcfactory;
        this.cmanager = cmanager;
    }
    
   /* Applications call this method, which delegates on the container's
     * connection manager to obtain a connection instance through
     * TradeManagedConnectionFactory */
    @Override
    public SmdConnection getConnection(String host, int port) throws ResourceException {
        log.info("[TradeConnectionFactoryImpl] getConnection()");
        
        mcfactory.setHost(host);
        mcfactory.setPort(String.valueOf(port));
        
        return (SmdConnection) cmanager.allocateConnection(mcfactory, null);
    }

}
