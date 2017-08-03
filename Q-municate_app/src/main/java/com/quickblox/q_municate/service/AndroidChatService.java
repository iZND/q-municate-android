package com.quickblox.q_municate.service;


import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;
import com.quickblox.q_municate.utils.LiveDataUtils;
import com.quickblox.users.model.QBUser;

import java.util.List;

import javax.inject.Inject;

public class AndroidChatService extends Service {

    private static final String TAG = AndroidChatService.class.getSimpleName();
    private QBChatService chatService;
    private int currentCommand;
    private QBUser currentUser;
    private Messenger messenger;
    private List<QBChatDialog> chatDialogs;

    @Inject
    ChatDialogsManager repositoryManager;

    public static void login(Context context, QBUser qbUser, Messenger messenger) {
        Intent intent = new Intent(context, AndroidChatService.class);

        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGIN);
        intent.putExtra(Consts.EXTRA_QB_USER, qbUser);
        intent.putExtra(Consts.EXTRA_PENDING_INTENT, messenger);

        context.startService(intent);
    }



    public static void login(Context context, QBUser qbUser) {
        login(context, qbUser, null);
    }

    public static void lightLogin(Context context, QBUser qbUser) {
        Intent intent = new Intent(context, AndroidChatService.class);

        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LIGHT_LOGIN);
        intent.putExtra(Consts.EXTRA_QB_USER, qbUser);
        intent.putExtra(Consts.EXTRA_FULL_LOGIN, false);
        context.startService(intent);
    }

    public static void loadDialogs(Context context, int page){
        Intent intent = new Intent(context, AndroidChatService.class);

        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOAD_DIALOGS);
        intent.putExtra(Consts.EXTRA_PAGE, page);

        context.startService(intent);
    }


    @Override
    public void onCreate() {
        super.onCreate();
        App.getInstance().getComponent().inject(this);
        createChatService();

        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started");

        parseIntentExtras(intent);

        startSuitableActions(intent);

        return START_REDELIVER_INTENT;
    }

    private void parseIntentExtras(Intent intent) {
        if (intent != null && intent.getExtras() != null) {
            currentCommand = intent.getIntExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_NOT_FOUND);
            messenger = (Messenger) intent.getParcelableExtra(Consts.EXTRA_PENDING_INTENT);
            currentUser = (QBUser) intent.getSerializableExtra(Consts.EXTRA_QB_USER);
        }
    }

    private void startSuitableActions(Intent intent) {
        if (currentCommand == Consts.COMMAND_LOGIN) {
            startLoginToChat(intent.getBooleanExtra(Consts.EXTRA_FULL_LOGIN, true));
        } else if (currentCommand == Consts.COMMAND_LOGOUT) {
            logout();
        } else if (currentCommand == Consts.COMMAND_LOAD_DIALOGS) {
            loadDialogs(intent);
        }
    }

    private void createChatService() {
        if (chatService == null) {
            QBChatService.setDebugEnabled(true);
            QBChatService.setDefaultPacketReplyTimeout(30 * 1000);

            QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
            configurationBuilder.setSocketTimeout(0);
            QBChatService.setConfigurationBuilder(configurationBuilder);
            chatService = QBChatService.getInstance();
        }
    }

    private void startLoginToChat(boolean fullLogin) {
        if (!chatService.isLoggedIn()) {
            loginToChat(currentUser, fullLogin);
        } else {
            sendResultToActivity(true, null);
        }
    }

    private void loginToChat(QBUser qbUser, boolean fullLogin) {
        chatService.login(qbUser, new QBEntityCallback<QBUser>() {
            @Override
            public void onSuccess(QBUser qbUser, Bundle bundle) {
                Log.d(TAG, "login onSuccess");
                if (fullLogin) {
                    try {
                        Thread.sleep(2000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                    loadContacts();
                }
            }

            @Override
            public void onError(QBResponseException e) {
                Log.d(TAG, "login onError " + e.getMessage());
                sendResultToActivity(false, e.getMessage() != null
                        ? e.getMessage()
                        : "Login error");
            }
        });
    }

    private void loadContacts() {

        LiveDataUtils.observeValue(App.getInstance().getUserManager().loadUsersFromContactList(),
                users -> {
                    if (!CollectionsUtil.isEmpty(users)){
                        startActionsOnSuccessLogin(true);
                    } else {
                        Log.d(TAG, "onError load users from contact list" );
                    }
                });
    }

    public void loadDialogs(Intent intent){
        LiveData<List<QBChatDialog>> listLiveData = repositoryManager.loadDialogs(true);

        LiveDataUtils.observeValue(listLiveData, (loadedChatDialogs) -> {
            chatDialogs = loadedChatDialogs;
            for (QBChatDialog qbChatDialog : chatDialogs) {
                qbChatDialog.join(null, null);
            }
            Bundle bundle = new Bundle();
            sendBroadcast(bundle, Consts.EXTRA_LOAD_DIALOGS_ACTION);
        });

    }

    private void startActionsOnSuccessLogin(boolean success) {
        //initPingListener();
        sendResultToActivity(success, null);
    }

    private void sendBroadcast(boolean success) {
        Intent intent =new Intent(Consts.EXTRA_LOGIN_ACTION);
        Bundle bundle = new Bundle();
        bundle.putInt(Consts.EXTRA_LOGIN_RESULT, (success ? 1 : 0));
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendBroadcast(Bundle bundle, String action) {
        Intent intent =new Intent(action);
        intent.putExtras(bundle);
        LocalBroadcastManager.getInstance(this).sendBroadcast(intent);
    }

    private void sendResultToActivity(boolean isSuccess, String errorMessage) {
        if (messenger != null) {
            Log.d(TAG, "sendResultToActivity()");
            try {
                messenger.send(Message.obtain(null,
                        Consts.EXTRA_LOGIN_RESULT_CODE, isSuccess ? 1 : 0, 0));
            } catch (RemoteException e) {
                String errorMessageSendingResult = e.getMessage();
                Log.d(TAG, errorMessageSendingResult != null
                        ? errorMessageSendingResult
                        : "Error sending result to activity");
            }
        } else {
            sendBroadcast(isSuccess);
        }
    }

    public static void logout(Context context) {
        Intent intent = new Intent(context, AndroidChatService.class);
        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGOUT);
        context.startService(intent);
    }

    private void logout() {
        destroyRtcClientAndChat();
    }

    private void destroyRtcClientAndChat() {
        //ChatPingAlarmManager.onDestroy();
        if (chatService != null) {
            chatService.logout(new QBEntityCallback<Void>() {
                @Override
                public void onSuccess(Void aVoid, Bundle bundle) {
                    chatService.destroy();
                }

                @Override
                public void onError(QBResponseException e) {
                    Log.d(TAG, "logout onError " + e.getMessage());
                    chatService.destroy();
                }
            });
        }
        stopSelf();
    }

    @Override
    public void onDestroy() {
        Log.d(TAG, "Service onDestroy()");
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG, "Service onBind)");
        return null;
    }
}

