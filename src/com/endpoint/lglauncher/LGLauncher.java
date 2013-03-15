package com.endpoint.lglauncher;

import org.acra.ACRA;
import org.acra.annotation.ReportsCrashes;

import android.app.Application;
import android.util.Log;

@ReportsCrashes(formKey = "dENRZUFHVzNFRENfQjFJM1JjNm9oYlE6MQ")
public class LGLauncher extends Application {
	
	public static final boolean DEBUG = true;
	public static final String APP_NAME = "LGLauncher";

	@Override
	public void onCreate() {
		super.onCreate();
		
		if (DEBUG) Log.d(APP_NAME, "in application onCreate");
		ACRA.init(this);
	}
	
	public static void reportError(Exception e) {
		ACRA.getErrorReporter().handleException(e);
	}
}
