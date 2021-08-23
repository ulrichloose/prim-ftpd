package org.primftpd;

import android.content.Context;

import androidx.multidex.MultiDexApplication;

import org.primftpd.log.CsvLoggerFactory;

public class PftpdApp extends MultiDexApplication {
    @Override
    public void onCreate() {
        super.onCreate();

        // init static context to be able to create log file in scoped dir
        Context context = getApplicationContext();
        CsvLoggerFactory.CONTEXT = context;
    }
}
