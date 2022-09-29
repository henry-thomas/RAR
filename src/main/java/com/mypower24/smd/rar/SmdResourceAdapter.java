/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar;

import com.mypower24.smd.rar.inbound.SmdServiceSubscriber;
import com.mypower24.smd.rar.inbound.ObtainEndpointWork;
import com.mypower24.smd.rar.inbound.SmdActivationSpec;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.BootstrapContext;
import javax.resource.spi.Connector;
import javax.resource.spi.ResourceAdapter;
import javax.resource.spi.ResourceAdapterInternalException;
import javax.resource.spi.TransactionSupport;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.endpoint.MessageEndpointFactory;
import javax.resource.spi.work.Work;
import javax.resource.spi.work.WorkException;
import javax.resource.spi.work.WorkManager;
import javax.transaction.xa.XAResource;

/**
 *
 * @author henry
 */
@Connector(
        displayName = "SmdResourceAdapter",
        vendorName = "SolarMD",
        version = "1.0",
        licenseRequired = false,
        description = "Point to point messaging implementation",
        transactionSupport = TransactionSupport.TransactionSupportLevel.NoTransaction
)
public class SmdResourceAdapter implements ResourceAdapter {

    private static final Logger log = Logger.getLogger("SmdResourceAdapter");
    private WorkManager workManager;
    private SmdActivationSpec tSpec;
    private Work tSubscriber;

    @Override
    public void start(BootstrapContext ctx) throws ResourceAdapterInternalException {
        log.info("[SmdResourceAdapter] start()");
        /* Get the work manager from the container to submit tasks to
         * be executed in container-managed threads */
        workManager = ctx.getWorkManager();
    }

    @Override
    public void stop() {
        log.info("[SmdResourceAdapter] stop()");
    }

    @Override
    public void endpointActivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) throws ResourceException {
        log.info("[SmdResourceAdapter] endpointActivation()");
        tSpec = (SmdActivationSpec) spec;
        /* New in JCA 1.7 - Get the endpoint class implementation (i.e. the
         * MDB class). This allows looking at the MDB implementation for
         * annotations. */
        Class endpointClass = endpointFactory.getEndpointClass();
        tSpec.setBeanClass(endpointClass);
        tSpec.findCommandsInMDB();

        /* MessageEndpoint msgEndpoint = endpointFactory.createEndpoint(null);
         * but we need to do that in a different thread, otherwise the MDB
         * never deploys. */
        ObtainEndpointWork work = new ObtainEndpointWork(this, endpointFactory);
        workManager.scheduleWork(work);
    }

    /* Called from ObtainEndpoint work after obtaining the endpoint */
    public void endpointAvailable(MessageEndpoint endpoint) {

        try {
            /* Start the traffic subscriber client in a new thread */
            tSubscriber = new SmdServiceSubscriber(endpoint, tSpec);
            workManager.scheduleWork(tSubscriber);
        } catch (WorkException e) {
            log.info("[SmdResourceAdapter] Can't start the subscriber");
            log.info(e.getMessage());
        }
    }

    @Override
    public void endpointDeactivation(MessageEndpointFactory endpointFactory, ActivationSpec spec) {
        log.info("[SmdResourceAdapter] endpointDeactivation()");
        /* Stop listening */
        tSubscriber.release();
    }

    @Override
    public XAResource[] getXAResources(ActivationSpec[] ass) throws ResourceException {
        log.info("[SmdResourceAdapter] getXAResources()");
        return null;
    }

}
