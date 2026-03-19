package com.example.oltmanager;

public class Ont {
    private String sn;
    private String ponPort;
    private String status;

    public Ont(String sn, String ponPort, String status) {
        this.sn = sn;
        this.ponPort = ponPort;
        this.status = status;
    }

    public String getSn() {
        return sn;
    }

    public void setSn(String sn) {
        this.sn = sn;
    }

    public String getPonPort() {
        return ponPort;
    }

    public void setPonPort(String ponPort) {
        this.ponPort = ponPort;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }
}