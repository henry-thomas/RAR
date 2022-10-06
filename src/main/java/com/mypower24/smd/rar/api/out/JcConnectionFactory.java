/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mypower24.smd.rar.api.out;

import java.io.Serializable;
import javax.resource.Referenceable;
import javax.resource.ResourceException;

/**
 *
 * @author henry
 */
public interface JcConnectionFactory extends Serializable, Referenceable {

    public JcConnection getConnection(String host, int port) throws ResourceException;

}
