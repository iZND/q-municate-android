package com.quickblox.q_municate.ui.viewmodel;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;
import com.quickblox.q_municate.chat.ChatConnectionProvider;
import com.quickblox.q_municate.ui.fragments.chats.QbChatDialogListViewModel;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.util.Log;

import javax.inject.Inject;

public class QBChatMessageViewModel extends ViewModel {
    private static final String TAG = QbChatDialogListViewModel.class.getSimpleName();
    private LiveData<List<QBMessage>> messages = new MutableLiveData<>();

    @Inject
    ChatDialogsManager dialogsManager;

    public LiveData<List<QMUser>> participants;

    public LiveData<List<QBMessage>> chatMessages;

    public LiveData<QBChatDialog> chatDialog;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();
    private String dlgId;
    private ChatConnectionProvider chatConnectionProvider;
    ;


    public QBChatMessageViewModel(String dlgId){
        this.dlgId = dlgId;
    }

    private void init() {
        /*loadDialog(dlgId);
        loadDialogData(dlgId);
        loadMessages();*/
    }

    public LiveData<QBChatDialog> loadDialog(String dlgId) {
        return chatConnectionProvider.loadDialog(dlgId);
    }

    public LiveData<List<QMUser>> loadDialogData(String dlgId){
        return chatConnectionProvider.loadDialogData(dlgId);
    }

    public LiveData<List<QBMessage>> loadMessages(){
        Log.i(TAG, "load messages");
        return dialogsManager.loadMessages(dlgId, true);
    }

    public LiveData<List<QMUser>> loadUsersInDialog(QBChatDialog dialog){
        return dialogsManager.loadUsersInDialog(dialog);
    }

    public void setChatConnectionProvider(ChatConnectionProvider chatConnectionProvider) {
        this.chatConnectionProvider = chatConnectionProvider;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private String dlgId;

        public Factory(String dlgId){
            this.dlgId = dlgId;
        }

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            QBChatMessageViewModel qbChatMessageViewModel = new QBChatMessageViewModel(dlgId);
            App.getInstance().getComponent().inject(qbChatMessageViewModel);
            qbChatMessageViewModel.init();
            return (T) qbChatMessageViewModel;
        }
    }


}
