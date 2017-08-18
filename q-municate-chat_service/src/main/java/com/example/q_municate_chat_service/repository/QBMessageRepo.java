package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.util.MessageUtils;
import com.example.q_municate_chat_service.util.RxUtils;
import com.quickblox.chat.Consts;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBChatMessage;
import com.quickblox.chat.request.QBMessageGetBuilder;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;
import rx.functions.Func1;

import static com.example.q_municate_chat_service.util.Consts.PAGE_LIMIT;


public class QBMessageRepo extends BaseRepoImpl<QBMessage> implements BaseRepo<QBMessage, String>{

    private static final String TAG = QBMessageRepo.class.getSimpleName();

    QBMessageDao messageDao;
    private String dialogId;

    public QBMessageRepo(QBMessageDao messageDao) {
        this.messageDao = messageDao;
    }

    @Override
    public void create(QBMessage message) {
        dbExecutor.execute( () -> messageDao.insert(message));
    }

    @Override
    public LiveData<List<QBMessage>> loadAll() {
        return null;
    }

    public LiveData<List<QBMessage>> loadAll(String dialogId, boolean forceLoad) {
        Log.i(TAG, "loadAll dialogId="+dialogId);
        this.dialogId = dialogId;
        final LiveData<List<QBMessage>> dbSource = messageDao.getAllByDialog(dialogId);
        result.addSource(dbSource, (messages) -> {
                Log.i(TAG, "onChanged from db source");
                if (shouldFetch(messages)) {
                    result.removeSource(dbSource);
                    Log.i(TAG, "onChanged from db source :shouldFetch");
                    fetchFromNetwork(dbSource);
                } else {
                    result.setValue(messages);
                }
            });
        return result;
    }

    @Override
    public LiveData<QBMessage> loadById(String s) {
        return null;
    }

    public void update(QBMessage message) {
        dbExecutor.execute( () -> messageDao.update(message));
    }

    @Override
    public void delete(QBMessage event) {

    }

    @Override
    protected void performApiReuqest() {
        QBChatDialog chatDialog = new QBChatDialog(dialogId);

        QBMessageGetBuilder messageGetBuilder = new QBMessageGetBuilder();
        messageGetBuilder.setLimit(PAGE_LIMIT);

        /*if (isLoadOldMessages) {
            messageGetBuilder.lt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            messageGetBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
        } else {
            messageGetBuilder.gt(Consts.MESSAGE_DATE_SENT, lastDateLoad);
            if (lastDateLoad > 0) {
                messageGetBuilder.sortAsc(QBServiceConsts.EXTRA_DATE_SENT);
            } else {
                messageGetBuilder.sortDesc(QBServiceConsts.EXTRA_DATE_SENT);
            }
        }*/

        messageGetBuilder.markAsRead(false);

        QBRestChatService.getDialogMessages(chatDialog, messageGetBuilder).performAsync(new QBEntityCallback<ArrayList<QBChatMessage>>() {
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
        QBUser user = QBChatService.getInstance().getUser();
        ArrayList<QBMessage> messages = new ArrayList<>(qbChatMessages.size());
        for (QBChatMessage qbChatMessage : qbChatMessages) {
            QBMessage message = new QBMessage(qbChatMessage);
            message.setReadForOwner(MessageUtils.isReadForMe(message, user.getId()));
            messages.add(message);
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
