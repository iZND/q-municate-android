package com.quickblox.q_municate.ui.viewmodel;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.ui.fragments.chats.QbChatDialogListViewModel;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import javax.inject.Inject;

public class QBChatMessageViewModel extends ViewModel {
    private static final String TAG = QbChatDialogListViewModel.class.getSimpleName();
    private LiveData<List<QBMessage>> messages = new MutableLiveData<>();

    @Inject
    QBMessageRepo repository;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public QBChatMessageViewModel(){

    }

    public LiveData<List<QBMessage>> getMessages(String dlgId) {
        return repository.loadAll(dlgId);
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            QBChatMessageViewModel qbChatMessageViewModel = new QBChatMessageViewModel();
            App.getInstance().getComponent().inject(qbChatMessageViewModel);
            return (T) qbChatMessageViewModel;
        }
    }
}
