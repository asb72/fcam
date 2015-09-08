package com.tw.fcam_advanced.Service;

import android.app.PendingIntent;
import android.app.Service;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Handler;
import android.os.IBinder;
import android.tw.john.TWUtil;
import android.widget.Toast;

import com.tw.fcam_advanced.FCamActivity;


public class FcamService extends Service {
    private TWUtil twUtil;
    private final Handler mHandler;
    public int keyArg1;
    public int keyArg2;
    public boolean learning;
    public PendingIntent pi;

    public FcamService() {
        twUtil = null;
        keyArg1 = keyArg2 = -1;
        learning = false;
        mHandler = new ServiceTwHandler(this);
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.twUtil = new TWUtil(16);

        if (this.twUtil.open(new short[]{(short) 513}) != 0)
            stopSelf();
        else {
            twUtil.start();
            this.twUtil.removeHandler("FCamActivityService");
            this.twUtil.addHandler("FCamActivityService", this.mHandler);
        }

        Toast.makeText(this, "Служба FCam создана", Toast.LENGTH_SHORT).show();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        PrefsLoad();
        if (intent != null) {
            learning = intent.getBooleanExtra(FCamActivity.PARAM_LEARN, false);
            if (learning) {
                pi = intent.getParcelableExtra(FCamActivity.PARAM_INTENT);
                try {
                    pi.send(FCamActivity.STATUS_START_LEARNING);
                } catch (PendingIntent.CanceledException e) {
                    e.printStackTrace();
                }
            }
        }
        Toast.makeText(this, "Служба FCam запущена, learning = " + learning, Toast.LENGTH_SHORT).show();
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        this.twUtil.removeHandler("FCamActivityService");
        this.twUtil.close();
        this.twUtil = null;
        Toast.makeText(this, "Служба FCam остановлена", Toast.LENGTH_SHORT).show();
    }

    @Override
    public IBinder onBind(Intent intent) {
       return null;
    }

    public void PrefsLoad() {
        try {
            SharedPreferences prefs = getSharedPreferences("fcam_advanced", 0);
            keyArg1 = prefs.getInt(FCamActivity.KEY_ARG1, -1);
            keyArg2 = prefs.getInt(FCamActivity.KEY_ARG2, -1);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
