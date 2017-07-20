package com.example.q_municate_chat_service.entity;



public abstract class NotificationType {

    protected String notificationType;

    protected int internalType;

    protected String info;

    protected String body;

    public String getBody(){
        return body;
    }

}
