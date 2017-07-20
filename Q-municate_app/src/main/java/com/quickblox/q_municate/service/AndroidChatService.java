package com.quickblox.q_municate.service;


import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.annotation.Nullable;
import android.util.Log;

import com.quickblox.chat.QBChatService;
import com.quickblox.chat.model.QBChatDialog;
import com.quickblox.core.QBEntityCallback;
import com.quickblox.core.exception.QBResponseException;
import com.quickblox.users.model.QBUser;

import java.util.ArrayList;

public class AndroidChatService extends Service{

        private static final String TAG = AndroidChatService.class.getSimpleName();
        private QBChatService chatService;
        private int currentCommand;
        private QBUser currentUser;
        private Messenger messenger;

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

            startSuitableActions();

            return START_REDELIVER_INTENT;
        }

        private void parseIntentExtras(Intent intent) {
            if (intent != null && intent.getExtras() != null) {
                currentCommand = intent.getIntExtra(Consts.EXTRA_COMMAND_TO_SERVICE, Consts.COMMAND_NOT_FOUND);
                messenger = (Messenger)intent.getParcelableExtra(Consts.EXTRA_PENDING_INTENT);
                currentUser = (QBUser) intent.getSerializableExtra(Consts.EXTRA_QB_USER);
            }
        }

        private void startSuitableActions() {
            if (currentCommand == Consts.COMMAND_LOGIN) {
                startLoginToChat();
            } else if (currentCommand == Consts.COMMAND_LOGOUT) {
                logout();
            }
        }

        private void createChatService() {
            if (chatService == null) {
                QBChatService.setDebugEnabled(true);
                QBChatService.setDefaultPacketReplyTimeout(30*1000);

                QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
                configurationBuilder.setSocketTimeout(0);
                QBChatService.setConfigurationBuilder(configurationBuilder);
                chatService = QBChatService.getInstance();
            }
        }

        private void startLoginToChat() {
            if (!chatService.isLoggedIn()) {
                loginToChat(currentUser);
            } else {
                sendResultToActivity(true, null);
            }
        }

        private void loginToChat(QBUser qbUser) {
            chatService.login(qbUser, new QBEntityCallback<QBUser>() {
                @Override
                public void onSuccess(QBUser qbUser, Bundle bundle) {
                    Log.d(TAG, "login onSuccess");
                    startActionsOnSuccessLogin();
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

        private void startActionsOnSuccessLogin() {
            //initPingListener();
            sendResultToActivity(true, null);
        }

    /*private void initPingListener() {
        ChatPingAlarmManager.onCreate(this);
        ChatPingAlarmManager.getInstanceFor().addPingListener(new PingFailedListener() {
            @Override
            public void pingFailed() {
                Log.d(TAG, "Ping chat server failed");
            }
        });
    }*/

        private void sendResultToActivity(boolean isSuccess, String errorMessage) {
            if (messenger != null) {
                Log.d(TAG, "sendResultToActivity()");
                try {
                    messenger.send(Message.obtain(null,
                            Consts.EXTRA_LOGIN_RESULT_CODE, isSuccess ? 1:0, 0));
                } catch (RemoteException e ) {
                    String errorMessageSendingResult = e.getMessage();
                    Log.d(TAG, errorMessageSendingResult != null
                            ? errorMessageSendingResult
                            : "Error sending result to activity");
                }
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

