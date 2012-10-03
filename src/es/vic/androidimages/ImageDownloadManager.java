package es.vic.androidimages;

import java.io.InputStream;
import java.lang.ref.SoftReference;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.HttpStatus;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.impl.client.DefaultHttpClient;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.Handler;
import android.widget.ImageView;

/**
 * The ImageDownloadManager manages image download.
 * @author Victor de Francisco
 */
public class ImageDownloadManager {

	private final ImageDownloaderService imageDownloaderService;
	private final ExecutorService threadPool;
	private ImageFetcher imageFetcher;

	private Uri imageUri;

	private int memoryAvailable;

	private ImageCache cache;

	private ImageView imageView;

	private PhoneManager deviceManager;

	private static ImageDownloadManager sharedImageManager;

	private Handler handler;

	/**
	 * This method calculates the cache size needed with given memoryAvailable.
	 * @param memoryAvailable Memory assigned to the application
	 * @return Cache size
	 */
	private int calculateCacheSize(int memoryAvailable) {
		if (memoryAvailable == 0) {
			return 0;
		}
		else {
			// Let 2/3 of memory available to store images
			return (((memoryAvailable * 2)/3));
		}
	}

	/**
	 * This method gets a bitmap from cache with given Uri as key.
	 * @param uri Uri of bitmap to get from cache
	 * @return Bitmap with given Uri
	 */
	private Bitmap getBitmapFromCache(Uri uri) {
		if (uri != null){
			return cache.getBitmap(uri);
		}

		return null;
	}

	/**
	 * This method adds a bitmap to cache.
	 * @param uri Uri of bitmap
	 * @param bitmap The bitmap
	 */
	private void addBitmapToCache(Uri uri, Bitmap bitmap) {
		if ((uri != null) && (bitmap != null)) {
			cache.putBitmap(uri, bitmap);
		}
	}

	private ImageDownloadManager() {
		deviceManager = PhoneManager.getSharedDeviceManager();

		memoryAvailable = deviceManager.getHeapSize();
		cache = new ImageCache(calculateCacheSize(memoryAvailable));

		imageDownloaderService = ImageDownloaderService.instance();
		threadPool = imageDownloaderService.getExecutorService();

		handler = new Handler();
	}

	/**
	 * This method is used to instantiate the class, cause implements Singleton pattern.
	 * There must be only one instance of the class
	 * @return ImageDownloadManager instance
	 */
	public static ImageDownloadManager getSharedImageDownloadManager() {
		if (ImageDownloadManager.sharedImageManager == null) {
			ImageDownloadManager.sharedImageManager = new ImageDownloadManager();
		}

		return ImageDownloadManager.sharedImageManager;
	}

	/**
	 * This method loads in the given ImageView an image downloaded from the given Uri.
	 * @param uri Uri to download the image from
	 * @param imageView ImageView to set the image downloaded
	 * @param callback Callback to call when download has finished
	 */
	public void loadImage(final Uri uri, ImageView imageView, DownloadCallback callback) {

		if ((uri != null) && (android.util.Patterns.WEB_URL.matcher(uri.toString()).matches()) && (imageView != null)){

			imageUri = uri;

			this.imageView = imageView;

			// First search bitmap in cache
			Bitmap bitmap = getBitmapFromCache(imageUri);

			if (bitmap != null) {
				// Cover located in cache, use it
				imageView.setImageBitmap(bitmap);
			} else {
				// Cover don't located in cache, download it
				imageFetcher = new ImageFetcher(imageUri, imageView, callback);
				threadPool.execute(imageFetcher);
			}
		}
	}

	/**
	 * This class implements a thread pool to download images simultaneously.
	 * @author Victor de Francisco
	 */
	private static final class ImageDownloaderService {

		// Number of threads in the pool
		private static final int NUM_RUNNING_THREADS = 10;

		private static class SingletonHolder {
			private static final ImageDownloaderService INSTANCE = new ImageDownloaderService();
		}

		private SoftReference<ExecutorService> executorServiceReference = new SoftReference<ExecutorService>(
				createExecutorService());

		private ImageDownloaderService() {
		}

		public static ImageDownloaderService instance() {
			return SingletonHolder.INSTANCE;
		}

		public ExecutorService getExecutorService() {
			ExecutorService executorService = executorServiceReference.get();

			if (executorService == null) {
				executorService = createExecutorService();
				executorServiceReference = new SoftReference<ExecutorService>(executorService);
			}

			return executorService;
		}

		private ExecutorService createExecutorService() {
			return Executors.newFixedThreadPool(ImageDownloaderService.NUM_RUNNING_THREADS);
		}
	}

	/**
	 * This class implements a runnable, the thread that performs image download.
	 * @author Victor de Francisco
	 */
	private class ImageFetcher implements Runnable {

		private String bitmapUrl = "";

		private boolean isRunning;
		private boolean error;

		private final Uri uri;
		private final ImageView imageView;
		private final DefaultHttpClient client;
		private final HttpGet getRequest;

		private DownloadCallback callback;

		public ImageFetcher(Uri uri, ImageView imageView, DownloadCallback callback) {
			this.uri = uri;
			this.imageView = imageView;
			bitmapUrl = uri.toString();

			this.callback = callback;

			client = new DefaultHttpClient();
			getRequest = new HttpGet(uri.toString());
		}

		@Override
		public void run() {
			InputStream inputStream = null;
			Bitmap bitmap = null;

			try {
				HttpResponse response = client.execute(getRequest);
				final int statusCode = response.getStatusLine().getStatusCode();

				if (statusCode != HttpStatus.SC_OK) {
					error = true;
				} else {
					error = false;
				}

				final HttpEntity entity = response.getEntity();

				if (entity != null) {
					inputStream = null;
					try {
						inputStream = entity.getContent();
						bitmap = decodeBitmap(inputStream);
					} finally {
						if (inputStream != null) {
							inputStream.close();
						}

						// After download bitmap, add it to cache
						addBitmapToCache(uri, bitmap);

						entity.consumeContent();
					}
				}

			} catch (Exception e) {
			} finally {
				final Bitmap imageBitmap = bitmap;
				handler.post(new Runnable() {
					@Override
					public void run() {
						if (imageBitmap != null) {
							imageView.setImageBitmap(imageBitmap);
							callback.done(false);
						} else {
							callback.done(true);
						}
					}
				});
			}
		}
	}

	/**
	 * This method calculates sample size, it depends on heap size.
	 * @return Victor de Francisco
	 */
	private int calculateInSampleSize() {
		int heapSize = deviceManager.getHeapSize();
		int sampleSize = 1;

		if (heapSize <= 16) {
			sampleSize = 5;
		} else if (heapSize <= 32) {
			sampleSize = 3;
		} else if (heapSize <=64) {
			sampleSize = 2;
		}

		return sampleSize;
	}

	/**
	 * This method decodes a stream to build a bitmap.
	 * @param stream Stream of bytes to build the bitmap
	 * @return Bitmap built
	 */
	private Bitmap decodeBitmap(InputStream stream) {
		BitmapFactory.Options options = new BitmapFactory.Options();

		// It depends on device's features
		options.inScaled = true;
		options.inTargetDensity = deviceManager.getScreenDensity();
		options.outWidth = deviceManager.getScreenWidthPixels();
		options.outHeight = deviceManager.getScreenHeightPixels();

		options.inSampleSize = calculateInSampleSize();

		return BitmapFactory.decodeStream(stream, null, options);
	}
}
