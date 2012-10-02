package es.vic.androidimages;

/**
 * The DownloadCallback interface used to implement the operation completion handler.
 * @author Victor de Francisco
 */
public interface DownloadCallback {
	/**
	 * This method is called when the operation has been finished.
	 * @param error Error declaration
	 */
	void done(final boolean error);
}
