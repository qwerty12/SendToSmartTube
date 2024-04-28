package com.sendtosmarttube;

import android.app.Application;
import android.content.Context;

import org.lsposed.hiddenapibypass.HiddenApiBypass;

public final class SendToSmartTubeApplication extends Application {
    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        HiddenApiBypass.addHiddenApiExemptions("Lcom/android/org/conscrypt/Conscrypt");
    }
}
