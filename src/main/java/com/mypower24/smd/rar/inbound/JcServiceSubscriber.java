/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.inbound;

import com.mypower24.smd.rar.lib.JcMessage;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;

/**
 *
 * @author henry
 */
public class JcServiceSubscriber implements Work {

    private static final Logger log = Logger.getLogger("DnsServiceSubscriber");
    private final MessageEndpoint mdb;
    private final SmdActivationSpec spec;
    private Socket socket;
    private volatile boolean running;

    public JcServiceSubscriber(MessageEndpoint mdb, SmdActivationSpec spec) {
        this.mdb = mdb;
        this.spec = spec;
        running = true;
    }

    @Override
    public void release() {
        log.info("[DnsServiceSubscriber] release()");
        try {
            running = false;
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
        }
    }

    /* Invoke a method from the MDB */
    private String callMdb(MessageEndpoint mdb, Method command, JcMessage message)
            throws ResourceException {
        String resp;
        try {
            log.info("[DnsServiceSubscriber] callMdb()");
            mdb.beforeDelivery(command);
            Object ret = command.invoke(mdb, message);
            resp = (String) ret;
        } catch (NoSuchMethodException | ResourceException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            log.info(String.format("Invocation error %s", ex.getMessage()));
            resp = "ERROR Invocation error - " + ex.getMessage();
        }
        mdb.afterDelivery();
        return resp;
    }

    /* Invoke a method from the MDB */
    private String callMdb(MessageEndpoint mdb, Method command, String... params)
            throws ResourceException {
        String resp;
        try {
            log.info("[DnsServiceSubscriber] callMdb()");
            mdb.beforeDelivery(command);
            Object ret = command.invoke(mdb, (Object[]) params);
            resp = (String) ret;
        } catch (NoSuchMethodException | ResourceException
                | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException ex) {
            log.info(String.format("Invocation error %s", ex.getMessage()));
            resp = "ERROR Invocation error - " + ex.getMessage();
        }
        mdb.afterDelivery();
        return resp;
    }

    @Override
    public void run() {
        ObjectInputStream ois;
        JcMessage jcMessage;
        String command;

        try {
            /* Connect to the JCluster EIS */
            int port = Integer.parseInt(spec.getPort());
            log.info("[DnsServiceSubscriber] Connecting...");
            socket = new Socket("localhost", port);
            ois = new ObjectInputStream(socket.getInputStream());
            log.info("[DnsServiceSubscriber] Connected");

            while (running) {
                jcMessage = (JcMessage) ois.readObject();
                command = jcMessage.getCommand();
                /* Does the MDB support this message? */
                if (spec.getCommands().containsKey(command)) {
                    Method mdbMethod = spec.getCommands().get(command);
                    /* Invoke the method of the MDB */
                    callMdb(mdb, mdbMethod, jcMessage);
                } else {
                    log.info("[DnsServiceSubscriber] Unknown message");
                }

            }
        } catch (IOException | ResourceException ex) {
            log.log(Level.INFO, "[DnsServiceSubscriber] Error - {0}", ex.getMessage());
        } catch (ClassNotFoundException ex) {
            Logger.getLogger(JcServiceSubscriber.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

}
