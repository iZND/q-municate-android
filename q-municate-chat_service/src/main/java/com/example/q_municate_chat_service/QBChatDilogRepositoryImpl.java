package com.example.q_municate_chat_service;

import android.arch.lifecycle.LiveData;
import android.os.Bundle;

import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.core.server.Performer;

import java.util.ArrayList;
import java.util.List;

public class QBChatDilogRepositoryImpl implements QBChatDilogRepository {

    private QBChatDialogDao chatDialogDao;

    public QBChatDilogRepositoryImpl(QBChatDialogDao dialogDao){
        chatDialogDao = dialogDao;
    }

    @Override
    public void create(QBChatDialog dialog) {
        //
    }

    @Override
    public LiveData<List<QBChatDialog>> loadAll() {
        LiveData<List<QBChatDialog>> all = chatDialogDao.getAll();
        if (all != null) {
            return all;
        }
        return new LiveData<List<QBChatDialog>>() {
            @Override
            protected void onActive() {
                QBRestChatService.getChatDialogs(null, null).performAsync(new QBEntityCallback<ArrayList<QBChatDialog>>() {
                    @Override
                    public void onSuccess(ArrayList<QBChatDialog> qbChatDialogs, Bundle bundle) {
                        chatDialogDao.insertAll(qbChatDialogs);
                        postValue(qbChatDialogs);
                    }

                    @Override
                    public void onError(QBResponseException e) {
                        postValue(null);
                    }
                });
            }
        };
    }

    @Override
    public LiveData<QBChatDialog> loaByd(String id) {
        return null;
    }

    @Override
    public void delete(QBChatDialog event) {

    }
}
