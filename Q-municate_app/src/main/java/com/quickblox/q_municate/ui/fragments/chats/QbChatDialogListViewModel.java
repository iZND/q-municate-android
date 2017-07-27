package com.quickblox.q_municate.ui.fragments.chats;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.QBChatDilogRepository;
import com.example.q_municate_chat_service.entity.ContactItem;
import com.example.q_municate_chat_service.repository.BaseRepo;
import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.RepositoryManager;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_core.utils.FinderUnknownUsers;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

import javax.inject.Inject;

import rx.Scheduler;
import rx.schedulers.Schedulers;

public class QbChatDialogListViewModel extends ViewModel {

    private static final String TAG = QbChatDialogListViewModel.class.getSimpleName();
    private LiveData<List<QBChatDialog>> dialogs = new MutableLiveData<>();

    @Inject
    RepositoryManager repository;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public QbChatDialogListViewModel(){

    }

    private void initContactlist(){

    }

    public LiveData<List<QBChatDialog>> getDialogs() {
        return repository.loadDialogs();
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
