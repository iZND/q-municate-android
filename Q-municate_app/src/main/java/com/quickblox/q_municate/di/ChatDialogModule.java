package com.quickblox.q_municate.di;

import com.example.q_municate_chat_service.dao.ContactListDao;
import com.example.q_municate_chat_service.dao.QMUserDao;
import com.example.q_municate_chat_service.repository.ContactListRepoImpl;
import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.example.q_municate_chat_service.repository.QMUserRepositoryImpl;
import com.quickblox.q_municate.business.ChatDialogsManager;

import javax.inject.Singleton;

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
    public QMUserDao createUserDao(){
        return dialogDatabase.userDao();
    }

    @Provides
    public ContactListDao createContactListDao(){
        return dialogDatabase.contatIlistDao();
    }

    @Provides
    public QBMessageRepo createChatMessageRepo(QBMessageDao messageDao){
        return new QBMessageRepo(messageDao);
    }

    @Provides
    public QBChatDilogRepositoryImpl createChatDialogRepo(QBChatDialogDao dialogDao){
        return new QBChatDilogRepositoryImpl(dialogDao);
    }

    @Provides
    public ContactListRepoImpl createContactListRepo(ContactListDao contactListDao){
        return new ContactListRepoImpl(contactListDao);
    }

    @Provides
    public QMUserRepository createUserRepository(QMUserDao userDao){
        return new QMUserRepositoryImpl(userDao);
    }

    @Singleton
    @Provides
    public ChatDialogsManager createRepoManager(QBChatDilogRepositoryImpl dialogDao, QMUserRepository userRepository, QBMessageRepo messageRepo){
        return new ChatDialogsManager(dialogDao, userRepository, messageRepo);
    }



}
