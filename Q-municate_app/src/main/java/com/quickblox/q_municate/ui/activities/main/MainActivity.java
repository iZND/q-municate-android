package com.quickblox.q_municate.ui.activities.main;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.os.Messenger;
import android.support.v4.app.Fragment;
import android.text.TextUtils;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;

import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.assist.SimpleImageLoadingListener;
import com.quickblox.q_municate.R;
import com.quickblox.q_municate.service.AndroidChatService;
import com.quickblox.q_municate.service.Consts;
import com.quickblox.q_municate.ui.activities.base.BaseLoggableActivity;
import com.quickblox.q_municate.ui.activities.settings.SettingsActivity;
import com.quickblox.q_municate.ui.fragments.chats.DialogsListFragment;
import com.quickblox.q_municate.utils.helpers.FacebookHelper;
import com.quickblox.q_municate.utils.helpers.ImportFriendsHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate.utils.image.ImageUtils;
import com.quickblox.q_municate_core.core.command.Command;
import com.quickblox.q_municate_core.models.AppSession;
import com.quickblox.q_municate_core.models.UserCustomData;
import com.quickblox.q_municate_core.service.QBServiceConsts;
import com.quickblox.q_municate_core.utils.Utils;

public class MainActivity extends BaseLoggableActivity {

    private static final String TAG = MainActivity.class.getSimpleName();
    private static final String FIRST_LOGIN = "first_login";

    private FacebookHelper facebookHelper;

    private ImportFriendsSuccessAction importFriendsSuccessAction;
    private ImportFriendsFailAction importFriendsFailAction;
    private LoginChatAction loginChatAction;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        context.startActivity(intent);
    }

    public static void start(Context context, boolean firstLogin) {
        Intent intent = new Intent(context, MainActivity.class);
        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        intent.putExtra(FIRST_LOGIN, firstLogin);
        context.startActivity(intent);
    }

    @Override
    protected int getContentResId() {
        return R.layout.activity_main;
    }

    @Override
    public void onBackPressed() {
        if( getSupportFragmentManager().getBackStackEntryCount() == 1){
           finish();
        } else {
            super.onBackPressed();
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d("MainActivity", "onCreate");

        initFields();
        setUpActionBarWithUpButton();

        boolean isFisrtLogin = getIntent().getBooleanExtra(FIRST_LOGIN, false);

        /*if (!isChatInitializedAndUserLoggedIn()) {
            Log.d("MainActivity", "onCreate. !isChatInitializedAndUserLoggedIn()");
            loginChat();
        } else {*/
           /* if (getSupportFragmentManager().findFragmentById(R.id.container_fragment) == null && isFisrtLogin) {
                launchDialogsListFragment();
            }*/
        //}

        addDialogsAction();
    }

    @Override
    protected Messenger getMessenger() {
        return new Messenger(new Handler(){
            @Override
            public void handleMessage(Message msg) {
                switch (msg.what){
                    case Consts.EXTRA_LOGIN_RESULT_CODE:
                        //performLoginChatSuccessAction(null);
                        break;
                    default:
                        super.handleMessage(msg);

                }
            }
        });
    }

    @Override
    protected void onChatServiceBound() {
        Log.i(TAG, "onChatServiceBound");
        Fragment currentFragment = getCurrentFragment();
        if (currentFragment == null) {
            performLoginChatSuccessAction(null);
        } else{
            Log.i(TAG, "currentFragment=" +currentFragment);
        }
    }

    private void initFields() {
        Log.d("MainActivity", "initFields()");
        title = " " + AppSession.getSession().getUser().getFullName();
        importFriendsSuccessAction = new ImportFriendsSuccessAction();
        importFriendsFailAction = new ImportFriendsFailAction();
        loginChatAction = new LoginChatAction();
        facebookHelper = new FacebookHelper(MainActivity.this);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        facebookHelper.onActivityResult(requestCode, resultCode, data);
        if (SettingsActivity.REQUEST_CODE_LOGOUT == requestCode && RESULT_OK == resultCode) {
            AndroidChatService.fulllogout(this);
            startLandingScreen();
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        return false;
    }

    @Override
    protected void onStart() {
        super.onStart();
        Log.d("MainActivity", "onStart()");
    }

    @Override
    protected void onRestart() {
        super.onRestart();
        Log.d("MainActivity", "onRestart()");
    }

    @Override
    protected void onResume() {
        actualizeCurrentTitle();
        super.onResume();
        addActions();
    }

    private void actualizeCurrentTitle() {
        if (AppSession.getSession().getUser().getFullName() != null) {
            title = " " + AppSession.getSession().getUser().getFullName();
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        removeActions();
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        removeDialogsAction();
    }

    @Override
    protected void checkShowingConnectionError() {
        if (!isNetworkAvailable()) {
            setActionBarTitle(getString(R.string.dlg_internet_connection_is_missing));
            setActionBarIcon(null);
        } else {
            setActionBarTitle(title);
            checkVisibilityUserIcon();
        }
    }

    @Override
    protected void performLoginChatSuccessAction(Bundle bundle) {
        Log.d(TAG, "performLoginChatSuccessAction()");
        super.performLoginChatSuccessAction(bundle);
        actualizeCurrentTitle();
        launchDialogsListFragment();
    }

    private void addDialogsAction() {
        addAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION, new LoadChatsSuccessAction());
    }

    private void removeDialogsAction() {
        removeAction(QBServiceConsts.LOAD_CHATS_DIALOGS_SUCCESS_ACTION);
    }

    private void addActions() {
        addAction(Consts.EXTRA_LOGIN_ACTION, loginChatAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION, importFriendsSuccessAction);
        addAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION, importFriendsFailAction);

        updateBroadcastActionList();
    }

    private void removeActions() {
        removeAction(QBServiceConsts.IMPORT_FRIENDS_SUCCESS_ACTION);
        removeAction(QBServiceConsts.IMPORT_FRIENDS_FAIL_ACTION);
        removeAction(Consts.EXTRA_LOGIN_ACTION);

        updateBroadcastActionList();
    }

    private void checkVisibilityUserIcon() {
        UserCustomData userCustomData = Utils.customDataToObject(AppSession.getSession().getUser().getCustomData());
        if (!TextUtils.isEmpty(userCustomData.getAvatarUrl())) {
            loadLogoActionBar(userCustomData.getAvatarUrl());
        } else {
            setActionBarIcon(ImageUtils.getRoundIconDrawable(this,
                            BitmapFactory.decodeResource(getResources(), R.drawable.placeholder_user)));
        }
    }

    private void loadLogoActionBar(String logoUrl) {
        ImageLoader.getInstance().loadImage(logoUrl, ImageLoaderUtils.UIL_USER_AVATAR_DISPLAY_OPTIONS,
                new SimpleImageLoadingListener() {

                    @Override
                    public void onLoadingComplete(String imageUri, View view, Bitmap loadedBitmap) {
                        setActionBarIcon(ImageUtils.getRoundIconDrawable(MainActivity.this, loadedBitmap));
                    }
                });
    }

    private void performImportFriendsSuccessAction() {
        appSharedHelper.saveUsersImportInitialized(true);
        hideProgress();
    }

    private void performImportFriendsFailAction(Bundle bundle) {
        performImportFriendsSuccessAction();
    }

    private void launchDialogsListFragment() {
        Log.d("MainActivity", "launchDialogsListFragment()");
        setCurrentFragment(DialogsListFragment.newInstance(),true);
        //AndroidChatService.loadDialogs(this, 1);
    }

    private void startImportFriends(){
        ImportFriendsHelper importFriendsHelper = new ImportFriendsHelper(MainActivity.this);

        if (facebookHelper.isSessionOpened()){
            importFriendsHelper.startGetFriendsListTask(true);
        } else {
            importFriendsHelper.startGetFriendsListTask(false);
        }

        hideProgress();
    }

    private class LoginChatAction implements Command {

        @Override
        public void execute(Bundle bundle) throws Exception {
            Log.i(TAG, "LoginChatAction");



             if (bundle.getInt(Consts.EXTRA_LOGIN_RESULT) == 1){
                 //performLoginChatSuccessAction(bundle);
             }
        }
    }

    private class ImportFriendsSuccessAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsSuccessAction();
        }
    }

    private class ImportFriendsFailAction implements Command {

        @Override
        public void execute(Bundle bundle) {
            performImportFriendsFailAction(bundle);
        }
    }
}