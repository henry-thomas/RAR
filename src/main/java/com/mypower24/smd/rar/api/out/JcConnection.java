/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Interface.java to edit this template
 */
package com.mypower24.smd.rar.api.out;

import com.mypower24.smd.rar.lib.JcMessage;
import javax.resource.ResourceException;

/**
 *
 * @author henry
 */
public interface JcConnection {

    public JcMessage send(JcMessage req) throws Exception;

    public void close() throws ResourceException;
}
