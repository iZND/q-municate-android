package com.quickblox.qmunicate.ui.main;

import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.widget.DrawerLayout;
import android.util.Log;

import com.facebook.Session;
import com.facebook.SessionState;
import com.quickblox.qmunicate.App;
import com.quickblox.qmunicate.R;
import com.quickblox.qmunicate.core.gcm.GSMHelper;
import com.quickblox.qmunicate.ui.base.BaseActivity;
import com.quickblox.qmunicate.ui.chats.ChatsListFragment;
import com.quickblox.qmunicate.ui.importfriends.ImportFriends;
import com.quickblox.qmunicate.ui.invitefriends.InviteFriendsFragment;
import com.quickblox.qmunicate.utils.Consts;
import com.quickblox.qmunicate.utils.DialogUtils;
import com.quickblox.qmunicate.utils.FacebookHelper;
import com.quickblox.qmunicate.utils.PrefsHelper;

public class MainActivity extends BaseActivity implements NavigationDrawerFragment.NavigationDrawerCallbacks {

    private static final String TAG = MainActivity.class.getSimpleName();

    private static final int ID_FRIEND_LIST_FRAGMENT = 0;
    private static final int ID_CHATS_LIST_FRAGMENT = 1;
    private static final int ID_SETTINGS_FRAGMENT = 2;
    private static final int ID_INVITE_FRIENDS_FRAGMENT = 3;

    private Fragment currentFragment;
    private FacebookHelper facebookHelper;
    private ImportFriends importFriends;
    private boolean isImportInitialized;
    private boolean isSignUpInitialized;
    private GSMHelper gsmHelper;

    //    private GSMHelper gsmHelper;

    public static void start(Context context) {
        Intent intent = new Intent(context, MainActivity.class);
        context.startActivity(intent);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (currentFragment instanceof InviteFriendsFragment) {
            currentFragment.onActivityResult(requestCode, resultCode, data);
        } else if (facebookHelper != null) {
            facebookHelper.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        useDoubleBackPressed = true;
        isImportInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_IMPORT_INITIALIZED, false);
        isSignUpInitialized = App.getInstance().getPrefsHelper().getPref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, false);

        initNavigationDrawer();

        if (!isImportInitialized && isSignUpInitialized) {
            showProgress();
            facebookHelper = new FacebookHelper(this, savedInstanceState, new FacebookSessionStatusCallback());
            importFriends = new ImportFriends(MainActivity.this, facebookHelper);
            App.getInstance().getPrefsHelper().savePref(PrefsHelper.PREF_SIGN_UP_INITIALIZED, false);
        }

        //TODO VF Uncomment when Push woild be needed
        //checkGCMRegistration();
    }

    private void initNavigationDrawer() {
        NavigationDrawerFragment navigationDrawerFragment = (NavigationDrawerFragment) getFragmentManager().findFragmentById(R.id.navigation_drawer);
        navigationDrawerFragment.setUp(R.id.navigation_drawer, (DrawerLayout) findViewById(R.id.drawer_layout));
    }

    @Override
    protected void onResume() {
        super.onResume();
        //TODO VF Uncomment when Push woild be needed
        // gsmHelper.checkPlayServices();
    }

    @Override
    public void onNavigationDrawerItemSelected(int position) {
        Fragment fragment = null;
        switch (position) {
            case ID_FRIEND_LIST_FRAGMENT:
                fragment = FriendListFragment.newInstance();
                break;
            case ID_CHATS_LIST_FRAGMENT:
                fragment = ChatsListFragment.newInstance();
                break;
//                DialogUtils.show(this, getResources().getString(R.string.comming_soon));
//                return;
            case ID_SETTINGS_FRAGMENT:
                fragment = SettingsFragment.newInstance();
                break;
            case ID_INVITE_FRIENDS_FRAGMENT:
                fragment = InviteFriendsFragment.newInstance();
                break;
        }
        setCurrentFragment(fragment);
    }

    public void setCurrentFragment(Fragment fragment) {
        currentFragment = fragment;
        getFragmentManager().popBackStack(null, FragmentManager.POP_BACK_STACK_INCLUSIVE);
        FragmentTransaction transaction = buildTransaction();
        transaction.replace(R.id.container, fragment, null);
        transaction.commit();
    }

    private FragmentTransaction buildTransaction() {
        FragmentTransaction transaction = getFragmentManager().beginTransaction();
        transaction.setTransition(FragmentTransaction.TRANSIT_FRAGMENT_OPEN);
        return transaction;
    }

    private class FacebookSessionStatusCallback implements Session.StatusCallback {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (session.isOpened()) {
                importFriends.startGetFriendsListTask(true);
            } else if (!(!session.isOpened() && !session.isClosed())) {
                importFriends.startGetFriendsListTask(false);
                hideProgress();
            }
        }
    }

    private void checkGCMRegistration() {
        if (gsmHelper.checkPlayServices()) {
            String registrationId = gsmHelper.getRegistrationId();
            Log.i(TAG, "registrationId=" + registrationId);
            if (registrationId.isEmpty()) {
                gsmHelper.registerInBackground();
            }
            int subscriptionId = gsmHelper.getSubscriptionId();
            if (Consts.NOT_INITIALIZED_VALUE != subscriptionId) {
                gsmHelper.subscribeToPushNotifications(registrationId);
            }
        } else {
            Log.i(TAG, "No valid Google Play Services APK found.");
        }
    }
}