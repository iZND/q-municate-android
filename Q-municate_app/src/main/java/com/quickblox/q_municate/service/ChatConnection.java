package com.quickblox.q_municate.service;


import com.quickblox.chat.QBChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatMessage;

class ChatConnection {

    private final IncomingMessageListenmer dialogMessageListener;
    private QBChatService chatService;

    public ChatConnection(QBChatService chatService) {
        this.chatService = chatService;
        dialogMessageListener = new IncomingMessageListenmer();
    }

    public void start() {
        chatService.getIncomingMessagesManager().addDialogMessageListener(dialogMessageListener);
    }

    class IncomingMessageListenmer implements QBChatDialogMessageListener {

        @Override
        public void processMessage(String s, QBChatMessage qbChatMessage, Integer integer) {

        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        }
    }
}
