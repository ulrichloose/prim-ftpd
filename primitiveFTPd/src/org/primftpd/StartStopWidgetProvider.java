package org.primftpd;

import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.RemoteViews;
import android.media.AudioManager;

import org.primftpd.services.DownloadsService;
import org.primftpd.util.ServersRunningBean;
import org.primftpd.util.ServicesStartStopUtil;

public class StartStopWidgetProvider extends AppWidgetProvider
{
	private static final String ACTION_CLICK = "ACTION_CLICK_WIDGET";

	public static Intent buildServerStartStopIntent(Context context) {
		Intent intent = new Intent(context, StartStopWidgetProvider.class);
		intent.setAction(ACTION_CLICK);
		return intent;
	}

	private Context mContext;

	@Override
	public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
		super.onUpdate(context, appWidgetManager, appWidgetIds);

		for (int appWidgetId : appWidgetIds) {
			Intent intent = buildServerStartStopIntent(context);

			PendingIntent pendingIntent = PendingIntent.getBroadcast(
					context,0, intent, PendingIntent.FLAG_IMMUTABLE);

			RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget);
			views.setOnClickPendingIntent(R.id.widgetIcon, pendingIntent);
			appWidgetManager.updateAppWidget(appWidgetId, views);
		}
	}

	@Override
	public void onEnabled(Context context) {
		super.onEnabled(context);
	}

	@Override
	public void onDisabled(Context context) {
		super.onDisabled(context);
	}

	@Override
	public void onReceive(Context context, Intent intent) {
		super.onReceive(context, intent);
		String action = intent != null ? intent.getAction() : null;
		//logger().debug("onReceive(), action: {}", action);
		Log.d(getClass().getName(), "onReceive(), action: " + action);
		
		if (ACTION_CLICK.equals(action)) {
			ServersRunningBean serversRunningBean = ServicesStartStopUtil.checkServicesRunning(context);

			if (!serversRunningBean.atLeastOneRunning()) {
				ServicesStartStopUtil.startServers(context);
			} else {
				ServicesStartStopUtil.stopServers(context);
			}
			mContext = context;
			AudioManager am = (AudioManager)mContext.getSystemService(Context.AUDIO_SERVICE);
			am.playSoundEffect(AudioManager.FX_KEY_CLICK, 0.5F);
		} else if (DownloadsService.ACTION_STOP.equals(action)) {
			context.stopService(new Intent(context, DownloadsService.class));
		}
	}

	@Override
	public void onDeleted(Context context, int[] appWidgetIds) {
		super.onDeleted(context, appWidgetIds);
	}

	@Override
	public void onAppWidgetOptionsChanged(
			Context context,
			AppWidgetManager appWidgetManager,
			int appWidgetId,
			Bundle newOptions) {
		super.onAppWidgetOptionsChanged(context, appWidgetManager, appWidgetId, newOptions);
	}
}
