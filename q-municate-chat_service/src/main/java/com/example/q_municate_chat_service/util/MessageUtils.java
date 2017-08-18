package com.example.q_municate_chat_service.util;


import com.example.q_municate_chat_service.entity.QBMessage;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.users.model.QBUser;

public class MessageUtils {

    public static boolean isReadForMe(QBMessage message, int userId){
        boolean isRead = false;
        if (!CollectionsUtil.isEmpty(message.getReadIds())){
            isRead = message.getReadIds().contains
                    (userId);
        }
        return isRead;
    }
}
