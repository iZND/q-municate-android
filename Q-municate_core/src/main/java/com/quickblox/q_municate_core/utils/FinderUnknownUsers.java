package com.quickblox.q_municate_core.utils;

import android.content.Context;

import com.quickblox.chat.model.QBChatDialog ;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.q_municate_core.qb.helpers.QBRestHelper;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.utils.ErrorUtils;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class FinderUnknownUsers {

    private QBChatDialog  dialog;
    private List<QBChatDialog > dialogsList;
    private Set<Integer> loadIdsSet;
    private QBUser currentUser;

    public FinderUnknownUsers(QBUser currentUser, List<QBChatDialog > dialogsList) {
        init( currentUser);
        this.dialogsList = dialogsList;
    }

    public FinderUnknownUsers(QBUser currentUser, QBChatDialog  dialog) {
        init( currentUser);
        this.dialog = dialog;
    }

    private void init(QBUser currentUser) {
        this.currentUser = currentUser;
        loadIdsSet = new HashSet<Integer>();
    }

    public void find() {
        if (dialogsList != null) {
            findUserInDialogsList(dialogsList);
        } else {
            findUserInDialog(dialog);
        }
    }

    private void findUserInDialogsList(List<QBChatDialog > dialogsList) {
        for (QBChatDialog  dialog : dialogsList) {
            findUserInDialog(dialog);
        }
        if (!loadIdsSet.isEmpty()) {
            loadUsers();
        }
    }

    private void loadUsers() {
        int oneElement = 1;
        try {
            if (loadIdsSet.size() == oneElement) {
                int userId = loadIdsSet.iterator().next();
                QMUser user = QMUserService.getInstance().getUserSync(userId, true);
            } else {
                Collection<QMUser> userCollection = loadUsers(loadIdsSet);
            }
        } catch (QBResponseException e) {
            ErrorUtils.logError(e);
        }
    }

    private List<QMUser> loadUsers(Set<Integer> loadIdsSet) throws QBResponseException {
        return QMUserService.getInstance().getUsersByIDsSync(loadIdsSet, null);
    }

    private void findUserInDialog(QBChatDialog  dialog) {
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = QMUserService.getInstance().getUserCache().exists((long)occupantId);
            if (!isUserInBase && currentUser.getId().intValue() != occupantId) {
                loadIdsSet.add(occupantId);
            }
        }
    }
}