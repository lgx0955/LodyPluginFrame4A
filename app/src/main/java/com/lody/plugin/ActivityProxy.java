package com.lody.plugin;

import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.Bundle;

import com.lody.plugin.reflect.Reflect;
import com.lody.plugin.reflect.ReflectException;


/**
 * Created by lody  on 2015/3/24.
 */
public class ActivityProxy extends Activity {

    /**当前代理的插件 */
    private Activity plugin;
    /** 反射ActivityProxy.this实例 */
    private Reflect thisEditor;
    /** 反射正在代理的插件Activity实例 */
    private Reflect activityEditor;
    /** 代理的插件Activity类名 */
    private String pluginActivityName = LPuginConfig.DEF_PLUGIN_CLASS_NAME;
    /** 插件apk 路径 */
    private String pluginDexPath = LPuginConfig.KEY_PLUGIN_DEX_PATH;
    /** 插件的所有资源 */
    private Resources pluginResource = null;
    /** 插件的资源管理器 */
    private AssetManager pluginAssetManager = null;
    /** 插件的Theme */
    private Resources.Theme pluginTheme = null;
    /** 插件包信息 */
    private PackageInfo pluginPkgInfo = null;
    /** 插件在AndroidManifest.xml中的索引 */
    private int pluginIndex = 0;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        final Intent fromIntent = getIntent();
        final Bundle pluginMessage = fromIntent.getExtras();
        if(pluginMessage != null){
            this.pluginActivityName = pluginMessage.getString(LPuginConfig.KEY_PLUGIN_ACT_NAME, LPuginConfig.DEF_PLUGIN_CLASS_NAME);
            this.pluginDexPath = pluginMessage.getString(LPuginConfig.KEY_PLUGIN_DEX_PATH,LPuginConfig.DEF_PLUGIN_DEX_PATH);
            this.pluginIndex = pluginMessage.getInt(LPuginConfig.KEY_PLUGIN_INDEX, 0);
        }
        if(pluginDexPath == LPuginConfig.DEF_PLUGIN_DEX_PATH){
            throw new LaunchPluginException("No Plugin can be PROXY!!!");

        }

        loadPluginFromAPK();
        handleLoadPlugin();
        performLoadPlugin(savedInstanceState);


    }

    /**
     * 加载插件apk
     */
    private void loadPluginFromAPK() {
        loadPluginResources();
        loadPluginDex();
    }

    /**
     * 加载插件的dex
     */
    private void loadPluginDex() {
        try {
            pluginPkgInfo = PluginTool.getAppInfo(this, pluginDexPath);
        } catch (PackageManager.NameNotFoundException e) {
            throw new LaunchPluginException("NOT Found plugin from:" + pluginDexPath);
        }
        int activityCount = pluginPkgInfo.activities.length;
        if(activityCount <= pluginIndex && activityCount <= 0){
            throw new IndexOutOfBoundsException("Such index are lower for the count of the plugin.");
        }
        if (pluginActivityName == LPuginConfig.DEF_PLUGIN_CLASS_NAME && pluginPkgInfo.activities != null) {
            pluginActivityName = pluginPkgInfo.activities[pluginIndex].name;
        }

        Class<?> pluginClass = null;
        try {
            pluginClass = getClassLoader().loadClass(pluginActivityName);
        } catch (ClassNotFoundException e) {
            throw new LaunchPluginException(e.getMessage());
        }
        try {
            plugin = (Activity) pluginClass.newInstance();
        } catch (InstantiationException e) {
            throw new LaunchPluginException(e.getMessage());
        } catch (IllegalAccessException e) {
            throw new LaunchPluginException(e.getMessage());
        }

    }



    /**
     * 加载插件资源
     */
    private void loadPluginResources() {

        try {
            AssetManager assetManager = AssetManager.class.newInstance();
            Reflect assetEditor = Reflect.on(assetManager);
            assetEditor.call("addAssetPath",pluginDexPath);
            pluginAssetManager = assetManager;
            Resources superRes = super.getResources();
            pluginResource = new Resources(pluginAssetManager,
                    superRes.getDisplayMetrics(),
                    superRes.getConfiguration());
            pluginTheme = pluginResource.newTheme();
            pluginTheme.setTo(super.getTheme());

        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }


    private void performLoadPlugin(Bundle savedInstanceState) {
        activityEditor.call("onCreate", savedInstanceState);

    }

    private void handleLoadPlugin() {

        if(plugin == null) return;

        activityEditor = Reflect.on(plugin);
        thisEditor = Reflect.on(this);

        try {
            activityEditor.set("mBase", ActivityProxy.this);
            activityEditor.set("mWindow", this.getWindow());
            activityEditor.set("mManagedDialogs", thisEditor.get("mManagedDialogs"));
            activityEditor.set("mCurrentConfig", thisEditor.get("mCurrentConfig"));
            activityEditor.set("mSearchManager", thisEditor.get("mSearchManager"));
            activityEditor.set("mMenuInflater", thisEditor.get("mMenuInflater"));
            activityEditor.set("mConfigChangeFlags", thisEditor.get("mConfigChangeFlags"));
            activityEditor.set("mIntent", thisEditor.get("mIntent"));
            activityEditor.set("mToken", thisEditor.get("mToken"));
            activityEditor.set("mInstrumentation", thisEditor.get("mInstrumentation"));
            activityEditor.set("mMainThread", thisEditor.get("mMainThread"));
            activityEditor.set("mEmbeddedID", thisEditor.get("mEmbeddedID"));
            activityEditor.set("mApplication", thisEditor.get("mApplication"));
            activityEditor.set("mComponent", thisEditor.get("mComponent"));
            activityEditor.set("mActivityInfo", thisEditor.get("mActivityInfo"));
            activityEditor.set("mAllLoaderManagers", thisEditor.get("mAllLoaderManagers"));
            activityEditor.set("mLoaderManager", thisEditor.get("mLoaderManager"));
            if (Build.VERSION.SDK_INT >= 13) {
                //在android 3.2 以后，Android引入了Fragment.
                activityEditor.set("mFragments", thisEditor.get("mFragments"));
            }
            activityEditor.set("mUiThread", thisEditor.get("mUiThread"));
            activityEditor.set("mHandler", thisEditor.get("mHandler"));
            activityEditor.set("mInstanceTracker", thisEditor.get("mInstanceTracker"));
            activityEditor.set("mTitle", thisEditor.get("mTitle"));
            activityEditor.set("mInstanceTracker", thisEditor.get("mInstanceTracker"));
            activityEditor.set("mInstanceTracker", thisEditor.get("mInstanceTracker"));
            activityEditor.set("mInstanceTracker", thisEditor.get("mInstanceTracker"));
        }catch (ReflectException e){
           e.printStackTrace();
        }

    }



    /////////////////////////////////////////////////////////////////////////////////////////////////////////////////
    ////////////////////////////////////////////////Override////////////////////////////////////////////////////////
    ///////////////////////////////////////////////////////////////////////////////////////////////////////////////


    @Override
    public ClassLoader getClassLoader() {
        return LPluginDexLoader.getClassLoader(pluginDexPath,getApplicationContext(),
                super.getClassLoader());
    }

    @Override
    public Resources getResources() {
        return this.pluginResource == null ? super.getResources():pluginResource;
    }

    @Override
    public AssetManager getAssets() {

        return this.pluginAssetManager == null? super.getAssets() : pluginAssetManager;
    }

    @Override
    public void startActivity(Intent intent) {
        /*TODO:首先查询这个Activity是否在Host里，
        然后查询插件中是否有这个Activity，
        分为三种情况：
        第一种：Host有，插件没有，那么直接super.startActivity(intent);
        第二种：Host有，插件也有，这就需要抉择，不过比较少见，论实际还是选择启动Host的比较靠谱。
        第三种：Host和插件里面都没有，那就呵呵了：)
        */
        //插件跳转未完成
        super.startActivity(intent);
    }

    @Override
    public void startActivityForResult(Intent intent, int requestCode) {
        //插件跳转未完成
        super.startActivityForResult(intent, requestCode);
    }
}
