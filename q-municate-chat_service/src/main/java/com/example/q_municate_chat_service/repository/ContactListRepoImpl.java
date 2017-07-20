package com.example.q_municate_chat_service.repository;


import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
import android.support.annotation.Nullable;
import android.util.Log;

import com.example.q_municate_chat_service.dao.ContactListDao;
import com.example.q_municate_chat_service.entity.ContactItem;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.QBContactList;
import com.quickblox.chat.listeners.QBSubscriptionListener;
import com.quickblox.chat.model.QBContactListItem;
import com.quickblox.core.helper.CollectionsUtil;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;


public class ContactListRepoImpl extends BaseRepoImpl<ContactItem, Integer> {

    private static final String TAG = ContactListRepoImpl.class.getSimpleName();

    ContactListDao contactListDao;

    private final QBContactList roster;

    public ContactListRepoImpl(ContactListDao contactListDao) {
        this.contactListDao = contactListDao;

        roster = QBChatService.getInstance().getRoster(QBContactList.SubscriptionMode.mutual,
                new SubscriptionListener());
        Log.i(TAG, "loading roster");
    }

    @Override
    public void create(ContactItem event) {

    }

    @Override
    public LiveData<List<ContactItem>> loadAll() {

        Log.i(TAG, "loadAll");
        final LiveData<List<ContactItem>> dbSource = contactListDao.getAll();
        result.addSource(dbSource, new Observer<List<ContactItem>>() {
            @Override
            public void onChanged(@Nullable List<ContactItem> data) {
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

    private void fetchFromNetwork(LiveData<List<ContactItem>> dbSource) {

        final LiveData<List<ContactItem>> apiSource = createApiData();
        result.addSource(apiSource, new Observer<List<ContactItem>>() {
            @Override
            public void onChanged(final @Nullable List<ContactItem> contactItemList) {
                Log.i(TAG, "onChanged from api source");
                if (!CollectionsUtil.isEmpty(contactItemList)) {
                    dbExecutor.execute(new Runnable() {
                        @Override
                        public void run() {
                            contactListDao.insertAll(contactItemList);
                        }
                    });
                }
                result.setValue(contactItemList);
            }
        });
    }

    @Override
    public LiveData<ContactItem> loadById(Integer integer) {
        return null;
    }

    @Override
    public void delete(ContactItem event) {

    }

    @Override
    protected void performApiReuqest() {

        Collection<QBContactListItem> entries = roster.getEntries();
        Log.i(TAG, "performApiReuqest entries:"+roster.getEntries());
        ArrayList<ContactItem> items = new ArrayList<>(entries.size());
        for (QBContactListItem entry : entries) {
            items.add(new ContactItem(entry.getRosterEntry()));
        }
        result.postValue(items);
    }

    private class SubscriptionListener implements QBSubscriptionListener {

        @Override
        public void subscriptionRequested(int userId) {
            try {
                create(null);
            } catch (Exception e) {

            }
        }
    }
}
