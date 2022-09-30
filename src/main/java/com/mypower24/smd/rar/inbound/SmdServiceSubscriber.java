/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.inbound;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.net.Socket;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.json.Json;
import javax.json.stream.JsonParser;
import javax.resource.ResourceException;
import javax.resource.spi.endpoint.MessageEndpoint;
import javax.resource.spi.work.Work;

/**
 *
 * @author henry
 */
public class SmdServiceSubscriber implements Work {

    private static final Logger log = Logger.getLogger("DnsServiceSubscriber");
    private MessageEndpoint mdb;
    private SmdActivationSpec spec;
    private Socket socket;
    private volatile boolean listen;

    public SmdServiceSubscriber(MessageEndpoint mdb, SmdActivationSpec spec) {
        this.mdb = mdb;
        this.spec = spec;
        listen = true;
    }

    @Override
    public void release() {
        log.info("[DnsServiceSubscriber] release()");
        try {
            listen = false;
            if (socket != null) {
                socket.close();
            }
        } catch (IOException ex) {
        }
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
        BufferedReader in;
        String jsonLine;
        String key;
        JsonParser parser;

        try {
            /* Connect to the traffic EIS */
            int port = Integer.parseInt(spec.getPort());
            log.info("[DnsServiceSubscriber] Connecting...");
            socket = new Socket("localhost", port);
            in = new BufferedReader(new InputStreamReader(socket.getInputStream()));
            log.info("[DnsServiceSubscriber] Connected");

            while (listen) {
                jsonLine = in.readLine();
                parser = Json.createParser(new StringReader(jsonLine));
                if (parser.hasNext() && parser.next() == JsonParser.Event.START_OBJECT
                        && parser.hasNext() && parser.next() == JsonParser.Event.KEY_NAME) {

                    key = parser.getString();
                    /* Does the MDB support this message? */
                    if (spec.getCommands().containsKey(key)) {
                        Method mdbMethod = spec.getCommands().get(key);
                        /* Invoke the method of the MDB */
                        callMdb(mdb, mdbMethod, jsonLine);
                    } else {
                        log.info("[DnsServiceSubscriber] Unknown message");
                    }
                } else {
                    log.info("[DnsServiceSubscriber] Wrong message format");
                }

            }
        } catch (IOException | ResourceException ex) {
            log.log(Level.INFO, "[DnsServiceSubscriber] Error - {0}", ex.getMessage());
        }
    }

}
