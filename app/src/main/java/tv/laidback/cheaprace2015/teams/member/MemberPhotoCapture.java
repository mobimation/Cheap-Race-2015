package tv.laidback.cheaprace2015.teams.member;

import android.app.Activity;
import android.hardware.Camera;
import android.os.Bundle;
import android.util.Log;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.widget.Button;
import android.widget.Toast;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.NoSuchElementException;

import tv.laidback.cheaprace2015.R;

public class MemberPhotoCapture extends Activity {
    private final String TAG = getClass().getSimpleName();
    private SurfaceView preview=null;
    private SurfaceHolder previewHolder=null;
    private Camera camera=null;
    private boolean inPreview=false;

    private int id;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_photo_capture);
        Button buttonTakePhoto=(Button)findViewById(R.id.buttonPhotoCapture);
        buttonTakePhoto.setOnClickListener(
          new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Take photo
                camera.autoFocus(new Camera.AutoFocusCallback() {
                    @Override
                    public void onAutoFocus(boolean success, Camera camera) {
                        if (success) {
                            Log.d(TAG, "Focus obtained");
                            camera.takePicture(
                                    new Camera.ShutterCallback() {
                                        @Override
                                        public void onShutter() {
                                            Log.d(TAG, "onShutter()");
                                        }
                                    },
                                    new Camera.PictureCallback() { // RAW
                                        @Override
                                        public void onPictureTaken(byte[] data, Camera camera) {
                                            Log.d(TAG, "Raw image available");
                                        }
                                    },
                                    new Camera.PictureCallback() { // JPG
                                        @Override
                                        public void onPictureTaken(byte[] data, Camera camera) {
                                            Log.d(TAG, "JPG image available");
                                            // TODO handle image data
                                        }
                                    });
                        } else
                            Log.d(TAG, "Focusing failed, ask user to try again");
                    }
                });
            }
          }
        );
        int currentapiVersion = android.os.Build.VERSION.SDK_INT;
        if (currentapiVersion > android.os.Build.VERSION_CODES.FROYO){
            id=findFrontFacingCamera();
            Log.d(TAG, "Trying to open camera with id " + id);
            // camera = Camera.open(id);
            try {
                camera = getFrontFacingCamera();
            }
            catch( NoSuchElementException nse) {
                Log.e(TAG,"Cannot find camera: "+nse.getMessage());
            }
        } else{
            Log.d(TAG,"Trying to open camera using <= Froyo API");

            Camera.Parameters parameters = camera.getParameters();
            parameters.set("camera-id", 2);
            // Samsung Galaxy S also supports (800, 480) as front camera preview size.
            parameters.setPreviewSize(640, 480);
            camera.setParameters(parameters);
            camera = Camera.open();
        }
        preview=(SurfaceView)findViewById(R.id.portraitCameraView);

        // TODO Auto-generated method stub
        previewHolder=preview.getHolder();
        previewHolder.addCallback(surfaceCallback);
        previewHolder.setType(SurfaceHolder.SURFACE_TYPE_PUSH_BUFFERS);
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onPause() {
        if (inPreview) {
            camera.stopPreview();
        }

        camera.release();
        camera=null;
        inPreview=false;

        super.onPause();
    }



    /**
     * Open front facing camera, API level 9 or higher
     * @return Reference to opened camera
     * @throws NoSuchElementException
     */
    Camera getFrontFacingCamera() throws NoSuchElementException {
        Camera.CameraInfo cameraInfo = new Camera.CameraInfo();
        for (int cameraIndex = 0; cameraIndex < Camera.getNumberOfCameras(); cameraIndex++) {
            Camera.getCameraInfo(cameraIndex, cameraInfo);
            if (cameraInfo.facing == Camera.CameraInfo.CAMERA_FACING_FRONT) {
                try {
                    return Camera.open(cameraIndex);
                }
                catch (RuntimeException e) {
                    e.printStackTrace();
                }
            }
        }
        throw new NoSuchElementException("Can't find front camera.");
    }

    /**
     * Try to find a front facing camera for team member self portrait preparation
     * @return id of camera
     */
    private int findFrontFacingCamera() {
        int idCamera=0;
        // Look for front-facing camera, using the Gingerbread API.
        // Java reflection is used for backwards compatibility with pre-Gingerbread APIs.
        try {
            Class<?> cameraClass = Class.forName("android.hardware.Camera");
            Object cameraInfo = null;
            Field field = null;
            int cameraCount = 0;
            Method getNumberOfCamerasMethod = cameraClass.getMethod( "getNumberOfCameras" );
            if ( getNumberOfCamerasMethod != null ) {
                cameraCount = (Integer) getNumberOfCamerasMethod.invoke( null, (Object[]) null );
            }
            Class<?> cameraInfoClass = Class.forName("android.hardware.Camera$CameraInfo");
            if ( cameraInfoClass != null ) {
                cameraInfo = cameraInfoClass.newInstance();
            }
            if ( cameraInfo != null ) {
                field = cameraInfo.getClass().getField( "facing" );
            }
            Method getCameraInfoMethod = cameraClass.getMethod( "getCameraInfo", Integer.TYPE, cameraInfoClass );
            if ( getCameraInfoMethod != null && cameraInfoClass != null && field != null ) {
                for ( int camIdx = 0; camIdx < cameraCount; camIdx++ ) {
                    getCameraInfoMethod.invoke( null, camIdx, cameraInfo );
                    int facing = field.getInt( cameraInfo );
                    if ( facing == 1 ) { // Camera.CameraInfo.CAMERA_FACING_FRONT
                        try {
                            Method cameraOpenMethod = cameraClass.getMethod( "open", Integer.TYPE );
                            if ( cameraOpenMethod != null ) {
                                Log.d("TestLedActivity","Id frontale trovato: "+camIdx);
                                //camera = (Camera) cameraOpenMethod.invoke( null, camIdx );
                                idCamera=camIdx;
                            }
                        } catch (RuntimeException e) {
                            Log.e("TestLedActivity", "Camera failed to open: " + e.getLocalizedMessage());
                        }
                    }
                }
            }
        }
        catch ( ClassNotFoundException e     ) {Log.e(TAG, "ClassNotFoundException" + e.getMessage());}
        catch ( NoSuchMethodException e      ) {Log.e(TAG, "NoSuchMethodException" + e.getMessage());}
        catch ( NoSuchFieldException e       ) {Log.e(TAG, "NoSuchFieldException" + e.getMessage());}
        catch ( IllegalAccessException e     ) {Log.e(TAG, "IllegalAccessException" + e.getMessage());}
        catch ( InvocationTargetException e  ) {Log.e(TAG, "InvocationTargetException" + e.getMessage());}
        catch ( InstantiationException e     ) {Log.e(TAG, "InstantiationException" + e.getMessage());}
        catch ( SecurityException e          ) {Log.e(TAG, "SecurityException" + e.getMessage());}

        if ( camera == null ) {
            Log.d(TAG,"Trying default camera id 0");
            // Try using the pre-Gingerbread APIs to open the camera.
            idCamera=0;
        }

        return idCamera;
    }



    private Camera.Size getBestPreviewSize(int width, int height,
                                           Camera.Parameters parameters) {
        Camera.Size result=null;

        for (Camera.Size size : parameters.getSupportedPreviewSizes()) {
            if (size.width<=width && size.height<=height) {
                if (result==null) {
                    result=size;
                }
                else {
                    int resultArea=result.width*result.height;
                    int newArea=size.width*size.height;

                    if (newArea>resultArea) {
                        result=size;
                    }
                }
            }
        }

        return(result);
    }


    SurfaceHolder.Callback surfaceCallback=new SurfaceHolder.Callback() {
        public void surfaceCreated(SurfaceHolder holder) {
            try {
                camera.setPreviewDisplay(previewHolder);
            }
            catch (Throwable t) {
                Log.e(TAG,"Exception in setPreviewDisplay()", t);
                Toast.makeText(MemberPhotoCapture.this, t.getMessage(), Toast.LENGTH_LONG).show();
            }
        }

        @Override
        public void surfaceChanged(SurfaceHolder holder,
                                   int format, int width,
                                   int height) {
            Camera.Parameters parameters=camera.getParameters();
            Camera.Size size=getBestPreviewSize(width, height,
                    parameters);

            if (size!=null) {
                //parameters.set("camera-id", 0);
                parameters.setPreviewSize(size.width, size.height);
                camera.setParameters(parameters);
                camera.startPreview();
                inPreview=true;
            }
        }

        public void surfaceDestroyed(SurfaceHolder holder) {
            // no-op
        }
    };

}
