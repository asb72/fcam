package com.tw.fcam_advanced;

import android.app.Activity;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.source.Util;
import android.tw.john.TWUtil;
import android.view.Display;
import android.view.MotionEvent;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.tw.fcam_advanced.Service.FcamService;

public class FCamActivity extends Activity {
    private TWUtil twUtil;
    private int height;
    public int streamDev;
    public boolean streaming;
    public LinearLayout layout;
    public ImageView imageView;
    private TextView camColorView;
    private TextView systemColorView;
    private TextView appKeyView;
    private final Handler mHandler;
    private int width;
    private int system_color;
    private int cam_color;
    private int keyArg1;
    private int keyArg2;
    private boolean mirror;
    private SharedPreferences prefs;
    private static final int[] colors;

    final int TASK_LEARN = 1;
    public final static int STATUS_START_LEARNING = 100;
    public final static int STATUS_LEARNED = 200;

    public final static String KEY_ARG1 = "keyArg1";
    public final static String KEY_ARG2 = "keyArg2";
    public final static String PARAM_LEARN = "learn";
    public final static String PARAM_INTENT = "pendingIntent";

    static {
        colors = new int[]{Color.WHITE, Color.RED, Color.GREEN, Color.BLUE};
    }

    public FCamActivity() {
        this.twUtil = null;
        this.width = 800;
        this.height = 480;
        this.streamDev = -1;
        this.streaming = false;
        keyArg1 = keyArg2 = -1;
        this.mHandler = new twHandler(this);
    }

    private void m0a(int x, int y) {
        int x_ = (x * 255) / (this.width - 1);
        int y_ = (y * 255) / (this.height - 1);
        this.twUtil.write(
                2050,
                x_ < 128 ? x_ + ((128 - x_) / 12) : x_ - ((x_ - 128) / 12),
                y_ < 128 ? y_ + ((128 - y_) / 10) : y_ - ((y_ - 128) / 10)
        );
    }

    private void startPreview() {
        this.imageView.setImageDrawable(null);
        displayStream(true);
    }

    private void displayStream(boolean z) {
        if (z && !this.streaming) {
            Util.setChannel(this.streamDev, Util.SOURCE_CHANNEL_CVBS2);
            Util.startDisplay(this.streamDev, this.height, this.width);
            Util.turnOnOffDisplay(this.streamDev, 0, false);
            this.streaming = true;
            this.mHandler.sendEmptyMessageDelayed(65295, 1000);
        } else if (!z && this.streaming) {
            this.mHandler.removeMessages(65295);
            Util.stopDisplay(this.streamDev);
            this.streaming = false;
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (resultCode == STATUS_LEARNED && requestCode == TASK_LEARN) {
            int keyArg1 = data.getIntExtra(KEY_ARG1, -1);
            int keyArg2 = data.getIntExtra(KEY_ARG2, -1);

            try {
                SharedPreferences.Editor ed = this.prefs.edit();
                ed.putInt(KEY_ARG1, keyArg1);
                ed.putInt(KEY_ARG2, keyArg2);
                ed.commit();
            } catch (Exception e) {
                e.printStackTrace();
            }
            appKeyView.setText(getResources().getText(R.string.key_clear));
            Toast.makeText(this, "Кнопка FCam обучена на код аппаратной кнопки: " + keyArg1 + "." + keyArg2, Toast.LENGTH_SHORT).show();
        }
    }

    public void onClick(View view) {
        Intent intent;
        switch (view.getId()) {
            case R.id.home:
                intent = new Intent("android.intent.action.MAIN");
                intent.setFlags(Intent.FLAG_RECEIVER_FOREGROUND);
                intent.addCategory("android.intent.category.HOME");
                startActivity(intent);
                break;
            case R.id.back:
                finish();
                break;
            case R.id.mirror:
                this.mirror = !this.mirror;
                Util.setMirror(this.streamDev, this.mirror ? 1 : 0);

                try {
                    SharedPreferences.Editor ed = this.prefs.edit();
                    ed.putBoolean("mirror", this.mirror);
                    ed.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                Toast.makeText(getApplicationContext(), "Mirroring is " + this.mirror, Toast.LENGTH_SHORT).show();
                break;
            case R.id.cam_color:
                if (++cam_color > 3) {
                    cam_color = 0;
                }
                this.twUtil.write(272, 0xFFFFFF & colors[cam_color]);

                try {
                    SharedPreferences.Editor ed = this.prefs.edit();
                    ed.putInt("cam_color", this.cam_color);
                    ed.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                camColorView.setTextColor(colors[cam_color]);
                break;
            case R.id.system_color:
                if (++system_color > 3) {
                    system_color = 0;
                }
                this.twUtil.write(272, 0xFFFFFF & colors[system_color]);

                try {
                    SharedPreferences.Editor ed = this.prefs.edit();
                    ed.putInt("system_color", this.system_color);
                    ed.apply();
                } catch (Exception e) {
                    e.printStackTrace();
                }
                systemColorView.setTextColor(colors[system_color]);
                break;
            case R.id.app_key:
                if (keyArg1 == -1 && keyArg2 == -1) {
                    PendingIntent pi = createPendingResult(TASK_LEARN, new Intent(), 0);

                    intent = new Intent(this, FcamService.class)
                            .putExtra(PARAM_LEARN, true)
                            .putExtra(PARAM_INTENT, pi);
                    startService(intent);
                } else {
                    try {
                        SharedPreferences.Editor ed = this.prefs.edit();
                        ed.putInt(KEY_ARG1, keyArg1 = -1);
                        ed.putInt(KEY_ARG2, keyArg2 = -1);
                        ed.commit();
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    stopService(new Intent(this, FcamService.class));
                    appKeyView.setText(getResources().getText(R.string.key_setup));
                }
                break;
        }
        this.mHandler.removeMessages(65280);
        this.mHandler.sendEmptyMessageDelayed(65280, 3000);
    }

    public void onCreate(Bundle bundle) {
        super.onCreate(bundle);
        setContentView(R.layout.fcam);
        this.layout = (LinearLayout) findViewById(R.id.hb);
        this.imageView = (ImageView) findViewById(R.id.warning_image);
        this.camColorView = (TextView) findViewById(R.id.cam_color);
        this.systemColorView = (TextView) findViewById(R.id.system_color);
        this.appKeyView = (TextView) findViewById(R.id.app_key);
        Display defaultDisplay = getWindowManager().getDefaultDisplay();
        this.width = defaultDisplay.getRawWidth();
        this.height = defaultDisplay.getRawHeight();
        this.twUtil = new TWUtil();
        if (this.twUtil.open(null) != 0) {
            finish();
        }
        this.streamDev = Util.openDev(Util.DEST_DISP_CVBS_OUT);
        try {
            this.prefs = getSharedPreferences("fcam_advanced", 0);
            this.mirror = prefs.getBoolean("mirror", false);
            this.cam_color = prefs.getInt("cam_color", 0);
            if ((cam_color > 3) || (cam_color < 0)) {
                cam_color = 0;
            }
            camColorView.setTextColor(colors[cam_color]);

            this.system_color = prefs.getInt("system_color", 0);
            if ((system_color > 3) || (system_color < 0)) {
                system_color = 0;
            }
            systemColorView.setTextColor(colors[system_color]);

            keyArg1 = prefs.getInt(KEY_ARG1, -1);
            keyArg2 = prefs.getInt(KEY_ARG2, -1);
            appKeyView.setText(getResources().getText(
                    (keyArg1 == -1 && keyArg2 == -1) ? R.string.key_setup : R.string.key_clear
            ));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    protected void onDestroy() {
        Util.closeDev(this.streamDev);
        this.streamDev = -1;
        this.twUtil.close();
        this.twUtil = null;
        super.onDestroy();
    }

    protected void onNewIntent(Intent intent) {
        setIntent(intent);
    }

    protected void onPause() {
        this.twUtil.removeHandler("FCamActivity1");
        displayStream(false);

        if (this.mirror)
            Util.setMirror(this.streamDev, 0);

        this.twUtil.write(272, 0xFFFFFF & colors[system_color]);
        super.onPause();
    }

    protected void onResume() {
        super.onResume();

        this.twUtil.addHandler("FCamActivity1", this.mHandler);
        this.twUtil.write(769, 192, 6);

        startPreview();

        Util.setMirror(this.streamDev, this.mirror ? 1 : 0);
        this.twUtil.write(272, 0xFFFFFF & colors[cam_color]);
    }

    public boolean onTouchEvent(MotionEvent motionEvent) {
        switch (motionEvent.getActionMasked()) {
            case MotionEvent.ACTION_DOWN:
                m0a((int) motionEvent.getX(), (int) motionEvent.getY());
                if (motionEvent.getY() < 391.0f && this.layout.getVisibility() != View.VISIBLE) {
                    this.layout.setVisibility(View.VISIBLE);
                    this.mHandler.removeMessages(65280);
                    this.mHandler.sendEmptyMessageDelayed(65280, 3000);
                    break;
                }
        }
        return super.onTouchEvent(motionEvent);
    }
}