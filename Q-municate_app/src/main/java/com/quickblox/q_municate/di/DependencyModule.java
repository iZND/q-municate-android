package com.quickblox.q_municate.di;


import android.arch.persistence.room.Room;
import android.content.Context;

import com.example.q_municate_chat_service.QBChatDilogRepository;
import com.example.q_municate_chat_service.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;
import com.quickblox.q_municate.App;

import javax.inject.Singleton;

import dagger.Provides;

public class DependencyModule {

    private App countdownApplication;

    public DependencyModule(App countdownApplication) {
        this.countdownApplication = countdownApplication;
    }

    @Provides
    Context applicationContext() {
        return countdownApplication;
    }

    @Provides
    @Singleton
    QBChatDilogRepository providesEventRepository(QbChatDialogDatabase eventDatabase) {
        return new QBChatDilogRepositoryImpl(eventDatabase.chatDialogDao());
    }

    @Provides
    @Singleton
    QbChatDialogDatabase providesEventDatabase(Context context) {
        return Room.databaseBuilder(context.getApplicationContext(),
                QbChatDialogDatabase.class, "chat_dialog.db").build();
    }
}
