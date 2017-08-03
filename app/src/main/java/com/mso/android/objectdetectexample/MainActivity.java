package com.mso.android.objectdetectexample;

import android.app.Activity;
import android.graphics.ImageFormat;
import android.graphics.Rect;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;

import java.io.IOException;
import java.util.List;

import static android.content.ContentValues.TAG;
import static java.lang.Math.abs;

public class MainActivity extends Activity implements SurfaceHolder.Callback, Camera.PreviewCallback{

    // Used to load the 'native-lib' library on application startup.
    static {
        System.loadLibrary("native-lib");
    }

    private Camera mCamera;

    private boolean cameraConnected = false;

    public native int[] objectDetectPoints(byte[] image, int width, int height);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        SurfaceView surView = (SurfaceView) findViewById(R.id.surfaceView);
        SurfaceHolder surHolder = surView.getHolder();
        surHolder.addCallback(this);
    }

    @Override
    public void onPreviewFrame(byte[] bytes, Camera camera) {

        int width = camera.getParameters().getPreviewSize().width;
        int height = camera.getParameters().getPreviewSize().height;

        int[] points = new int[8];

        int[] result = objectDetectPoints(bytes, width, height);


        Log.d("Coords", "coord 1: "+ result[0]);
        Log.d("Coords", "coord 2: "+ result[1]);
        Log.d("Coords", "coord 3: "+ result[2]);
        Log.d("Coords", "coord 4: "+ result[3]);
        Log.d("Coords", "coord 5: "+ result[4]);
        Log.d("Coords", "coord 6: "+ result[5]);
        Log.d("Coords", "coord 7: "+ result[6]);
        Log.d("Coords", "coord 8: "+ result[7]);
    }

    @Override
    public void surfaceCreated(SurfaceHolder holder) {

        try {

            for (int camIdx = 0; camIdx < Camera.getNumberOfCameras(); ++camIdx) {
                Log.d(TAG, "Trying to open camera with new open(" + Integer.valueOf(camIdx) + ")");
                try {
                    mCamera = Camera.open(camIdx);
                    cameraConnected = true;
                } catch (RuntimeException e) {
                    Log.e(TAG, "Camera #" + camIdx + "failed to open: " + e.getLocalizedMessage());
                }
                if (cameraConnected) break;
            }

            mCamera.setParameters(getCameraParameters());
            mCamera.setPreviewCallback(this);
            mCamera.setPreviewDisplay(holder);
            mCamera.startPreview();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private Camera.Parameters getCameraParameters(){

        Camera.Parameters parameters = mCamera.getParameters();

        List<String> supportedFocusModes = mCamera.getParameters().getSupportedFocusModes();
        boolean hasAutoFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_AUTO);
        boolean hasContinuosFocus = supportedFocusModes != null && supportedFocusModes.contains(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);

        if(hasAutoFocus) {
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_AUTO);
        }else if(hasContinuosFocus){
            parameters.setFocusMode(Camera.Parameters.FOCUS_MODE_CONTINUOUS_PICTURE);
        }

        parameters.setPreviewFormat(ImageFormat.NV21);

        // valores para cheque
        int targetHeight = 1440;
        float minimumProportion = 1.6f;
        List<Camera.Size> suportedSizes = parameters.getSupportedPictureSizes();
        Camera.Size bestPictureSize = getBestPictureSize(suportedSizes, targetHeight, minimumProportion);
        parameters.setPictureSize(bestPictureSize.width, bestPictureSize.height);

        suportedSizes = parameters.getSupportedPreviewSizes();
        Camera.Size bestPreviewSize = getBestSize(suportedSizes, bestPictureSize.width, bestPictureSize.height);
        parameters.setPreviewSize(bestPreviewSize.width, bestPreviewSize.height);

        for(String whiteBalanceSupported : parameters.getSupportedWhiteBalance()) {
            if (whiteBalanceSupported.equalsIgnoreCase(Camera.Parameters.WHITE_BALANCE_AUTO)){
                parameters.setAutoWhiteBalanceLock(false);
                parameters.setWhiteBalance(Camera.Parameters.WHITE_BALANCE_AUTO);
                break;
            }
        }

        parameters.setFlashMode(Camera.Parameters.FLASH_MODE_OFF);
        parameters.setExposureCompensation(0);

        return parameters;
    }

    public Camera.Size getBestSize(List<Camera.Size> suportedSizes, int width, int height){

        Camera.Size bestSize = null;
        int bestArea = 0;
        float bestRatioDiff = 9999;
        float previewRatio = (float)width / (float)height;

        for(Camera.Size size : suportedSizes)
        {
            float ratioDiff = abs(((float)size.width / (float)size.height) - previewRatio);
            int area = size.height * size.width;
            if( ratioDiff < bestRatioDiff
                    || (ratioDiff <= (bestRatioDiff + 1e-6) && area >= bestArea))
            {
                bestArea = area;
                bestSize = size;
                bestRatioDiff = ratioDiff;
            }
        }
        return bestSize;
    }


    public Camera.Size getBestPictureSize(List<Camera.Size> suportedSizes, int targetHeight, float proporcaoMinima){

        Camera.Size bestSize = null;
        float bestHeightDiff = 9999;

        for(Camera.Size size : suportedSizes)
        {
            float ratio = (float)size.width / (float)size.height;
            int heightDiff = abs(size.height - targetHeight);
            if( ratio >= proporcaoMinima && heightDiff <= bestHeightDiff)
            {
                bestHeightDiff = heightDiff;
                bestSize = size;
            }
        }
        return bestSize;
    }


    @Override
    public void surfaceChanged(SurfaceHolder surfaceHolder, int i, int i1, int i2) {

    }

    @Override
    public void surfaceDestroyed(SurfaceHolder surfaceHolder) {
        if (mCamera != null) {
            mCamera.stopPreview();
            mCamera.setPreviewCallback(null);
            mCamera.release();
            mCamera = null;
        }
    }
}
