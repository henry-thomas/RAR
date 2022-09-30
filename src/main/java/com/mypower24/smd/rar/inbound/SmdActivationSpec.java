/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.inbound;

import com.mypower24.smd.rar.api.in.SmdCommand;
import com.mypower24.smd.rar.api.in.SmdMsgListener;
import java.io.Serializable;
import java.lang.reflect.Method;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;
import javax.resource.ResourceException;
import javax.resource.spi.Activation;
import javax.resource.spi.ActivationSpec;
import javax.resource.spi.ConfigProperty;
import javax.resource.spi.InvalidPropertyException;
import javax.resource.spi.ResourceAdapter;

/**
 *
 * @author henry
 */
/* The activation spec used by the MDB to configure the resource adapter */
@Activation(
        messageListeners = {SmdMsgListener.class}
)
public class SmdActivationSpec implements ActivationSpec, Serializable {

    private ResourceAdapter ra;
    @ConfigProperty()
    private String port;
    private Class beanClass;
    private Map<String, Method> commands = new HashMap<>();
    private static final long serialVersionUID = 1674967719558213103L;
    private static final Logger log = Logger.getLogger("SmdActivationSpec");

    /* Port is set by the MDB using @ActivationConfigProperty */
    public String getPort() {
        return port;
    }

    public void setPort(String port) {
        this.port = port;
    }

    /* Set from the RA class and accessed by the traffic subscriber thread */
    public void setBeanClass(Class c) {
        beanClass = c;
    }

    public Class getBeanClass() {
        return beanClass;
    }

    public Map<String, Method> getCommands() {
        return commands;
    }

    /* Inspect the MDB class for methods with a custom annotation.
     * This allows the MDB business interface to be emtpy */
    public void findCommandsInMDB() {
        log.info("[SmdActivationSpec] findCommandsInMDB()");
        for (Method method : beanClass.getMethods()) {
            if (method.isAnnotationPresent(SmdCommand.class)) {
                SmdCommand tCommand = method.getAnnotation(SmdCommand.class);
                commands.put(tCommand.name(), method);
            }
        }

        if (commands.isEmpty()) {
            log.info("No command annotations in MDB.");
        }

        for (Method m : commands.values()) {
            for (Class c : m.getParameterTypes()) {
                if (c != String.class) {
                    log.info("Command args must be String.");
                }
            }
        }
    }

    @Override
    public void validate() throws InvalidPropertyException {
        log.info("[SmdActivationSpec] validate()");
    }

    @Override
    public ResourceAdapter getResourceAdapter() {
        return ra;
    }

    @Override
    public void setResourceAdapter(ResourceAdapter ra) throws ResourceException {
        log.info("[SmdActivationSpec] setResourceAdapter()");
        this.ra = ra;
    }

}
