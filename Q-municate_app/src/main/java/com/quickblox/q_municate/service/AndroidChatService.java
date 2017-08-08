package com.quickblox.q_municate.service;


import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.chat.model.QBDialogType;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.helper.CollectionsUtil;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.ChatDialogsManager;
import com.quickblox.q_municate.utils.LiveDataUtils;
import com.quickblox.users.model.QBUser;

import org.jivesoftware.smack.SmackException;
import org.jivesoftware.smack.XMPPException;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

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
    private ExecutorService executorService;

    private Handler handler = new Handler(Looper.getMainLooper());

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

        intent.putExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_LOGIN);
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
        initThreads();
        Log.d(TAG, "Service onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Service started with action =" + intent.getIntExtra(Consts.EXTRA_COMMAND_TO_SERVICE,
                Consts.COMMAND_NOT_FOUND));

        parseIntentExtras(intent);

        startSuitableActions(intent);

        return START_REDELIVER_INTENT;
    }

    private void initThreads() {
        executorService = Executors.newSingleThreadExecutor();
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
            executorService.execute(
                    () -> startLoginToChat(intent.getBooleanExtra(Consts.EXTRA_FULL_LOGIN, true)));

        } else if (currentCommand == Consts.COMMAND_LOGOUT) {
            executorService.execute( () -> logout());
        } else if (currentCommand == Consts.COMMAND_LOAD_DIALOGS) {
            executorService.execute( () ->loadDialogs(intent));
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
        try {
            chatService.login(qbUser);
            Thread.sleep(2000);
            if (fullLogin) {
                loadContacts();
            } else {
                //joinDialogs();
                sendResultToActivity(true, null);
            }
        } catch (XMPPException|IOException|SmackException|InterruptedException e) {
            e.printStackTrace();
            sendResultToActivity(false, e.getMessage() != null
                    ? e.getMessage()
                    : "Login error");
        }
    }

    private void joinDialogs() {
        if (chatDialogs == null) {
            return;
        }
        for (QBChatDialog chatDialog : chatDialogs) {
            if (QBDialogType.GROUP == chatDialog.getDialogType()) {
                chatDialog.join(null, null);
            }
        }
    }

    private void loadContacts() {
        handler.post( () ->
            LiveDataUtils.observeValue(App.getInstance().getUserManager().loadUsersFromContactList(),
                users -> {
                    //if (!CollectionsUtil.isEmpty(users)){
                        startActionsOnSuccessLogin(true);
                    /*} else {
                        Log.d(TAG, "onError load users from contact list" );
                    }*/
                }));

    }

    public void loadDialogs(Intent intent){
        LiveData<List<QBChatDialog>> listLiveData = repositoryManager.loadDialogs(true);

        handler.post( () -> {
            LiveDataUtils.observeValue(listLiveData, (loadedChatDialogs) -> {
                chatDialogs = loadedChatDialogs;
                for (QBChatDialog qbChatDialog : chatDialogs) {
                    qbChatDialog.initForChat(chatService);
                    qbChatDialog.join(null, null);
                }
                Bundle bundle = new Bundle();
                sendBroadcast(bundle, Consts.EXTRA_LOAD_DIALOGS_ACTION);
            });
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
        Log.d(TAG, "sendResultToActivity()");
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
        for (QBChatDialog chatDialog : chatDialogs) {
            if (QBDialogType.GROUP == chatDialog.getDialogType()) {
                try {
                    chatDialog.leave();
                } catch (XMPPException | SmackException.NotConnectedException e) {
                    e.printStackTrace();
                }
            }
        }
        if (chatService != null) {
            chatService.logout(null);
        }
        //stopSelf();
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

