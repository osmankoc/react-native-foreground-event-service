
package com.ForegroundEventLib;

import android.app.Activity;
import android.app.ActivityManager;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.Log;

import com.facebook.react.bridge.Promise;
import com.facebook.react.bridge.ReactApplicationContext;
import com.facebook.react.bridge.ReactContext;
import com.facebook.react.bridge.ReactContextBaseJavaModule;
import com.facebook.react.bridge.ReactMethod;
import com.facebook.react.bridge.Callback;
import com.facebook.react.bridge.ReadableMap;
import com.facebook.react.bridge.ReadableMapKeySetIterator;
import com.facebook.react.bridge.WritableMap;
import com.facebook.react.modules.core.DeviceEventManagerModule;

import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

public class RNForegroundEventServiceModule extends ReactContextBaseJavaModule implements ForegroundEventReceiver  {

  public final static String TAG = "ForegroundEventLib";
  private Intent mForegroundServiceIntent;
  private final ReactApplicationContext mContext;
  public final static String nativeEventName = "com.ForegroundEventLib.serviceEvent";
  private final static String jsEventName = "serviceEvent";
  private static BroadcastReceiver mEventReceiver;
  private static int SERVICE_INTERVAL = 60000;


  public RNForegroundEventServiceModule(ReactApplicationContext reactContext) {
    super(reactContext);
    mForegroundServiceIntent = new Intent(reactContext, ForegroundSvc.class);
    this.mContext = reactContext;
    mForegroundServiceIntent.putExtra("title", "Foreground Service");
    mForegroundServiceIntent.putExtra("sBody", "Your application works.");
    mForegroundServiceIntent.putExtra("interval", SERVICE_INTERVAL);
    mForegroundServiceIntent.putExtra("icon", getIconId(""));
    Log.i(TAG, "Module contructed");
    createEventReceiver();
    registerEventReceiver();
  }


  @Nullable
  @Override
  public Map<String, Object> getConstants() {
    final Map<String, Object> constants = new HashMap<>();
    String[] eventslist = new String[] { jsEventName };
    constants.put("RNevents", eventslist);
    constants.put("jsEventName", jsEventName);

    return constants;
  }

  private int getIconId(String icon) {
    String packageName= mContext.getPackageName();
    if (icon == null || icon == "") {
      icon = "ic_stat_foreground";
      //packageName = "com.ForegroundEventLib";

    }
    Log.d(TAG, "Icon : " + icon);
    int iconId = mContext.getResources().getIdentifier(icon, "mipmap", packageName);
    if (iconId <= 0)
      iconId = mContext.getResources().getIdentifier(icon, "drawable", packageName);
    Log.d(TAG, "Icon Id : " + iconId);
    return iconId;
  }


//  private int getResourceIdForResourceName(String resourceName) {
//    int resourceId = mContext.getResources().getIdentifier(resourceName, "drawable", mContext.getPackageName());
//    if (resourceId == 0) {
//      resourceId = mContext.getResources().getIdentifier(resourceName, "mipmap", mContext.getPackageName());
//    }
//    return resourceId;
//  }
//

  @ReactMethod
  public void startBackgroundService(@Nullable ReadableMap conf, Promise promise) {

    if(conf != null) {

      ReadableMapKeySetIterator it = conf.keySetIterator();

      while (it.hasNextKey()) {
        String tKey = it.nextKey();
        switch (tKey) {
          case "title":
            mForegroundServiceIntent.removeExtra("title");
            mForegroundServiceIntent.putExtra("title", conf.getString(tKey));
            break;
          case "body":
            mForegroundServiceIntent.removeExtra("sBody");
            mForegroundServiceIntent.putExtra("sBody", conf.getString(tKey));
            break;
          case "icon":
            mForegroundServiceIntent.removeExtra("icon");
            mForegroundServiceIntent.putExtra("icon", getIconId(conf.getString(tKey)));
            break;
          case "interval":
            mForegroundServiceIntent.removeExtra("interval");
            SERVICE_INTERVAL = conf.getInt(tKey);
            mForegroundServiceIntent.putExtra("interval", SERVICE_INTERVAL);
            break;
        }

      }
      Log.i(TAG, "Configuration is set!");
    }



    if(isServiceRunningInForeground()) {
      Log.i(TAG, "Foreground Service is already working!");
      promise.resolve("Service is already working!");
      return;
    }
    ComponentName componentName = mContext.startService(mForegroundServiceIntent);
    if (componentName != null) {
      Log.i(TAG, "Foreground Service started");
      promise.resolve("Foreground Service started");
    } else {
      Log.e(TAG, "Foreground service is not started");
      promise.reject("ERROR_SERVICE_ERROR", "Foreground Service: Foreground service is not started");
    }
  }


  @ReactMethod
  public void stopService(Promise promise) {
    if(!isServiceRunningInForeground()) {
      //if(!EkoLocationForegroundService.isSvcRunning()) {
      Log.w(TAG, "Service is already stopped!");
      promise.resolve("Service is already stopped!");
      return;
    }
    boolean stopped = mContext.stopService(mForegroundServiceIntent);
    if (stopped) {
      Log.i(TAG, "Service stopped");
      promise.resolve("Service stopped!");
    } else {
      promise.reject("ERROR_SERVICE_ERROR", "Foreground servis: Forground service failed to stop");
    }

  }

  private boolean isServiceRunningInForeground() {
    ActivityManager manager = (ActivityManager) mContext.getSystemService(Context.ACTIVITY_SERVICE);
    for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
      if (ForegroundSvc.class.getName().equals(service.service.getClassName())) {
        if (service.foreground) {
          return true;
        }

      }
    }
    return false;
  }


  public void sendEventToJS(ReactContext reactContext, String eventName, @Nullable WritableMap params) {
    reactContext
            .getJSModule(DeviceEventManagerModule.RCTDeviceEventEmitter.class)
            .emit(eventName, params);
  }

  @Override
  public String getName() {
    return "RNForegroundEventService";
  }

  @Override
  public void createEventReceiver() {
    if(mEventReceiver != null)
      return ;
    mEventReceiver = new BroadcastReceiver() {
      @Override
      public void onReceive(Context context, Intent intent) {

        sendEventToJS(mContext, jsEventName, null);
      }
    };
  }

  @Override
  public void registerEventReceiver() {
    IntentFilter eventFilter = new IntentFilter();
    eventFilter.addAction(nativeEventName);
    mContext.registerReceiver(mEventReceiver, eventFilter);
  }
}