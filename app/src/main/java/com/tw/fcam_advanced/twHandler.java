package com.tw.fcam_advanced;

import android.os.Handler;
import android.os.Message;
import android.source.Util;
import android.view.View;

class twHandler extends Handler {
    private final FCamActivity activity;
    private boolean closeTimerStarted;

    twHandler(FCamActivity fCamActivity) {
        this.activity = fCamActivity;
        this.closeTimerStarted = false;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
            case 65270:
                if (this.activity.layout.getVisibility() != View.VISIBLE) {
                    this.activity.finish();
                } else {
                    removeMessages(65270);
                    sendEmptyMessageDelayed(65270, 8500);
                }
                break;
            case 65280:
                this.activity.layout.setVisibility(View.INVISIBLE);
                break;
            case 65295:
                if (!this.activity.streaming) {
                    return;
                }
                if (Util.isDisplayReady(this.activity.streamDev, Util.SOURCE_CHANNEL_CVBS2)) {
                    this.activity.imageView.setImageDrawable(null);
                    sendEmptyMessageDelayed(65295, 2000);

                    this.closeTimerStarted = false;
                    removeMessages(65270);
                    return;
                }
                if (!this.closeTimerStarted) {
                    removeMessages(65270);
                    sendEmptyMessageDelayed(65270, 5000);
                    this.closeTimerStarted = true;
                }
                this.activity.imageView.setImageResource(R.drawable.warning_novideosignal);
                sendEmptyMessageDelayed(65295, 1000);
                break;
            default:
        }
    }
}
