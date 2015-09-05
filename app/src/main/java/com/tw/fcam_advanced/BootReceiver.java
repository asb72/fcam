package com.tw.fcam_advanced;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;

import com.tw.fcam_advanced.Service.FcamService;

public class BootReceiver extends BroadcastReceiver {
    public BootReceiver() {
    }

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.getAction().equals("android.intent.action.BOOT_COMPLETED")) {
            try {
                SharedPreferences prefs = context.getSharedPreferences("fcam_advanced", 0);

                int keyArg1 = prefs.getInt(FCamActivity.KEY_ARG1, -1);
                int keyArg2 = prefs.getInt(FCamActivity.KEY_ARG2, -1);

                if (keyArg1 != -1 && keyArg2 != -1)
                    context.startService(new Intent(context, FcamService.class));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
