package com.quickblox.q_municate.service;


import android.arch.lifecycle.LiveData;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.util.ArrayMap;
import android.util.Log;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.exception.QBChatException;
import com.quickblox.chat.listeners.QBChatDialogMessageListener;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;
import com.quickblox.q_municate.chat.ChatConnectionProvider;
import com.quickblox.q_municate.utils.LiveDataUtils;
import com.quickblox.q_municate_core.utils.ChatNotificationUtils;
import com.quickblox.q_municate_core.utils.ChatUtils;
import com.quickblox.q_municate_core.utils.DateUtilsCore;


import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.util.List;

class ChatConnection implements ChatConnectionProvider{

    private static final String TAG = ChatConnection.class.getSimpleName();
    private final IncomingMessageListenmer dialogMessageListener;
    private QBChatService chatService;
    private ChatDialogsManager dialogsManager;
    private List<QBChatDialog> chatDialogs;
    private ArrayMap<String, QBChatDialog> mapChatDialogs = new ArrayMap<>();
    private Handler handler = new Handler(Looper.getMainLooper());

    public ChatConnection(QBChatService chatService, ChatDialogsManager dialogsManager) {
        this.chatService = chatService;
        this.dialogsManager = dialogsManager;
        dialogMessageListener = new IncomingMessageListenmer();
    }

    public void start() {
        chatService.getIncomingMessagesManager().addDialogMessageListener(dialogMessageListener);
        joinDialogs();
    }

    public void stop(){
        leaveDialogs();
    }

    private void joinDialogs() {
        if (CollectionsUtil.isEmpty(chatDialogs)) {
            return;
        }
        for (QBChatDialog chatDialog : chatDialogs) {
            chatDialog.initForChat(chatService);
            if (QBDialogType.GROUP == chatDialog.getDialogType()) {
                chatDialog.join(null, null);
            }
        }
    }

    private void leaveDialogs(){
        if(CollectionsUtil.isEmpty(chatDialogs)){
            return;
        }
        for (QBChatDialog chatDialog : chatDialogs) {
            if (QBDialogType.GROUP == chatDialog.getDialogType()) {
                try {
                    chatDialog.leave();
                } catch (XMPPException | SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    @Override
    public void sendMessage(QBChatDialog chatDialog, QBMessage message) throws SmackException.NotConnectedException {
        long time = DateUtilsCore.getCurrentTime();
        message.setProperty(ChatNotificationUtils.PROPERTY_DATE_SENT, String.valueOf(time));
        message.setSaveToHistory(ChatNotificationUtils.VALUE_SAVE_TO_HISTORY);
        try {
            chatDialog.sendMessage(message);
        } catch ( SmackException.NotConnectedException e) {
            throw e;
        } finally {
            if (QBDialogType.PRIVATE.equals(chatDialog.getDialogType())) {
                dialogsManager.saveMessage(message);
                updateDialog(chatDialog, message);
            }
        }
    }

    @Override
    public LiveData<List<QBChatDialog>> loadDialogs(boolean forceLoad) {
        Log.d(TAG, "loading Dialogs");
        if(!CollectionsUtil.isEmpty(chatDialogs)) {
            return new LiveData<List<QBChatDialog>>() {
                @Override
                protected void onActive() {
                    super.onActive();
                    setValue(chatDialogs);
                }
            };
        }
        else {
            return loadDialogsFromManager();
        }
    }

    private LiveData<List<QBChatDialog>> loadDialogsFromManager() {
        LiveData<List<QBChatDialog>> listLiveData = dialogsManager.loadDialogs( true);

        handler.post( () -> {
            LiveDataUtils.observeValue(listLiveData, (loadedChatDialogs) -> {
                chatDialogs = loadedChatDialogs;
                Log.i(TAG, "chat user = " +chatService.getUser());
                    for (QBChatDialog qbChatDialog : chatDialogs) {
                        mapChatDialogs.put(qbChatDialog.getDialogId(), qbChatDialog);
                        Log.i(TAG, "ocupants:" + qbChatDialog.getOccupants());
                        qbChatDialog.initForChat(chatService);
                        qbChatDialog.join(null, null);
                    }
            });
        });
        return listLiveData;
    }

    @Override
    public LiveData<List<QMUser>> loadDialogData(String dlgId) {
        return dialogsManager.loadDialogData(dlgId);
    }

    @Override
    public LiveData<QBChatDialog> loadDialog(String dlgId) {
        if (!CollectionsUtil.isEmpty(mapChatDialogs)) {
            return new LiveData<QBChatDialog>() {
                @Override
                protected void onActive() {
                    setValue(findDiailog(dlgId));
                }
            };
        }
        return null;
    }

    private QBChatDialog findDiailog(String dialogId) {
        return mapChatDialogs.get(dialogId);
    }

    private void updateDialog(QBChatDialog chatDialog, QBChatMessage qbChatMessage) {
        chatDialog.setLastMessageDateSent(ChatUtils.getMessageDateSent(qbChatMessage));
        chatDialog.setLastMessage(qbChatMessage.getBody());
        dialogsManager.updateDialog(chatDialog);
    }

    class IncomingMessageListenmer implements QBChatDialogMessageListener {

        @Override
        public void processMessage(String dialogId, QBChatMessage qbChatMessage, Integer integer) {

            QBChatDialog chatDialog = findDiailog(dialogId);
            //Can receive message from unknown dialog only for QBDialogType.PRIVATE
            if (chatDialog == null) {
                chatDialog = ChatNotificationUtils.parseDialogFromQBMessage(App.getInstance(), qbChatMessage, QBDialogType.PRIVATE);
                ChatUtils.addOccupantsToQBDialog(chatDialog, qbChatMessage);
                dialogsManager.saveDialog(chatDialog);
            } else {
                updateDialog(chatDialog, qbChatMessage);
            }
            QBMessage message = new QBMessage(qbChatMessage);
            message.setDateSent(ChatUtils.getMessageDateSent(qbChatMessage));
            dialogsManager.saveMessage(message);
        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        }
    }




}
