package es.vic.androidimages;

import android.app.ActivityManager;
import android.content.Context;
import android.util.DisplayMetrics;
import android.view.WindowManager;

/**
 * The PhoneManager class manages phone features.
 * @author Victor de Francisco
 */
public class PhoneManager {

	private int heapSize;

	private int screenWidthPixels;

	private int screenHeightPixels;

	public final static int DENSITY_LOW = 120;
	public final static int DENSITY_MEDIUM = 160;
	public final static int DENSITY_HIGH = 240;
	public final static int DENSITY_XHIGH = 320;

	private int screenDensity;

	private static Context context;

	private ActivityManager activityManager;
	private static WindowManager windowManager;
	private DisplayMetrics displayMetrics;
	private static PhoneManager sharedDeviceManager;


	private PhoneManager() {
		activityManager = (ActivityManager) PhoneManager.context.getSystemService(Context.ACTIVITY_SERVICE);

		heapSize = activityManager.getMemoryClass();

		displayMetrics = new DisplayMetrics();
		PhoneManager.windowManager.getDefaultDisplay().getMetrics(displayMetrics);

		screenWidthPixels = displayMetrics.widthPixels;
		screenHeightPixels = displayMetrics.heightPixels;

		switch(displayMetrics.densityDpi) {
		case DisplayMetrics.DENSITY_LOW :
			screenDensity = PhoneManager.DENSITY_LOW;
			break;
		case DisplayMetrics.DENSITY_MEDIUM :
			screenDensity = PhoneManager.DENSITY_MEDIUM;
			break;
		case DisplayMetrics.DENSITY_HIGH :
			screenDensity = PhoneManager.DENSITY_HIGH;
			break;
		default:
			screenDensity = PhoneManager.DENSITY_XHIGH;
			break;
		}
	}

	public static void setContext(Context context) {
		PhoneManager.context = context;
	}

	// To use this class, this is the first method to call
	public static void setWindowManager(WindowManager windowManager) {
		PhoneManager.windowManager = windowManager;
	}

	public static PhoneManager getSharedDeviceManager() {
		if (PhoneManager.sharedDeviceManager == null) {
			PhoneManager.sharedDeviceManager = new PhoneManager();
		}

		return PhoneManager.sharedDeviceManager;
	}


	// Setters & getters

	public int getScreenWidthPixels() {
		return screenWidthPixels;
	}

	public int getScreenHeightPixels() {
		return screenHeightPixels;
	}

	public int getScreenDensity() {
		return screenDensity;
	}

	public int getHeapSize() {
		return heapSize;
	}
}