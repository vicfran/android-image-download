android-images
==============
Multithreaded asynchronous image download with memory caching

FILES:

 - ImageDownloadManager

    Main file of the project. Creates a pool of threads to download images and manages the downloads. Uses an image cache with fixed size, based
 on the memory available assigned to the app (heap). Images are created based on heap size, this is for memory management and performance, to avoid
 OutOfMemoryException when images were too big or memory available too short.

    Implements Singleton pattern, only one instance of this class must be created, to use this object always call getSharedImageManager() method.

    Number of threads in the pool can be configured simply changing NUM_RUNNING_THREADS constant value.

 - ImageCache

    Util class. Implements image caching with LRU (Least Recent Used) algorithm.

 - PhoneManager

    Util class. Obtains device info, like memory assigned to the app (heap), screen dimensions and density, etc.

 - DownloadCallback

    Util class. Callback to call when operations finished. For asynchronous operations.

USE:

   To use this libray, simply import the files in your project, where you want to download an image, call loadImage(...) method, passing the Uri of the
 image you wish to download, the ImageView to put the image donwloaded in, and the callback to call when the download finished.

 Example:

      ...
      private ImageDownloadManager imageDownloadManager = ImageDownloadManager.getSharedImageDownloadManager();

      if (imageDownloadManager != null) {
        imageDownloadManager.loadImage(Uri.parse("http://ExampleImageUri"), myLayoutImageView, new Callback() {
          @Override
          public void done(final boolean error) {
            if (!error) {
              Toast.make(context, "Download OK", Toast.LENGTH_SHORT).show();
            } else {
              Toast.make(context, "Download ERROR", Toast.LENGTH_SHORT).show();
            }
          }
        });
      }
      ...
