package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LifecycleOwner;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.MutableLiveData;
import android.arch.lifecycle.Observer;
import android.util.Log;

import com.example.q_municate_chat_service.entity.PagedResult;
import com.quickblox.chat.QBRestChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBRequestGetBuilder;
import com.quickblox.core.server.Performer;

import java.util.ArrayList;
import java.util.List;

public class RepoPageLoader implements Runnable{

    private static final String TAG = RepoPageLoader.class.getSimpleName();

    private  List<QBChatDialog> result = new ArrayList<>();

    private MutableLiveData<List<QBChatDialog>> liveData = new MutableLiveData<>();

    RepoPageLoader(){

    }

    LiveData<List<QBChatDialog>> asLiveData() {
        return liveData;
    }

    @Override
    public void run() {
        Log.i(TAG, "run loading");
        int pageNumber = 0;

        QBRequestGetBuilder requestGetBuilder = new QBRequestGetBuilder();
        boolean loadedAll = false;

        while (!loadedAll) {
            requestGetBuilder.setSkip(pageNumber * 100);
            try {
                ArrayList<QBChatDialog> dialogs = QBRestChatService.getChatDialogs(null, requestGetBuilder).perform();
                result.addAll(dialogs);
                if (dialogs.size() < 100 ) {
                    loadedAll = true;
                }
                pageNumber++;
            } catch (QBResponseException e) {
                e.printStackTrace();
                loadedAll = true;
            }
        }
        liveData.postValue(result);
    }
}
