package es.vic.androidimages;

import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import android.graphics.Bitmap;
import android.net.Uri;
import android.util.Log;

/**
 * The ImageCache class implements memory cache to store images, with
 *  LRU algorithm.
 * @author Victor de Francisco
 */
public class ImageCache {

	private Map<Uri, Bitmap> cache;
	private int cacheSize;

	//Map with uri and counter per entry , counter is incremented each time a bitmap
	// with uri associated is requested
	private Map<Uri, Integer> lru;


	public ImageCache(int cacheSize){
		this.cacheSize = cacheSize;

		// Synchronized to avoid concurrency problems
		cache = Collections.synchronizedMap(new HashMap<Uri, Bitmap>(cacheSize));
		lru = Collections.synchronizedMap(new HashMap<Uri, Integer>(cacheSize));
	}

	/**
	 * This method adds a bitmap to cache.
	 * @param uri Uri used as key
	 * @param bitmap Bitmap used as value
	 */
	public void putBitmap(Uri uri, Bitmap bitmap) {
		if ((uri != null) && (bitmap != null) && (getBitmap(uri) == null)) {
			if (cache.size() >= cacheSize) {
				Uri keyLRU = getKeyLRU();
				if (keyLRU != null) {
					cache.remove(keyLRU);
					lru.remove(keyLRU);
					// Good time to run garbage collector
					System.gc();
				}
			}

			cache.put(uri, bitmap);
			lru.put(uri, new Integer(1));
		}
	}

	/**
	 * This method gets a bitmap from cache.
	 * @param uri Uri of bitmap to get
	 * @return The bitmap with given Uri
	 */
	public Bitmap getBitmap(Uri uri) {
		if ((uri != null) && (cache.containsKey(uri))) {
			Integer timesAccessed = lru.get(uri);
			lru.put(uri, timesAccessed + 1);
			Log.d("ImageCache", "Uri: " + uri.toString() + "| Accessed: " + lru.get(uri).toString());

			return cache.get(uri);
		}

		return null;
	}

	public void refreshCache() {
		cache.clear();
		lru.clear();
	}

	/**
	 * This method is used to get the Least Recent Used bitmap's Uri.
	 * @return Uri of Least Recent Used bitmap
	 */
	public Uri getKeyLRU() {
		Iterator cacheIterator = lru.entrySet().iterator();
		int timesUsed = Integer.MAX_VALUE;
		Uri lru = null;

		while (cacheIterator.hasNext()) {
			Map.Entry<Uri, Integer> entry = (Map.Entry<Uri, Integer>) cacheIterator.next();
			if (entry.getValue() < timesUsed) {
				lru = entry.getKey();
			}
		}

		return lru;
	}
}