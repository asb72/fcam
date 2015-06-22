package com.tw.fcam;

import android.os.Handler;
import android.os.Message;
import android.source.Util;
import android.view.View;

class twHandler extends Handler {
    private final FCamActivity activity;

    twHandler(FCamActivity fCamActivity) {
        this.activity = fCamActivity;
    }

    public void handleMessage(Message message) {
        switch (message.what) {
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
                    return;
                }
                this.activity.imageView.setImageResource(R.drawable.warning_novideosignal);
                sendEmptyMessageDelayed(65295, 1000);
                break;
            default:
        }
    }
}
