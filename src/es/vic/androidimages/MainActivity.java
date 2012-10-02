package es.vic.androidimages;

import android.app.Activity;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;
import android.widget.HorizontalScrollView;
import android.widget.ImageView;
import android.widget.Toast;

public class MainActivity extends Activity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		final Context context = this;

		HorizontalScrollView scrollView = (HorizontalScrollView) findViewById(R.id.scroll_view);

		PhoneManager.setWindowManager(getWindowManager());
		PhoneManager.setContext(context);

		ImageDownloadManager imageManager = ImageDownloadManager.getSharedImageDownloadManager();

		ImageView imageView = new ImageView(this);
		imageView.setImageResource(R.drawable.ic_launcher);
		imageManager.loadImage(Uri.parse("http://cf2.imgobject.com/t/p/w342//c68qlPMhdqrhrLqRGRS5yjItFK0.jpg"), imageView, new DownloadCallback() {
			@Override
			public void done(final boolean error) {
				if (!error) {
					Toast.makeText(context, "NO ERROR", Toast.LENGTH_SHORT).show();
				} else {
					Toast.makeText(context, "ERROR", Toast.LENGTH_SHORT).show();
				}
			}
		});

		scrollView.addView(imageView);
	}
}
