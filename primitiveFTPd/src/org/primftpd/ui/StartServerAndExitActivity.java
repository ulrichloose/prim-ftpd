package org.primftpd.ui;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import org.primftpd.util.ServersRunningBean;
import org.primftpd.util.ServicesStartStopUtil;
import android.media.AudioManager;

public class StartServerAndExitActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Context context = getApplicationContext();

        ServersRunningBean serversRunningBean = ServicesStartStopUtil.checkServicesRunning(context);
        if (!serversRunningBean.atLeastOneRunning()) {
            ServicesStartStopUtil.startServers(context);
        } else {
            ServicesStartStopUtil.stopServers(context);
        }

        AudioManager am = (AudioManager)getSystemService(Context.AUDIO_SERVICE);
        am.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5F);

        // wait a short delay
        try {
            Thread.sleep(20);
        } catch (InterruptedException e) {
            // never mind
        }
        finish();
    }
}
