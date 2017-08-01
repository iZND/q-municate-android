package com.quickblox.q_municate.di;

import com.example.q_municate_chat_service.dao.QMUserDao;
import com.example.q_municate_chat_service.entity.user.QMUser;
import com.example.q_municate_chat_service.repository.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.dao.QBChatDialogDao;
import com.example.q_municate_chat_service.dao.QBMessageDao;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;
import com.example.q_municate_chat_service.entity.QBMessage;
import com.example.q_municate_chat_service.repository.BaseRepo;
import com.example.q_municate_chat_service.repository.QBMessageRepo;
import com.example.q_municate_chat_service.repository.QMUserRepository;
import com.example.q_municate_chat_service.repository.QMUserRepositoryImpl;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.q_municate.business.RepositoryManager;

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
    public QBMessageRepo createChatMessageRepo(QBMessageDao messageDao){
        return new QBMessageRepo(messageDao);
    }

    @Provides
    public QBChatDilogRepositoryImpl createChatDialogRepo(QBChatDialogDao dialogDao){
        return new QBChatDilogRepositoryImpl(dialogDao);
    }

    @Provides
    public QMUserRepository createUserRepository(QMUserDao userDao){
        return new QMUserRepositoryImpl(userDao);
    }

    @Provides
    public RepositoryManager createRepoManager(QBChatDilogRepositoryImpl dialogDao, QMUserRepository userRepository){
        return new RepositoryManager(dialogDao, userRepository);
    }



}
