package com.quickblox.q_municate.chat;


import android.arch.lifecycle.LiveData;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.chat.model.QBChatDialog;

import org.jivesoftware.smack.SmackException;

import java.util.List;

public interface ChatConnectionProvider {

    LiveData<List<QBChatDialog>> loadDialogs(boolean forceLoad);

    LiveData<List<QMUser>> loadDialogData(String dlgId);

    LiveData<QBChatDialog> loadDialog(String dlgId);

    void sendMessage(QBChatDialog chatDialog, QBMessage message) throws SmackException.NotConnectedException;

}
