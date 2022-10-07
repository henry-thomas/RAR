/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.LocalTransaction;

/**
 *
 * @author henry
 */
public class JcLocalTransaction implements LocalTransaction {

    private static final Logger log = Logger.getLogger("JcLocalTransaction");

    private final JcManagedConnection mconnection;
    private final JcConnectionImpl handle;
    private boolean isComplete = false;

    public JcLocalTransaction(JcManagedConnection mconnection, JcConnectionImpl handle) {
        this.mconnection = mconnection;
        this.handle = handle;
    }

    @Override
    public void begin() throws ResourceException {
        log.info("[JcLocalTransaction] begin()");
        isComplete = false;
    }

    @Override
    public void commit() throws ResourceException {
        log.info("[JcLocalTransaction] commit()");
//        if (isComplete) {
//        mconnection.closeHandle(handle);
//        }
    }

    @Override
    public void rollback() throws ResourceException {
        log.info("[JcLocalTransaction] rollback()");
    }

    public boolean isIsComplete() {
        return isComplete;
    }

    public void setIsComplete(boolean isComplete) {
        this.isComplete = isComplete;
    }

}
