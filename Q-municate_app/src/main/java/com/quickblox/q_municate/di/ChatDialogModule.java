package com.quickblox.q_municate.di;

import com.example.q_municate_chat_service.QBChatDilogRepository;
import com.example.q_municate_chat_service.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;

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
    public QBChatDilogRepository createChatDialogRepo(QBChatDialogDao dialogDao){
        return new QBChatDilogRepositoryImpl(dialogDao);
    }

}
