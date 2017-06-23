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
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate.App;
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
    QBChatDilogRepository repository;

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();

    public QbChatDialogListViewModel(){

    }

    private void intDialogs(){
        dialogs = repository.loadAll();
        dialogs.observeForever(new Observer<List<QBChatDialog>>() {
            @Override
            public void onChanged(final @Nullable List<QBChatDialog> qbChatDialogs) {
                        initContactlist();
            }
        });
    }

    private void initContactlist(){

        App.getInstance().getContactRepo().loadAll().observeForever(new Observer<List<ContactItem>>() {
            @Override
            public void onChanged(final @Nullable List<ContactItem> contactItemList) {

                ioExecuotr.execute(new Runnable() {
                    @Override
                    public void run() {
                        Collection<Integer> friendIdsList = new ArrayList<>(contactItemList.size());
                        for (ContactItem contactItem : contactItemList) {
                            friendIdsList.add(contactItem.getUserId());
                        }
                        QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                        requestBuilder.setPage(ConstsCore.USERS_PAGE_NUM);
                        requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);
                        try {
                            List<QMUser> usersByIDsSync = QMUserService.getInstance().getUsersByIDsSync(friendIdsList, requestBuilder);
                            ArrayList<Friend> friends = new ArrayList<>(usersByIDsSync.size());
                            for (QMUser qmUser : usersByIDsSync) {
                                friends.add(new Friend(qmUser));
                            }

                            DataManager.getInstance().getFriendDataManager().createOrUpdateAll(friends);

                        } catch (QBResponseException e) {
                            e.printStackTrace();
                        }
                    };
                });
            }
        });
    }

    public LiveData<List<QBChatDialog>> getDialogs() {
        intDialogs();
        return dialogs;
    }

    public void removeDialog(final QBChatDialog dialog){
        ioExecuotr.execute(new Runnable() {
            @Override
            public void run() {
                repository.delete(dialog);
            }
        });

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
