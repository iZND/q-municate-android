package com.quickblox.q_municate.service;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
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

    private LiveData<List<QBChatDialog>> dialogsiveData;
    private Observer<List<QBChatDialog>> dialogObserver;

    public ChatConnection(QBChatService chatService, ChatDialogsManager dialogsManager) {
        this.chatService = chatService;
        this.dialogsManager = dialogsManager;
        dialogMessageListener = new IncomingMessageListenmer();
        dialogObserver = new DialogObserver();
    }

    public void start() {
        chatService.getIncomingMessagesManager().addDialogMessageListener(dialogMessageListener);
        joinDialogs();
    }

    public void stop(){
        leaveDialogs();
    }

    public void destroy(){
        dialogsiveData.removeObserver(dialogObserver);
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
        message.setReadForOwner(true);
        try {
            chatDialog.sendMessage(message);
        } catch ( SmackException.NotConnectedException e) {
            throw e;
        } finally {
            if (QBDialogType.PRIVATE.equals(chatDialog.getDialogType())) {
                dialogsManager.saveMessage(message);
                updateDialog(chatDialog, message, true);
            }
        }
    }

    @Override
    public void markMessageRead(QBMessage message, QBChatDialog dialog) {
        Log.i(TAG, "markMessageRead" +message.getId());
        try {
            dialog.readMessage(message);
            message.setReadForOwner(true);
            Integer unreadMessageCount = dialog.getUnreadMessageCount();
            if (unreadMessageCount > 0) {
                unreadMessageCount = unreadMessageCount - 1;
                dialog.setUnreadMessageCount(unreadMessageCount);
                dialogsManager.updateDialog(dialog);
            }
            dialogsManager.updateMessage(message);
        } catch (XMPPException|SmackException.NotConnectedException  e) {
            e.printStackTrace();
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
        dialogsiveData = dialogsManager.loadDialogs( true);
        dialogsiveData.observeForever(dialogObserver);
        /*LiveDataUtils.observeValue(dialogsiveData, (loadedChatDialogs -> {
            chatDialogs = loadedChatDialogs;
            Log.i(TAG, "chat user = " +chatService.getUser());
            for (QBChatDialog qbChatDialog : chatDialogs) {
                mapChatDialogs.put(qbChatDialog.getDialogId(), qbChatDialog);
                Log.i(TAG, "ocupants:" + qbChatDialog.getOccupants());
                qbChatDialog.initForChat(chatService);
                qbChatDialog.join(null, null);
            }
        }));*/
        return dialogsiveData;
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

    private void updateDialog(QBChatDialog chatDialog, QBChatMessage qbChatMessage, boolean isOwnMessage) {
        chatDialog.setLastMessageDateSent(ChatUtils.getMessageDateSent(qbChatMessage));
        chatDialog.setLastMessage(qbChatMessage.getBody());
        if (!isOwnMessage) {
            chatDialog.setUnreadMessageCount(chatDialog.getUnreadMessageCount() + 1);
        }
        dialogsManager.updateDialog(chatDialog);
    }

    class DialogObserver implements Observer<List<QBChatDialog>>{

        @Override
        public void onChanged(@Nullable List<QBChatDialog> loadedChatDialogs) {
            boolean newDialogs = (chatDialogs == null);
            chatDialogs = loadedChatDialogs;
            Log.i(TAG, "chat user = " +chatService.getUser());
                for (QBChatDialog qbChatDialog : chatDialogs) {
                    if (newDialogs) {
                        mapChatDialogs.put(qbChatDialog.getDialogId(), qbChatDialog);
                    }
                    Log.i(TAG, "ocupants:" + qbChatDialog.getOccupants());
                    qbChatDialog.initForChat(chatService);
                    if (newDialogs) {
                        qbChatDialog.join(null, null);
                    }
                }
        }
    }

    class IncomingMessageListenmer implements QBChatDialogMessageListener {

        @Override
        public void processMessage(String dialogId, QBChatMessage qbChatMessage, Integer integer) {

            QBChatDialog chatDialog = findDiailog(dialogId);
            boolean ownMessage = qbChatMessage.getSenderId().equals(chatService.getUser().getId());
            Log.i(TAG, "processMessage isOwn"+ownMessage);
            //Can receive message from unknown dialog only for QBDialogType.PRIVATE
            if (chatDialog == null) {
                chatDialog = ChatNotificationUtils.parseDialogFromQBMessage(App.getInstance(), qbChatMessage, QBDialogType.PRIVATE);
                ChatUtils.addOccupantsToQBDialog(chatDialog, qbChatMessage);
                dialogsManager.saveDialog(chatDialog);
            } else {
                updateDialog(chatDialog, qbChatMessage, ownMessage);
            }
            QBMessage message = new QBMessage(qbChatMessage);
            message.setReadForOwner(ownMessage);
            message.setDateSent(ChatUtils.getMessageDateSent(qbChatMessage));
            dialogsManager.saveMessage(message);
        }

        @Override
        public void processError(String s, QBChatException e, QBChatMessage qbChatMessage, Integer integer) {

        }
    }




}
