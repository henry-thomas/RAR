/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.inbound;

import com.mypower24.smd.rar.SmdResourceAdapter;
import java.util.logging.Logger;
import javax.resource.spi.UnavailableException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;

/**
 *
 * @author henry
 */

/* This class is required only because obtaining an MDB endpoint
 * needs to be done in a different thread */
public class ObtainEndpointWork implements Work {

    private static final Logger log = Logger.getLogger("ObtainEndpointWork");
    private SmdResourceAdapter ra;
    private MessageEndpointFactory mef;
    private MessageEndpoint endpoint;

    public ObtainEndpointWork(SmdResourceAdapter ra,
            MessageEndpointFactory mef) {
        this.mef = mef;
        this.ra = ra;
    }

    public MessageEndpoint getMessageEndpoint() {
        return endpoint;
    }

    @Override
    public void release() {
        log.info("[ObtainEndpointWork] release()");
    }

    @Override
    public void run() {
         log.info("[ObtainEndpointWork] run()");
        try {
            /* Use the endpoint factory passed by the container upon
             * activation to obtain the MDB endpoint */
            endpoint = mef.createEndpoint(null);
            /* Return back to the resource adapter class */
            ra.endpointAvailable(endpoint);
        } catch (UnavailableException ex ) {
            log.info(ex.getMessage());
        }
    }

}
