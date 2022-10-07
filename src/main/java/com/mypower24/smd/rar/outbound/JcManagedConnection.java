/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.outbound;

import com.mypower24.smd.rar.SmdResourceAdapter;
import com.mypower24.smd.rar.lib.JcMessage;
import com.mypower24.smd.rar.lib.JcServerDescriptor;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.PrintWriter;
import java.net.Socket;
import java.net.SocketTimeoutException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.ConnectionEvent;
import javax.resource.spi.ConnectionEventListener;
import javax.resource.spi.ConnectionRequestInfo;
import javax.resource.spi.LocalTransaction;
import javax.resource.spi.ManagedConnection;
import javax.resource.spi.ManagedConnectionMetaData;
import javax.resource.spi.ResourceAdapter;
import javax.security.auth.Subject;
import javax.transaction.xa.XAResource;

/**
 *
 * @author henry
 */
public class JcManagedConnection implements ManagedConnection {

    private static final Logger log = Logger.getLogger("JcManagedConnection");

    private JcConnectionImpl connection;
    JcManagedConnectionFactory mcf;
    private final Set<JcConnectionImpl> createdConnections;
    private final Socket socket;
    private final ObjectOutputStream obOut;
    private final ObjectInputStream obIn;
    private PrintWriter logwriter;
    private SmdResourceAdapter ra;
    private final List<ConnectionEventListener> listeners;
    private final String host;
    private final String port;

    private final Map<Integer, JcMessage> reqRespMap = new HashMap<>();
    private final Thread readThread;

    JcManagedConnection(String host, String port, ResourceAdapter ra, JcManagedConnectionFactory mcf) throws IOException {
        log.info("[JcManagedConnection] Constructor");
        createdConnections = new HashSet<>();
        listeners = Collections.synchronizedList(new ArrayList<>(1));

        this.mcf = mcf;
        /* EIS-specific procedure to obtain a new connection */
        int portnum = Integer.parseInt(port);
        log.info(String.format("Connecting to %s on port %s...", host, port));
        socket = new Socket(host, portnum);
//        socket.setSoTimeout(2000);
        obOut = new ObjectOutputStream(socket.getOutputStream());
        obIn = new ObjectInputStream(socket.getInputStream());
        this.ra = (SmdResourceAdapter) ra;

        readThread = new Thread(this::readTask);
        readThread.setName(JcManagedConnection.class.getSimpleName() + "-readThread");
        readThread.start();

        this.host = host;
        this.port = port;

//        in.readLine(); in.readLine();
        log.info("Connected!");
    }

    private void readTask() {
        while (!socket.isClosed()) {
            try {
                Object readObject = obIn.readObject();
                if (readObject instanceof JcMessage) {
                    JcMessage response = (JcMessage) readObject;
                    JcMessage request = reqRespMap.remove(response.getRequestId());
                    if (request != null) {
                        synchronized (request) {
                            request.setResponse(response);
                            request.notifyAll();
                        }
                    }
                }
            } catch (IOException ex) {
                try {
                    Thread.sleep(500);
                    destroy();
                    Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
                } catch (InterruptedException | ResourceException ex1) {
                    Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex1);
                }
            } catch (ClassNotFoundException ex) {
                Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
            }
        }

    }

    public JcMessage sendCommandToServer(JcMessage request) throws ResourceException {
        try {
            obOut.writeObject(request);
            reqRespMap.put(request.getRequestId(), request);

            synchronized (request) {
                request.wait(2000);
            }

            if (request.getResponse() == null) {
                throw new IOException("No response received, timeout");
            }

            return request.getResponse();

        } catch (IOException ex) {
            Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
        } catch (InterruptedException ex) {
            Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public Object getConnection(Subject subject, ConnectionRequestInfo cxRequestInfo) throws ResourceException {
        try {
            //log.info("[JcManagedConnection] getConnection()");
            connection = new JcConnectionImpl(this);
            //port and serverId below should be set with activation, used for MDB Activation
            JcLocalTransaction trans = (JcLocalTransaction) getLocalTransaction();
            trans.setIsComplete(false);

            JcServerDescriptor jcServerDescriptor = new JcServerDescriptor("RATest-1", socket.getInetAddress().getHostAddress(), String.valueOf(socket.getPort()));
            JcMessage req = new JcMessage();
            req.setCommand("Hello");
            req.setData(jcServerDescriptor);

            JcMessage send = sendCommandToServer(req);

            createdConnections.add(connection);
            return connection;
        } catch (Exception ex) {
            Logger.getLogger(JcManagedConnection.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    @Override
    public void destroy() throws ResourceException {
        try {
            log.info("[JcManagedConnection] destroy()");
            socket.close();
            cleanup();
        } catch (IOException e) {
            log.log(Level.WARNING, "[JcManagedConnection] destroy() failed: {0}", e.getMessage());
        }
    }

    @Override
    public void cleanup() throws ResourceException {
//        log.info("[JcManagedConnection] cleanup()");
        for (JcConnectionImpl con : createdConnections) {
            if (con != null) {
                con.invalidate();
                con.setManagedConnection(null);
            }
        }
    }

    public void closeHandle(JcConnectionImpl handle) {
        createdConnections.remove((JcConnectionImpl) handle);
        ConnectionEvent event = new ConnectionEvent(this, ConnectionEvent.CONNECTION_CLOSED);
        event.setConnectionHandle(handle);
        for (ConnectionEventListener cel : listeners) {
            cel.connectionClosed(event);
        }

    }

    @Override
    public void associateConnection(Object connection) throws ResourceException {
        log.info("[JcManagedConnection] associateConnection()");
        if (connection instanceof JcManagedConnection) {
            this.connection = (JcConnectionImpl) connection;
            this.connection.setManagedConnection(this);
        } else {
            throw new ResourceException("Not supported : associating connection instance of " + connection.getClass().getName());
        }
    }

    public void disassociateConnection() {
        this.connection = null;
    }

    @Override
    public void addConnectionEventListener(ConnectionEventListener listener) {
        log.info("[JcManagedConnection] addConnectionEventListener");
        listeners.add(listener);
    }

    @Override
    public void removeConnectionEventListener(ConnectionEventListener listener) {
        log.info("[JcManagedConnection] removeConnectionEventListener");
        listeners.remove(listener);
    }

    @Override
    public XAResource getXAResource() throws ResourceException {
        log.info("[JcManagedConnection] getXAResource");
        return null;
    }

    @Override
    public LocalTransaction getLocalTransaction() throws ResourceException {
//        log.info("[JcManagedConnection] getLocalTransaction");
        return new JcLocalTransaction(this, connection);
    }

    @Override
    public ManagedConnectionMetaData getMetaData() throws ResourceException {
        return new ManagedConnectionMetaData() {
            @Override
            public String getEISProductName() throws ResourceException {
                return "JCluster ";
            }

            @Override
            public String getEISProductVersion() throws ResourceException {
                return "0.0.1";
            }

            @Override
            public int getMaxConnections() throws ResourceException {
                return 32;
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
}
