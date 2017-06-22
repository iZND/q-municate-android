package com.quickblox.q_municate.di;

import com.quickblox.q_municate.ui.fragments.chats.QbChatDialogListViewModel;

import javax.inject.Singleton;

import dagger.Component;

@Component(modules = ChatDialogModule.class)
@Singleton
public interface DIComponent {
    void inject(QbChatDialogListViewModel model);
}
