package com.parse.starter.models;

/**
 * Created by USER on 7/8/2018.
 */


public class ChatModel {
    public String message;
    public boolean isSend;
    public String time;

    public ChatModel(String message, boolean isSend,String time) {
        this.message = message;
        this.isSend = isSend;
        this.time = time;
    }

    public String getTime() {
        return time;
    }

    public void setTime(String time) {
        this.time = time;
    }

    public ChatModel() {
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public boolean isSend() {
        return isSend;
    }

    public void setSend(boolean send) {
        isSend = send;
    }
}
