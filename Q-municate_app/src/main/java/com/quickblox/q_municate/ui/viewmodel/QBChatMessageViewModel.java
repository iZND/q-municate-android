package com.quickblox.q_municate.ui.viewmodel;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;

import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;
import com.quickblox.q_municate.ui.fragments.chats.QbChatDialogListViewModel;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.Executor;
import java.util.concurrent.Executors;
import android.arch.lifecycle.ViewModel;
import android.arch.lifecycle.ViewModelProvider;
import android.databinding.ObservableArrayList;
import android.databinding.ObservableField;
import android.databinding.ObservableList;
import android.util.Pair;

import javax.inject.Inject;

import rx.Observer;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class QBChatMessageViewModel extends ViewModel {
    private static final String TAG = QbChatDialogListViewModel.class.getSimpleName();
    private LiveData<List<QBMessage>> messages = new MutableLiveData<>();

    @Inject
    ChatDialogsManager dialogsManager;

    public final ObservableField<Pair<QBChatDialog, List<QMUser>>> chatDialogData =
            new ObservableField<>();

    List<QMUser> participants;

    public ObservableList<QBMessage> chatMessages = new ObservableArrayList<>();

    private Executor ioExecuotr = Executors.newSingleThreadExecutor();
    private String dlgId;

    public QBChatMessageViewModel(String dlgId){
        this.dlgId = dlgId;
    }

    public void loadDialogById(String dlgId){
        dialogsManager.loadDialogData(dlgId).subscribeOn(Schedulers.io()).observeOn(AndroidSchedulers.mainThread())
                .subscribe(new rx.Observer<Pair<QBChatDialog, List<QMUser>>>() {
                    @Override
                    public void onCompleted() {

                    }

                    @Override
                    public void onError(Throwable e) {

                    }

                    @Override
                    public void onNext(Pair<QBChatDialog, List<QMUser>> dialogData) {
                        chatDialogData.set(dialogData);
                    }
                });;
    }

    public void loadMessages(){
        dialogsManager.loadMessages(dlgId, true)
                .subscribeOn(Schedulers.io())
                .observeOn(AndroidSchedulers.mainThread()).subscribe(new Observer<ArrayList<QBMessage>>() {
            @Override
            public void onCompleted() {

            }

            @Override
            public void onError(Throwable e) {

            }

            @Override
            public void onNext(ArrayList<QBMessage> qbMessages) {
                chatMessages.addAll(qbMessages);
            }
        });
    }

    public LiveData<List<QMUser>> loadUsersInDialog(QBChatDialog dialog){
        return dialogsManager.loadUsersInDialog(dialog);
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
            return (T) qbChatMessageViewModel;
        }
    }
}
