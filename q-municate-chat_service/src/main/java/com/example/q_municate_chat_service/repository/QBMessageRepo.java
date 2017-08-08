package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.util.RxUtils;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;


public class QBMessageRepo extends BaseRepoImpl<QBMessage> implements BaseRepo<QBMessage, String>{

    private static final String TAG = QBMessageRepo.class.getSimpleName();

    QBMessageDao messageDao;
    private String dialogId;

    public QBMessageRepo(QBMessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public void create(QBMessage event) {

    }

    @Override
    public LiveData<List<QBMessage>> loadAll() {
        return null;
    }

    public LiveData<List<QBMessage>> loadAll(String dialogId, boolean forceLoad) {
        Log.i(TAG, "loadAll dialogId="+dialogId);
        /*if (!forceLoad) {
            return RxUtils.makeObservable( new ArrayList<>(messageDao.getAllByDialog(dialogId)));
        } else {
            QBChatDialog chatDialog = new QBChatDialog(dialogId);
            return RxUtils.makeObservable(QBRestChatService.
                    getDialogMessages(chatDialog, null))
                    .switchMap( (qbChatMessages) -> {
                                       ArrayList<QBMessage> messages = parseMessage(qbChatMessages);
                                       return Observable.just(messages);
                                   });

        }*/
        this.dialogId = dialogId;
        final LiveData<List<QBMessage>> dbSource = messageDao.getAllByDialog(dialogId);
        if (!forceLoad) {
            Log.i(TAG, "return result from db");
            return dbSource;
        }
        result.addSource(dbSource, new Observer<List<QBMessage>>() {
            @Override
            public void onChanged(@Nullable List<QBMessage> data) {
                Log.i(TAG, "onChanged from db source");


                if (shouldFetch(data)) {
                    result.removeSource(dbSource);
                    Log.i(TAG, "onChanged from db source :shouldFetch");
                    fetchFromNetwork(dbSource);
                } else {
                    result.setValue(data);
                }
            }
        });
        return result;
    }

    @Override
    public LiveData<QBMessage> loadById(String s) {
        return null;
    }

    @Override
    public void delete(QBMessage event) {

    }

    @Override
    protected void performApiReuqest() {
        QBChatDialog chatDialog = new QBChatDialog(dialogId);
        QBRestChatService.getDialogMessages(chatDialog, null).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
            @Override
            public void onSuccess(ArrayList<QBChatMessage> qbChatMessages, Bundle bundle) {
                result.setValue(parseMessage(qbChatMessages));
            }

            @Override
            public void onError(QBResponseException e) {
                result.setValue(null);
            }
        });
    }

    private ArrayList<QBMessage> parseMessage(ArrayList<QBChatMessage> qbChatMessages){
        ArrayList<QBMessage> messages = new ArrayList<>(qbChatMessages.size());
        for (QBChatMessage qbChatMessage : qbChatMessages) {
            messages.add(new QBMessage(qbChatMessage));
        }
        return messages;
    }


    private void fetchFromNetwork(LiveData<List<QBMessage>> dbSource) {

        final LiveData<List<QBMessage>> apiSource = createApiData();
        result.addSource(apiSource, new Observer<List<QBMessage>>() {
            @Override
            public void onChanged(final @Nullable List<QBMessage> qbMessages) {
                Log.i(TAG, "onChanged from api source");
                if (!CollectionsUtil.isEmpty(qbMessages)) {
                    dbExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            messageDao.insertAll(qbMessages);
                        }
                    });
                }
                result.setValue(qbMessages);
            }
        });
    }
}
