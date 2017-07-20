package com.quickblox.q_municate.di;

import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.repository.BaseRepo;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.quickblox.chat.model.QBChatDialog;

import dagger.Module;
import dagger.Provides;

@Module
public class ChatDialogModule {

    private QbChatDialogDatabase dialogDatabase;

    public ChatDialogModule(QbChatDialogDatabase dialogDatabase){
        this.dialogDatabase = dialogDatabase;
    }

    @Provides
    public QBChatDialogDao createChatDialogDao(){
        return dialogDatabase.chatDialogDao();
    }

    @Provides
    public QBMessageDao createMessageDao(){
        return dialogDatabase.chatMessageDao();
    }

    @Provides
    public QBMessageRepo createChatMessageRepo(QBMessageDao dialogDao){
        return new QBMessageRepo(dialogDao);
    }

    @Provides
    public QBChatDilogRepositoryImpl createChatDialogRepo(QBChatDialogDao dialogDao){
        return new QBChatDilogRepositoryImpl(dialogDao);
    }

}
