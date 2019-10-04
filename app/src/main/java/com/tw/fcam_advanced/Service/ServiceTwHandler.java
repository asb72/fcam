package com.tw.fcam_advanced.Service;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Handler;
import android.os.Message;
import com.tw.fcam_advanced.FCamActivity;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;

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
                if (message.arg1 == 2 && message.arg2 == 90) {
                    new Thread() {
                        @Override
                        public void run() {
                            try {
                                try {
                                    URL url;
                                    try {
                                        url = new URL("http://37.60.16.2/open_0");
                                    } catch (MalformedURLException e) {
                                        throw new RuntimeException("Не удалось созать URL");
                                    }
                                    HttpURLConnection con;
                                    try {
                                        con = (HttpURLConnection) url.openConnection();
                                    } catch (IOException e) {
                                        throw new RuntimeException("Не удалось создать соединение");
                                    }

                                    try {
                                        if (con.getResponseCode() == HttpURLConnection.HTTP_OK) {
                                            //  Toast.makeText(this, "open", Toast.LENGTH_SHORT).show();
                                        } else {
                                            ;
                                        }
                                    } catch (IOException e) {
                                        throw new RuntimeException("Не удалось отправить запрос");
                                    }
                                } catch (Exception e) {
                                    throw e; //TODO запись в лог
                                }
                            } catch (Exception e) {
                                e.printStackTrace();
                            }
                        }
                    }.start();
                }
                break;
            default:
        }
    }
}
