package com.quickblox.q_municate.business;

import com.example.q_municate_chat_service.entity.ContactItem;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.repository.ContactListRepo;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate_core.utils.ConstsCore;

import java.util.ArrayList;
import java.util.List;

import rx.Observable;

public class UserManager {

    ContactListRepo contactListRepo;

    QMUserRepository userRepository;

    UserManager(){
    }

    public rx.Observable<List<QMUser>> loadUsersFromContactList(){
        final Observable<List<QMUser>>[] result = new Observable<List<QMUser>>[1];
        contactListRepo.loadAll().observeForever((contactItemList) -> {
            List<Integer> friendIdsList = new ArrayList<>(contactItemList.size());
            for (ContactItem contactItem : contactItemList) {
                friendIdsList.add(contactItem.getUserId());
            }
            QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
            requestBuilder.setPage(ConstsCore.USERS_PAGE_NUM);
            requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);
            result[0] = userRepository.loadByIds(friendIdsList);

        };
        return result[0];
    }
}
