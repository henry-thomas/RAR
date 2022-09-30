/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import com.mypower24.smd.rar.lib.TestRequest;
import com.mypower24.smd.rar.lib.TestResponse;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.work.Work;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 * @author henry
 */
public class SmdManagedConnection implements ManagedConnection, Work {

    private static final Logger log = Logger.getLogger("SmdManagedConnection");

    private SmdConnectionImpl connection;
    private final List<SmdConnectionImpl> createdConnections;
    private final Socket socket;
    private final ObjectOutputStream obOut;
    private final ObjectInputStream obIn;
    private PrintWriter logwriter;

    SmdManagedConnection(String host, String port) throws IOException {

        log.info("[SmdManagedConnection] Constructor");
        createdConnections = new ArrayList<>();

        /* EIS-specific procedure to obtain a new connection */
        int portnum = Integer.parseInt(port);
        log.info(String.format("Connecting to %s on port %s...", host, port));
        socket = new Socket(host, portnum);
        obOut = new ObjectOutputStream(socket.getOutputStream());
        obIn = new ObjectInputStream(socket.getInputStream());

//        in.readLine(); in.readLine();
        log.info("Connected!");
    }

    public TestResponse sendCommandToServer(TestRequest command) throws IOException, ClassNotFoundException {
        obOut.writeObject(command);
        Object readObject = obIn.readObject();
        log.log(Level.INFO, "[SmdManagedConnection] getConnection(): {0}", readObject.toString());
        return (TestResponse) readObject;
//        return in.readLine();
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        try {
            log.info("[SmdManagedConnection] getConnection()");
            connection = new SmdConnectionImpl(this);
            TestRequest req = new TestRequest();
            req.setReqHeader("Hello");
            connection.send(req);
            return connection;
        } catch (Exception ex) {
            Logger.getLogger(SmdManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void destroy() throws ResourceException {
        try {
            log.info("[SmdManagedConnection] destroy()");
            socket.close();
        } catch (IOException e) {
            log.log(Level.WARNING, "[SmdManagedConnection] destroy() failed: {0}", e.getMessage());
        }
    }

    @Override
    public void cleanup() throws ResourceException {
        for (SmdConnectionImpl con : createdConnections) {
            if (con != null) {
                con.invalidate();
            }
        }
    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        log.info("[SmdManagedConnection] associateConnection()");
        this.connection = (SmdConnectionImpl) connection;
        this.connection.setManagedConnection(this);
    }

    public void disassociateConnection() {
        this.connection = null;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        log.info("[SmdManagedConnection] addConnectionEventListener");
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.info("[SmdManagedConnection] removeConnectionEventListener");
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
        return null;
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return "smd-mypower24-platform";
            }

            @Override
            public String getEISProductVersion() throws ResourceException {
                return "1.0.0";
            }

            @Override
            public int getMaxConnections() throws ResourceException {
                return 5;
            }

            @Override
            public String getUserName() throws ResourceException {
                return "Henry";
            }
        };
    }

    @Override
    public void setLogWriter(PrintWriter out) throws ResourceException {
        this.logwriter = out;
    }

    @Override
    public PrintWriter getLogWriter() throws ResourceException {
        return logwriter;
    }

    @Override
    public void release() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

    @Override
    public void run() {
        throw new UnsupportedOperationException("Not supported yet."); // Generated from nbfs://nbhost/SystemFileSystem/Templates/Classes/Code/GeneratedMethodBody
    }

}
