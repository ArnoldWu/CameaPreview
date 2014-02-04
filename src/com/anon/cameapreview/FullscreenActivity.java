package com.anon.cameapreview;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.hardware.Camera.PictureCallback;
import android.os.Bundle;
import android.os.Environment;
import android.util.Log;
import android.view.Surface;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.FrameLayout;


@SuppressLint("SimpleDateFormat")
public class FullscreenActivity extends Activity {
	private final static String TAG = "FullscreenActivity";
	private Camera mCamera = null;
	private SurfaceView mPreview;
	public static final int MEDIA_TYPE_IMAGE = 1;
	public static final int MEDIA_TYPE_VIDEO = 2; 

	private PictureCallback mPicture = new PictureCallback() {

		@Override
		public void onPictureTaken(byte[] data, Camera camera) {

			File pictureFile = getOutputMediaFile(MEDIA_TYPE_IMAGE);
			if (pictureFile == null){
				Log.d(TAG, "Error creating media file, check storage permissions: ");
				return;
			}

			try {
				FileOutputStream fos = new FileOutputStream(pictureFile);
				fos.write(data);
				fos.close();
			} catch (FileNotFoundException e) {
				Log.d(TAG, "File not found: " + e.getMessage());
			} catch (IOException e) {
				Log.d(TAG, "Error accessing file: " + e.getMessage());
			}
			// keep preview
			preview(mCamera);
		}
		
	};   

	/** Create a File for saving an image or video */
	private static File getOutputMediaFile(int type){
		// To be safe, you should check that the SDCard is mounted
		// using Environment.getExternalStorageState() before doing this.

		File mediaStorageDir = new File(Environment.getExternalStoragePublicDirectory(
				Environment.DIRECTORY_PICTURES), "MyCameraApp");
		// This location works best if you want the created images to be shared
		// between applications and persist after your app has been uninstalled.

		// Create the storage directory if it does not exist
		if (! mediaStorageDir.exists()){
			if (! mediaStorageDir.mkdirs()){
				Log.d("MyCameraApp", "failed to create directory");
				return null;
			}
		}

		// Create a media file name
		String timeStamp = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
		File mediaFile;
		if (type == MEDIA_TYPE_IMAGE){
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"IMG_"+ timeStamp + ".jpg");
		} else if(type == MEDIA_TYPE_VIDEO) {
			mediaFile = new File(mediaStorageDir.getPath() + File.separator +
					"VID_"+ timeStamp + ".mp4");
		} else {
			return null;
		}

		return mediaFile;
	}

	private void preview(Camera camera){
		// Create our Preview view and set it as the content of our activity.
		mPreview = new CameraPreview(this, mCamera, setRotation());

		FrameLayout preview = (FrameLayout) findViewById(R.id.cameraPreview);
		preview.addView(mPreview);
		
	}
	
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);

		// do we have a camera?
		if (!getPackageManager()
				.hasSystemFeature(PackageManager.FEATURE_CAMERA)) {
			Log.e(TAG,"No camera on this device");
		} else {
			mCamera = Camera.open();
		}

		if(mCamera != null)
		{
			preview(mCamera);
		}

		// Add a listener to the Capture button
		Button captureButton = (Button) findViewById(R.id.capture);
		captureButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {
						mCamera.takePicture(null, null, mPicture);
					}
				}
				);	
		// Add a listener to the Capture button
		Button scanButton = (Button) findViewById(R.id.scan);
		scanButton.setOnClickListener(
				new View.OnClickListener() {
					@Override
					public void onClick(View v) {

					}
				}
				);	
	}

	private int setRotation(){
		android.hardware.Camera.CameraInfo info =
				new android.hardware.Camera.CameraInfo();

		int rotation = getWindowManager().getDefaultDisplay()
				.getRotation();
		int degrees = 0;
		switch (rotation) {
		case Surface.ROTATION_0: degrees = 0; break;
		case Surface.ROTATION_90: degrees = 90; break;
		case Surface.ROTATION_180: degrees = 180; break;
		case Surface.ROTATION_270: degrees = 270; break;
		}

		int result;
		if (info.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
			result = (info.orientation + degrees) % 360;
			result = (360 - result) % 360;  // compensate the mirror
		} else {  // back-facing
			result = (info.orientation - degrees + 360) % 360;
		}
		//	     return result;
		return 90;
	}

	@Override
	public void onResume() {
		super.onResume();

		if(mCamera == null){
			mCamera = Camera.open();
		}
		if(mCamera != null)
		{
			preview(mCamera);
		}
	}
	
	@Override
	protected void onPause() {
		super.onPause();
		releaseCamera();
	}
	private void releaseCamera(){
		if (mCamera != null){
			mCamera.release();        // release the camera for other applications
			mCamera = null;
		}
	}
}
