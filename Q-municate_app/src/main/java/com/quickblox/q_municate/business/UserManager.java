package com.quickblox.q_municate.business;

import android.arch.core.util.Function;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Transformations;
import android.util.Log;

import com.example.q_municate_chat_service.entity.ContactItem;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.repository.ContactListRepo;
import com.example.q_municate_chat_service.repository.ContactListRepoImpl;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;


public class UserManager {

    private static final String TAG = UserManager.class.getSimpleName();
    @Inject
    ContactListRepoImpl contactListRepo;

    @Inject
    QMUserRepository userRepository;

    public UserManager(){
    }

    public LiveData<List<QMUser>> loadUsersFromContactList(){
        return Transformations.switchMap(contactListRepo.loadAll(), (contactItemList) -> {
            Log.i(TAG, "loadUsersFromContactList + " + contactItemList);
            List<Integer> friendIdsList = new ArrayList<>(contactItemList.size());
            for (ContactItem contactItem : contactItemList) {
                friendIdsList.add(contactItem.getUserId());
            }
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(ConstsCore.USERS_PAGE_NUM);
            requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);
            return userRepository.loadByIds(friendIdsList);
            });
    }
}
