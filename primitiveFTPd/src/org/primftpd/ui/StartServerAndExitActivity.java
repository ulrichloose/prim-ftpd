package org.primftpd.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;

import org.primftpd.util.ServicesStartStopUtil;

class Toggle { static boolean running = false;   }

public class StartServerAndExitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();
        //ServicesStartStopUtil.startServers(context);
        if (Toggle.running == false) {
            ServicesStartStopUtil.startServers(context);
            Toggle.running = true;
        } else {
            ServicesStartStopUtil.stopServers(context);
            Toggle.running = false;
        }

        // wait a short delay
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // never mind
        }
        finish();
    }
}
