package com.quickblox.q_municate.service;


import android.app.Service;
import android.arch.lifecycle.LiveData;
import android.arch.lifecycle.Observer;
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

import com.example.q_municate_chat_service.entity.ContactItem;
import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.core.request.QBPagedRequestBuilder;
import com.quickblox.q_municate.App;
import com.quickblox.q_municate.business.RepositoryManager;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_db.models.Friend;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.model.QMUser;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import rx.Scheduler;
import rx.android.schedulers.AndroidSchedulers;
import rx.schedulers.Schedulers;

public class AndroidChatService extends Service {

    private static final String TAG = AndroidChatService.class.getSimpleName();
    private QBChatService chatService;
    private int currentCommand;
    private QBUser currentUser;
    private Messenger messenger;
    private List<QBChatDialog> chatDialogs;

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

    @Override
    public void onCreate() {
        super.onCreate();

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
        App.getInstance().getContactRepo().loadAll().observeForever(new Observer<List<ContactItem>>() {
            @Override
            public void onChanged(final @Nullable List<ContactItem> contactItemList) {
                Collection<Integer> friendIdsList = new ArrayList<>(contactItemList.size());
                for (ContactItem contactItem : contactItemList) {
                    friendIdsList.add(contactItem.getUserId());
                }
                QBPagedRequestBuilder requestBuilder = new QBPagedRequestBuilder();
                requestBuilder.setPage(ConstsCore.USERS_PAGE_NUM);
                requestBuilder.setPerPage(ConstsCore.USERS_PER_PAGE);
                QMUserService.getInstance().getUsersByIDs(friendIdsList, requestBuilder)
                        .subscribeOn(Schedulers.io())
                        .observeOn(AndroidSchedulers.mainThread())
                        .subscribe(new rx.Observer<List<QMUser>>() {
                    @Override
                    public void onCompleted() {
                        Log.d(TAG, "loadContacts onCompleted" );
                        startActionsOnSuccessLogin(true);
                    }

                    @Override
                    public void onError(Throwable e) {
                        Log.d(TAG, "onError " );
                    }

                    @Override
                    public void onNext(List<QMUser> qmUsers) {
                        Log.d(TAG, "loadContacts onNext" );
                    }
                });
            }
        });
    }

    public void loadDialogs(){
        RepositoryManager repositoryManager = null;
        LiveData<List<QBChatDialog>> listLiveData = repositoryManager.loadDialogs();
        listLiveData.observeForever((loadedChatDialogs) ->{

            chatDialogs = loadedChatDialogs;
            for (QBChatDialog qbChatDialog : chatDialogs) {
                qbChatDialog.join(null, null);
            }
            listLiveData.removeObserver(this);
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

