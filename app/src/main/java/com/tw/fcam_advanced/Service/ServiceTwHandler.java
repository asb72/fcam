package com.tw.fcam_advanced.Service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;

import com.tw.fcam_advanced.FCamActivity;

class ServiceTwHandler extends Handler {
    private FcamService fcamService;

    ServiceTwHandler(FcamService fcamService) {
        this.fcamService = fcamService;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 513:
                if (fcamService.learning) {
                    Intent intent = new Intent()
                            .putExtra(FCamActivity.KEY_ARG1, fcamService.keyArg1 = message.arg1)
                            .putExtra(FCamActivity.KEY_ARG2, fcamService.keyArg2 = message.arg2);

                    try {
                        fcamService.pi.send(fcamService, FCamActivity.STATUS_LEARNED, intent);
                    } catch (PendingIntent.CanceledException e) {
                        e.printStackTrace();
                    }
                    fcamService.learning = false;
                } else {
                    if (message.arg1 == fcamService.keyArg1 && message.arg2 == fcamService.keyArg2) {
                        Intent activity = new Intent(fcamService, FCamActivity.class);
                        activity.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        fcamService.startActivity(activity);
                    }
                }
                break;
            default:
        }
    }
}
