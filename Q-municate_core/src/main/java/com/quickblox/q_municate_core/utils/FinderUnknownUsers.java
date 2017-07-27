package com.quickblox.q_municate_core.utils;

import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate_user_service.QMUserService;
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

    public Collection<Integer> find() {
        if (dialogsList != null) {
            return findUserInDialogsList(dialogsList);
        } else {
            return findUserInDialog(dialog);
        }
    }

    private Collection<Integer> findUserInDialogsList(List<QBChatDialog > dialogsList) {
        for (QBChatDialog  dialog : dialogsList) {
            findUserInDialog(dialog);
        }
        return loadIdsSet;
    }

    private Collection<Integer> findUserInDialog(QBChatDialog  dialog) {
        List<Integer> occupantsList = dialog.getOccupants();
        for (int occupantId : occupantsList) {
            boolean isUserInBase = QMUserService.getInstance().getUserCache().exists((long)occupantId);
            if (!isUserInBase && currentUser.getId().intValue() != occupantId) {
                loadIdsSet.add(occupantId);
            }
        }
        return loadIdsSet;
    }
}