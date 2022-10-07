/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;

/**
 *
 * @author henry
 */
public class JcConnectionEventListener implements ConnectionEventListener {

    private static final Logger log = Logger.getLogger(JcConnectionEventListener.class.getName());

    @Override
    public void connectionClosed(ConnectionEvent ce) {
        log.log(Level.SEVERE, "connectionClosed()");
        try {
            JcConnectionImpl conn = (JcConnectionImpl) ce.getConnectionHandle();
            conn.close();
            
        } catch (ResourceException ex) {
            log.log(Level.SEVERE, null, ex);
        }
    }

    @Override
    public void localTransactionStarted(ConnectionEvent ce) {
        log.log(Level.SEVERE, "localTransactionStarted()");
    }

    @Override
    public void localTransactionCommitted(ConnectionEvent ce) {
        log.log(Level.SEVERE, "localTransactionCommitted()");
//        try {
//            JcManagedConnection conn = (JcManagedConnection) ce.getConnectionHandle();
//            conn.cleanup();
//        } catch (ResourceException ex) {
//            log.log(Level.SEVERE, null, ex);
//        }
    }

    @Override
    public void localTransactionRolledback(ConnectionEvent ce) {
        log.log(Level.SEVERE, "localTransactionRolledback()");
    }

    @Override
    public void connectionErrorOccurred(ConnectionEvent ce) {
        log.log(Level.SEVERE, "connectionErrorOccurred()");
    }

}
