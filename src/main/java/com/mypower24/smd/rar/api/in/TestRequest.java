/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package com.mypower24.smd.rar.api.in;

/**
 *
 * @author henry
 */
public class TestRequest {

    private final int requestId;
    private String reqHeader;

    private static int MSG_ID_INCR = 0;

    public TestRequest() {
        this.requestId = MSG_ID_INCR;
        MSG_ID_INCR++;
    }

    public int getRequestId() {
        return requestId;
    }

    public String getReqHeader() {
        return reqHeader;
    }

    public void setReqHeader(String reqHeader) {
        this.reqHeader = reqHeader;
    }

}
