package com.sendtosmarttube;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;

import java.io.IOException;
import java.util.concurrent.TimeUnit;
import java.util.regex.Pattern;

import io.github.muntashirakon.adb.AbsAdbConnectionManager;

public final class SendToSmartTubeActivity extends Activity {
    private static final Pattern YOUTUBE_LINK_PATTERN = Pattern.compile("^https://www\\.youtube\\.com/watch\\?v=[a-zA-Z0-9_-]{11}$");

    @Override
    protected void onCreate(final Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        moveTaskToBack(true);

        final Intent intent = getIntent();

        if (Intent.ACTION_SEND.equals(intent.getAction()) && "text/plain".equals(intent.getType()))
            handleSendText(intent);

        finishAndRemoveTask();
    }

    private void handleSendText(final Intent intent) {
        final String sharedText = intent.getStringExtra(Intent.EXTRA_TEXT);
        if (sharedText == null || !YOUTUBE_LINK_PATTERN.matcher(sharedText).matches())
            return;

        final Thread t = new Thread(() -> {
            AbsAdbConnectionManager manager = null;
            try {
                manager = AdbConnectionManager.getInstance(getApplication());
                manager.setTimeout(5, TimeUnit.SECONDS);
                if (!manager.connect("192.168.1.5", 5555))
                    throw new IOException("ADB connection failed");

                manager.openStream("exec:" + "exec /system/bin/cmd activity start -n 'com.teamsmart.videomanager.tv/com.liskovsoft.smartyoutubetv2.tv.ui.main.SplashActivity' -a android.intent.action.VIEW -d '" + sharedText.replaceAll("'", "'\\\\''") + "' -ez STANDALONE_PLAYER true\n").close();
            } catch (final Throwable th) {
                th.printStackTrace();
            } finally {
                if (manager != null) {
                    try {
                        manager.disconnect();
                    } catch (final IOException ignored) {}
                }
            }
        });
        t.start();
        try {
            t.join(10000);
        } catch (final InterruptedException ignored) {
            return;
        }

        if (t.isAlive()) {
            finishAndRemoveTask();
            System.exit(1);
        }
    }
}
