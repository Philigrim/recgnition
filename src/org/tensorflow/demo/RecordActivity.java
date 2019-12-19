package org.tensorflow.demo;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Formatter;
import java.util.Locale;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Camera;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.CamcorderProfile;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.view.View;
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.FrameLayout;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;

public class RecordActivity extends Activity implements LocationListener {

    private SimpleDateFormat dateAndTimeFormat;
    private TextView dateAndTime;
    private TextView rec;
    private Chronometer durationChronometer;
    private TextView speed;

    private Camera myCamera;
    private MyCameraSurfaceView myCameraSurfaceView;
    private MediaRecorder mediaRecorder;

    Button recordButton;
    boolean recording;

    /** Called when the activity is first created. */
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_record);

        recording = false;

        // check for gps permission
        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            // request for permission
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 1000);
        }else {
            //start the program if the permission is granted
            doStuff();
        }

        speed = findViewById(R.id.speed);
        this.updateSpeed(null);

        dateAndTime = (TextView) findViewById(R.id.date);
        rec = (TextView) findViewById(R.id.rec);
        durationChronometer = (Chronometer) findViewById(R.id.duration);

        dateAndTimeFormat = new SimpleDateFormat("yyyy-MM-dd   HH:mm:ss");





        //Get Camera for preview
        myCamera = getCameraInstance();
        if(myCamera == null){
            Toast.makeText(getApplicationContext(), "Fail to get Camera", Toast.LENGTH_LONG).show();
        }

        myCameraSurfaceView = new MyCameraSurfaceView(this, myCamera);
        FrameLayout myCameraPreview = (FrameLayout)findViewById(R.id.videoview);
        myCameraPreview.addView(myCameraSurfaceView);

        recordButton = (Button)findViewById(R.id.recordButton);
        recordButton.setOnClickListener(recordButtonOnClickListener);
    }

    Button.OnClickListener recordButtonOnClickListener = new Button.OnClickListener(){

        @Override
        public void onClick(View v) {
            try{
                if(recording){
                    dateAndTime.setVisibility(View.INVISIBLE);
                    durationChronometer.setVisibility(View.INVISIBLE);
                    rec.setVisibility(View.INVISIBLE);
                    speed.setVisibility(View.INVISIBLE);

                    durationChronometer.stop();

                    Toast.makeText(getApplicationContext(), "Video saved to:" + Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera/" + getFileName_CustomFormat() + ".mp4", Toast.LENGTH_LONG).show();

                    // stop recording and release camera
                    mediaRecorder.stop();  // stop the recording
                    releaseMediaRecorder(); // release the MediaRecorder object

                    //Exit after saved
                    //finish();
                    recordButton.setText("REC");
                    recording = false;
                }else{

                    dateAndTime.setVisibility(View.VISIBLE);
                    durationChronometer.setVisibility(View.VISIBLE);
                    rec.setVisibility(View.VISIBLE);
                    speed.setVisibility(View.VISIBLE);

                    durationChronometer.setBase(SystemClock.elapsedRealtime());
                    durationChronometer.start();
                    dateAndTimeTracking();

                    //Release Camera before MediaRecorder start
                    releaseCamera();

                    if(!prepareMediaRecorder()){
                        Toast.makeText(getApplicationContext(), "Fail in prepareMediaRecorder()!\n - Ended -", Toast.LENGTH_LONG).show();
                        finish();
                    }

                    mediaRecorder.start();
                    recording = true;
                    recordButton.setText("STOP");
                }
            }catch (Exception ex){
                ex.printStackTrace();
            }
        }};

    private void dateAndTimeTracking() {
        new Thread() {
            public void run() {
                while (!recording) {
                    try {
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                dateAndTime.setText(dateAndTimeFormat.format(System.currentTimeMillis()));
                            }
                        });
                        Thread.sleep(1000);
                    } catch (InterruptedException e) {
                        e.printStackTrace();
                    }
                }
            }
        }.start();
    }

    @Override
    public void onBackPressed()
    {
        Intent intent = new Intent(getApplicationContext(), DetectorActivity.class);
        startActivity(intent);
    }

    private Camera getCameraInstance(){
        Camera c = null;
        try {
            c = Camera.open(); // attempt to get a Camera instance
        }
        catch (Exception e){
            // Camera is not available (in use or does not exist)
        }
        return c; // returns null if camera is unavailable
    }

    private String getFileName_CustomFormat() {
        SimpleDateFormat sdfDate = new SimpleDateFormat("yyyy-MM-dd HH_mm_ss");
        Date now = new Date();
        String strDate = sdfDate.format(now);
        return strDate;
    }


    private boolean prepareMediaRecorder(){
        myCamera = getCameraInstance();
        
        myCamera.setDisplayOrientation(90); // preview while recording
        
        mediaRecorder = new MediaRecorder();

        myCamera.unlock();
        mediaRecorder.setCamera(myCamera);

        mediaRecorder.setAudioSource(MediaRecorder.AudioSource.CAMCORDER);
        mediaRecorder.setVideoSource(MediaRecorder.VideoSource.CAMERA);

        mediaRecorder.setOrientationHint(90); // file rotation

        mediaRecorder.setProfile(CamcorderProfile.get(CamcorderProfile.QUALITY_HIGH));



        mediaRecorder.setOutputFile(Environment.getExternalStorageDirectory() + "/" + Environment.DIRECTORY_DCIM + "/Camera/" + getFileName_CustomFormat() + ".mp4");
        //mediaRecorder.setOutputFile("/sdcard/myvideo1.mp4");
        mediaRecorder.setMaxDuration(86400000); // Set max duration 24 h.
        mediaRecorder.setMaxFileSize(2000000000); // Set max file size 2GB

        mediaRecorder.setPreviewDisplay(myCameraSurfaceView.getHolder().getSurface());

        try {
            mediaRecorder.prepare();
        } catch (IllegalStateException e) {
            releaseMediaRecorder();
            return false;
        } catch (IOException e) {
            releaseMediaRecorder();
            return false;
        }
        return true;

    }

    @Override
    protected void onPause() {
        super.onPause();
        releaseMediaRecorder();       // if you are using MediaRecorder, release it first
        releaseCamera();              // release the camera immediately on pause event
    }

    private void releaseMediaRecorder(){
        if (mediaRecorder != null) {
            mediaRecorder.reset();   // clear recorder configuration
            mediaRecorder.release(); // release the recorder object
            mediaRecorder = new MediaRecorder();
            myCamera.lock();           // lock camera for later use
        }
    }

    private void releaseCamera(){
        if (myCamera != null){
            myCamera.release();        // release the camera for other applications
            myCamera = null;
        }
    }




    /////////////// implements  LOCATION LISTENER

    @Override
    public void onLocationChanged(Location location){
        if(location != null) {
            Location myLocation = new Location(location);
            this.updateSpeed(myLocation);
        }
    }


    @Override
    public void onStatusChanged(String provider, int status, Bundle extras){
    }

    @Override
    public void onProviderEnabled(String provider){
    }

    @Override
    public void onProviderDisabled(String provider){
    }

    @SuppressLint("MissingPermission")
    private void doStuff() {
        LocationManager locationManager = (LocationManager) this.getSystemService(Context.LOCATION_SERVICE);
        if(locationManager != null) {
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER, 0, 0, this);
        }
    }

    private void updateSpeed(Location location){
        float nCurrentSpeed = 0;

        if(location !=null){
            nCurrentSpeed = location.getSpeed();
        }

        Formatter fmt = new Formatter(new StringBuilder());
        fmt.format(Locale.US, "%5.1f", nCurrentSpeed);
        String strCurrentSpeed = fmt.toString();
        strCurrentSpeed = strCurrentSpeed.replace(" ", "0");

        speed.setText(strCurrentSpeed + " km/h");

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if(requestCode == 1000) {
            if(grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                doStuff();
            } else {
                finish();
            }
        }
    }

}