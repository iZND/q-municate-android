package com.example.q_municate_chat_service.viewmodel;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.example.q_municate_chat_service.QBChatDilogRepository;
import com.quickblox.chat.model.QBChatDialog;

import java.util.List;

public class QbChatDialogListViewModel extends ViewModel {

    private LiveData<List<QBChatDialog>> dialogs = new MutableLiveData<>();
    private QBChatDilogRepository repository;

    public QbChatDialogListViewModel(QBChatDilogRepository repository){
        this.repository = repository;
        dialogs = repository.loadAll();
    }

    public LiveData<List<QBChatDialog>> getDialogs(){
        return dialogs;
    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        private QBChatDilogRepository repository;

        public Factory(QBChatDilogRepository repository){

            this.repository = repository;
        }


        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            return (T)new QbChatDialogListViewModel(repository);
        }
    }
}
