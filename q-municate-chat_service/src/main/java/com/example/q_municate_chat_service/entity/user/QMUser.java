package com.example.q_municate_chat_service.entity.user;


import android.arch.persistence.room.Entity;
import android.arch.persistence.room.Ignore;

import com.quickblox.core.helper.StringifyArrayList;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.List;

import static com.example.q_municate_chat_service.entity.user.QMUser.TABLE_NAME;


@Entity(tableName = TABLE_NAME, primaryKeys="id")
public class QMUser extends QBUser {

    public static final String TABLE_NAME = "users";

    private String avatar;

    private String status;

    public String getAvatar() {
        return avatar;
    }

    public void setAvatar(String avatar) {
        this.avatar = avatar;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    @Override
    @Ignore
    public void setListTags(StringifyArrayList<String> tags) {
        super.setListTags(tags);
    }

    @Override
    @Ignore
    public StringifyArrayList<String> getListTags() {
        return super.getListTags();
    }

    public static QMUser convert(QBUser qbUser){
        QMUser result = new QMUser();
        result.setId(qbUser.getId());
        result.setFullName(qbUser.getFullName());
        result.setEmail(qbUser.getEmail());
        result.setLogin(qbUser.getLogin());
        result.setPhone(qbUser.getPhone());
        result.setWebsite(qbUser.getWebsite());
        result.setLastRequestAt(qbUser.getLastRequestAt());
        result.setExternalId(qbUser.getExternalId());
        result.setFacebookId(qbUser.getFacebookId());
        result.setTwitterId(qbUser.getTwitterId());
        result.setTwitterDigitsId(qbUser.getTwitterDigitsId());
        result.setFileId(qbUser.getFileId());
        result.setTags(qbUser.getTags());
        result.setPassword(qbUser.getPassword());
        result.setOldPassword(qbUser.getOldPassword());
        result.setCustomData(qbUser.getCustomData());
        result.setCreatedAt(qbUser.getCreatedAt());
        result.setUpdatedAt(qbUser.getUpdatedAt());

        final QMUserCustomData userCustomData = Utils.customDataToObject(qbUser.getCustomData());
        result.setAvatar(userCustomData.getAvatarUrl());
        result.setStatus(userCustomData.getStatus());
        return result;
    }

    public static List<QMUser> convertList(List<QBUser> qbUsers){
        List<QMUser> result = new ArrayList<QMUser>(qbUsers.size());
        for(QBUser qbUser: qbUsers){
            result.add(QMUser.convert(qbUser));
        }
        return result;
    }
}
