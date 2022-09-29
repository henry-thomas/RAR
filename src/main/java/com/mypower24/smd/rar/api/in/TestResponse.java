/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.api.in;

/**
 *
 * @author henry
 */
public class TestResponse {

    private int messageId;
    private String messageDesc;
    private static int MSG_ID_INCR = 0;

    public TestResponse(String messageDesc) {
        this.messageDesc = messageDesc;
        this.messageId = MSG_ID_INCR;
        MSG_ID_INCR++;
    }

    public int getMessageId() {
        return messageId;
    }

    public void setMessageId(int messageId) {
        this.messageId = messageId;
    }

    public String getMessageDesc() {
        return messageDesc;
    }

    public void setMessageDesc(String messageDesc) {
        this.messageDesc = messageDesc;
    }

}
