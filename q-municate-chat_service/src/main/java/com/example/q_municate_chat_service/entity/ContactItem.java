package com.example.q_municate_chat_service.entity;


import android.arch.persistence.room.Entity;
import android.util.Log;

import com.quickblox.chat.model.QBContactListItem;
import com.quickblox.chat.model.QBRosterEntry;

import org.jivesoftware.smack.roster.RosterEntry;

import static com.example.q_municate_chat_service.entity.ContactItem.TABLE_NAME;

@Entity(tableName = TABLE_NAME, primaryKeys="userId")
public class ContactItem extends QBContactListItem{

    public static final String TABLE_NAME = "contact_list";
    private static final String TAG = ContactItem.class.getSimpleName();

    public ContactItem() {
        super(null);
    }

    public ContactItem(RosterEntry rosterEntry) {
        super(rosterEntry);
        Log.i(TAG, "status="+getStatus() + ", userId=" + getUserId());
    }
}
