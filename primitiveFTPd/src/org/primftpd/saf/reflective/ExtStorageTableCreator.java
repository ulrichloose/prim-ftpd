package org.primftpd.saf.reflective;

import java.io.File;
import java.util.List;

import org.primftpd.PrimitiveFtpdActivity;
import org.primftpd.R;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import android.app.Activity;
import android.content.Intent;
import android.content.UriPermission;
import android.net.Uri;
import android.os.Build;
import android.widget.TableLayout;

/**
 * Creates table to show external storage paths in main activity layout. Uses
 * lots of java reflection to be backwards compatible to android 4.1.
 *
 */
public class ExtStorageTableCreator
{
	public static final int VERSION_CODE_4_4 = 19;

	public static final String APP_DIR_ON_EXT_STORAGE =
		"/Android/data/org.primftpd/files";

	protected Logger logger = LoggerFactory.getLogger(getClass());

	public void createTable(
		PrimitiveFtpdActivity activity,
		TableLayout table)
	{
		// SAF API is available since android 4.4
		if (Build.VERSION.SDK_INT < VERSION_CODE_4_4) {
			return;
		}

		// create header line
		activity.createTableRow(
    		table,
    		activity.getText(R.string.externalStoragePath),
    		activity.getText(R.string.permission));

		try {
			// get persisted URI permissions
//      	activity.getContentResolver().getPersistedUriPermissions();
			List<UriPermission>  perms = ReflectionUtil.cast(
				ReflectionUtil.invokeMethod(
					activity.getContentResolver(),
					"getPersistedUriPermissions"));
			// TODO store a persistent mapping of path to uri

			// TODO use 'inofficial' way to include USB devices
			// note: ContextCompat requires android-support-lib
//	    	File[] dirs = ContextCompat.getExternalFilesDirs(
//	    		activity.getApplicationContext(),
//	    		null);
			File[] dirs = (File[]) ReflectionUtil.invokeMethod(
				activity.getApplicationContext(),
				"getExternalFilesDirs");

	    	for (File dir : dirs) {
	    		String path = dir.getAbsolutePath();
	    		if (path.endsWith(APP_DIR_ON_EXT_STORAGE)) {
	    			path = path.substring(0, path.indexOf(APP_DIR_ON_EXT_STORAGE));
	    		}
	    		activity.createTableRow(
		    		table,
		    		path,
		    		"");


    			// TODO some UI to invoke this
        		//Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT_TREE);
				String intentOpenDocTree = (String) ReflectionUtil.getConstant(
					Intent.class,
					"ACTION_OPEN_DOCUMENT_TREE");
				Intent intent = new Intent(intentOpenDocTree);
	    	    intent.addCategory(Intent.CATEGORY_OPENABLE);
				activity.startActivityForResult(
					intent,
					PrimitiveFtpdActivity.ACTIVITY_RESULT_OPEN_DOC_TREE);
	    	}
		} catch (Exception e) {
			logger.debug("", e);
		}

	}

	public void onActivityResult(
		PrimitiveFtpdActivity activity,
		int requestCode,
		int resultCode,
		Intent data)
    {
    	if (resultCode == Activity.RESULT_OK)
    	{
    		Uri uri = null;
            if (data != null) {
                uri = data.getData();
                final int takeFlags = data.getFlags()
                    & (Intent.FLAG_GRANT_READ_URI_PERMISSION
                    | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);

//                activity.getContentResolver().takePersistableUriPermission(
//                	uri,
//                	takeFlags);
        		try {
	                ReflectionUtil.invokeMethod(
	                	activity.getContentResolver(),
	            		"takePersistableUriPermission",
	            		new Object[]{uri, Integer.valueOf(takeFlags)});
				} catch (Exception e) {
					logger.debug("", e);
				}

            }
    	}
    }

}
