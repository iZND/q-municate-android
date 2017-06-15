package com.quickblox.q_municate;

import android.arch.persistence.room.Room;
import android.content.Context;
import android.support.multidex.MultiDexApplication;
import android.text.TextUtils;
import android.util.Log;

import com.crashlytics.android.Crashlytics;
import com.crashlytics.android.core.CrashlyticsCore;
import com.digits.sdk.android.Digits;
import com.example.q_municate_chat_service.QBChatDilogRepository;
import com.example.q_municate_chat_service.QBChatDilogRepositoryImpl;
import com.example.q_municate_chat_service.db.QbChatDialogDatabase;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.quickblox.auth.session.QBSettings;
import com.quickblox.chat.QBChatService;
import com.quickblox.core.QBHttpConnectionConfig;
import com.quickblox.core.ServiceZone;
import com.quickblox.q_municate.di.DependencyModule;
import com.quickblox.q_municate.utils.ActivityLifecycleHandler;
import com.quickblox.q_municate.utils.StringObfuscator;
import com.quickblox.q_municate.utils.helpers.ServiceManager;
import com.quickblox.q_municate.utils.helpers.SharedHelper;
import com.quickblox.q_municate.utils.image.ImageLoaderUtils;
import com.quickblox.q_municate_auth_service.QMAuthService;
import com.quickblox.q_municate_core.utils.ConstsCore;
import com.quickblox.q_municate_db.managers.DataManager;
import com.quickblox.q_municate_user_cache.QMUserCacheImpl;
import com.quickblox.q_municate_user_service.QMUserService;
import com.quickblox.q_municate_user_service.cache.QMUserCache;
import com.twitter.sdk.android.core.TwitterAuthConfig;
import com.twitter.sdk.android.core.TwitterCore;

import io.fabric.sdk.android.Fabric;

public class App extends MultiDexApplication {

    private static final String TAG = App.class.getSimpleName();

    private static App instance;
    private SharedHelper appSharedHelper;
    private SessionListener sessionListener;
    private QbChatDialogDatabase chatDialgDb;
    private DependencyModule dependencyModule;
    private QBChatDilogRepository dilogRepository;


    public static App getInstance() {
        return instance;
    }

    @Override
    public void onCreate() {

        super.onCreate();
        Log.i(TAG, "onCreate with update");
        initFabric();
        initApplication();
        registerActivityLifecycleCallbacks(new ActivityLifecycleHandler());
    }
    public DependencyModule getDependencyModule() {
        return dependencyModule;
    }

    private void initFabric() {
        Crashlytics crashlyticsKit = new Crashlytics.Builder()
                .core(new CrashlyticsCore.Builder().disabled(BuildConfig.DEBUG).build())
                .build();

        TwitterAuthConfig authConfig = new TwitterAuthConfig(
                StringObfuscator.getTwitterConsumerKey(),
                StringObfuscator.getTwitterConsumerSecret());

        Fabric.with(this,
                crashlyticsKit,
                new TwitterCore(authConfig),
                new Digits.Builder().withTheme(R.style.AppTheme).build());
    }

    private void initApplication() {
        instance = this;

        sessionListener = new SessionListener();
        getAppSharedHelper();
        initQb();
        initDb();
        initImageLoader(this);
        initServices();
    }

    private void initQb() {
        QBSettings.getInstance().init(getApplicationContext(),
                StringObfuscator.getApplicationId(),
                StringObfuscator.getAuthKey(),
                StringObfuscator.getAuthSecret());
        QBSettings.getInstance().setAccountKey(StringObfuscator.getAccountKey());

        initDomains();
        initHTTPConfig();

        QBChatService.ConfigurationBuilder configurationBuilder = new QBChatService.ConfigurationBuilder();
        configurationBuilder.setAutojoinEnabled(true);

        QBChatService.setConfigurationBuilder(configurationBuilder);
        QBChatService.setDebugEnabled(StringObfuscator.getDebugEnabled());
    }

    private void initDomains(){
        if (!TextUtils.isEmpty(getString(R.string.api_domain))) {
            QBSettings.getInstance().setEndpoints(getString(R.string.api_domain), getString(R.string.chat_domain), ServiceZone.PRODUCTION);
            QBSettings.getInstance().setZone(ServiceZone.PRODUCTION);
        }
    }

    private void initHTTPConfig(){
        QBHttpConnectionConfig.setConnectTimeout(ConstsCore.HTTP_TIMEOUT_IN_SECONDS);
        QBHttpConnectionConfig.setReadTimeout(ConstsCore.HTTP_TIMEOUT_IN_SECONDS);
    }

    private void initDb() {
        DataManager.init(this);

        chatDialgDb = Room.
                databaseBuilder(this, QbChatDialogDatabase.class, "chat_dialog.db").build();

        dilogRepository = new QBChatDilogRepositoryImpl(chatDialgDb.chatDialogDao());
    }

    private void initImageLoader(Context context) {
        ImageLoader.getInstance().init(ImageLoaderUtils.getImageLoaderConfiguration(context));
    }

    private void initServices() {
        QMAuthService.init();
        QMUserCache userCache = new QMUserCacheImpl(this);
        QMUserService.init(userCache);

        ServiceManager.getInstance();
    }

    public synchronized SharedHelper getAppSharedHelper() {
        return appSharedHelper == null
                ? appSharedHelper = new SharedHelper(this)
                : appSharedHelper;
    }

    public QbChatDialogDatabase getChatDialgDb() {
        return chatDialgDb;
    }

    public QBChatDilogRepository getDilogRepository() {
        return dilogRepository;
    }
}