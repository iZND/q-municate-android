package com.quickblox.q_municate.ui.fragments.chats;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;

import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

public class QbChatDialogListViewModel extends ViewModel {

    private static final String TAG = QbChatDialogListViewModel.class.getSimpleName();
    private LiveData<List<QBChatDialog>> dialogs = new MutableLiveData<>();

    @Inject
    ChatDialogsManager repository;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public QbChatDialogListViewModel(){

    }

    private void initContactlist(){

    }

    public LiveData<List<QBChatDialog>> getDialogs() {
        return repository.loadDialogs(false);
    }

    public void removeDialog(final QBChatDialog dialog){
        ioExecuotr.execute(()->  {
                repository.delete(dialog);
            }
        );

    }

    public static class Factory extends ViewModelProvider.NewInstanceFactory {

        @Override
        public <T extends ViewModel> T create(Class<T> modelClass) {
            QbChatDialogListViewModel qbChatDialogListViewModel = new QbChatDialogListViewModel();
            App.getInstance().getComponent().inject(qbChatDialogListViewModel);
            return (T) qbChatDialogListViewModel;
        }
    }
}
