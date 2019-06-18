package com.tulun.bean;

public class OfflineUser {
    private int id;
    private String toName;
    private String fromName;
    private int msgType;
    private String msg;
    private int state;
    public OfflineUser(){}

    public OfflineUser(String toName, String fromName, int msgType, String msg, int state) {
        this.toName = toName;
        this.fromName = fromName;
        this.msgType = msgType;
        this.msg = msg;
        this.state = state;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getToName() {
        return toName;
    }

    public void setToName(String toName) {
        this.toName = toName;
    }

    public String getFromName() {
        return fromName;
    }

    public void setFromName(String fromName) {
        this.fromName = fromName;
    }

    public int getMsgType() {
        return msgType;
    }

    public void setMsgType(int msgType) {
        this.msgType = msgType;
    }

    public String getMsg() {
        return msg;
    }

    public void setMsg(String msg) {
        this.msg = msg;
    }

    public int getState() {
        return state;
    }

    public void setState(int state) {
        this.state = state;
    }

    @Override
    public String toString() {
        return "Offline_msg [ to_name=" + toName + ", from_name=" + fromName + ", msg=" + msg + ",msg_type="+msgType +",state="+state+"]";
    }
}
