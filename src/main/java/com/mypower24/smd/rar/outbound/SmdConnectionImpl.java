/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import com.mypower24.smd.rar.api.out.SmdConnection;
import com.mypower24.smd.rar.lib.TestRequest;
import com.mypower24.smd.rar.lib.TestResponse;
import java.io.IOException;
import java.util.logging.Logger;
import javax.resource.ResourceException;

/**
 *
 * @author henry
 */
public class SmdConnectionImpl implements SmdConnection {

    private static final Logger log = Logger.getLogger("SmdConnectionImpl");
    private SmdManagedConnection mconnection;
    private boolean valid;

    SmdConnectionImpl(SmdManagedConnection mconnection) {
        this.mconnection = mconnection;
        valid = true;
    }

    SmdManagedConnection getManagedConnection() {
        return mconnection;
    }

    public void setManagedConnection(SmdManagedConnection mconnection) {
        this.mconnection = mconnection;
    }

    /* Called by the managed connection to invalidate this handle */
    public void invalidate() {
        valid = false;
    }

    @Override
    public TestResponse send(TestRequest req) throws Exception {
        log.info("[SmdConnectionImpl] send()");
        if (valid) {
            try {
                TestResponse resp = mconnection.sendCommandToServer(req);
                return resp;
            } catch (IOException e) {
                mconnection.destroy();
//                close();
                throw new IOException(e.getMessage());
            }
        }

        throw new Exception("Connection handle is invalid");
    }

    @Override
    public void close() throws ResourceException {
        log.info("[SmdConnectionImpl] close()");
        valid = false;
        mconnection.disassociateConnection();
    }
}
