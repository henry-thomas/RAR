/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mypower24.smd.rar.api.in;

import com.mypower24.smd.rar.api.in.SmdConnection;
import javax.resource.ResourceException;

/**
 *
 * @author henry
 */
public interface SmdConnectionFactory {

    public SmdConnection getConnection(String host, int port) throws ResourceException;
}
