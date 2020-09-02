package com.myapp.finalproject;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.app.Notification;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.Camera;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorListener;
import android.hardware.SensorManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.provider.MediaStore;
import android.telephony.SmsManager;

import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Set;

import static android.location.LocationManager.*;


public class MainActivity extends AppCompatActivity {

    private static SensorManager sensorManager;
    private static Sensor sensor;

    LocationManager locationManager;
    private TextView textView1;
    private Button Abort_btn;
    //private ImageView selectedImage;

    private float last_x, last_y, last_z;
    private float delta_x, delta_y, delta_z;
    private float x, y, z;
    private double threshold = 0;

    final int SEND_SMS_PERMISSION_REQUEST_CODE = 1;
    final int CAMERA_REQUEST_CODE=2;

    static CountDownTimer timer;
    SmsManager smsManager= SmsManager.getDefault();
    static boolean checkForAbort;

    MediaPlayer mp ;
    static FirebaseDatabase database = FirebaseDatabase.getInstance();
    static SharedPreferences mobiles;
    //static DatabaseReference Mobiles = database.getReference().child("Mobiles");
    public static DatabaseReference Notifications = database.getReference().child("Notifications");
    public static DatabaseReference CancelAlert = database.getReference().child("CancelAlert");
    List<String> Numbers=new ArrayList<>();

    private StorageReference mStorageRef;
    private ProgressDialog mProgress;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        mp=MediaPlayer.create(this,R.raw.gun);

        sensorManager = (SensorManager) getSystemService((Context.SENSOR_SERVICE));
        sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
        sensorManager.registerListener(listener, sensor, sensorManager.SENSOR_DELAY_NORMAL);


        threshold = sensor.getMaximumRange() / 2;
        Abort_btn = findViewById(R.id.button);
        textView1 = findViewById(R.id.textView2);
        Abort_btn.setEnabled(false);
        //selectedImage=findViewById(R.id.imageView);

        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.SEND_SMS,Manifest.permission.ACCESS_FINE_LOCATION,Manifest.permission.CAMERA}, SEND_SMS_PERMISSION_REQUEST_CODE);
        mStorageRef=FirebaseStorage.getInstance().getReference();
        mProgress=new ProgressDialog(this);


        mobiles= PreferenceManager.getDefaultSharedPreferences(this);
        SharedPreferences.Editor editor=mobiles.edit();
        editor.putString("Dad","6972816650");
        editor.putString("Mom","6973743002");
        editor.putString("Girlfriend","6955857329");
        editor.apply();
        Numbers.add(mobiles.getString("Dad",""));
        Numbers.add(mobiles.getString("Mom",""));
        Numbers.add(mobiles.getString("Girlfriend",""));
        timer = new CountDownTimer(30000, 1000) {

            @Override
            public void onTick(long millisUntilFinished) {
                mp.start();
                textView1.setText(getString(R.string.timer)+":"+ String.format("%d",millisUntilFinished / 1000));

            }

            @Override
            public void onFinish() {

                mp.stop();

                for (String Number : Numbers) {
                    smsManager.sendTextMessage(Number, null, "SOS", null, null);
                }
                Toast.makeText(MainActivity.this, R.string.sms, Toast.LENGTH_SHORT).show();
                if(checkForAbort){
                    for (String Number : Numbers) {
                        smsManager.sendTextMessage(Number, null, "Άκυρος ο συναγερμός.Όλα καλά.", null, null);
                    }
                    Toast.makeText(MainActivity.this, R.string.sms, Toast.LENGTH_SHORT).show();
                }

            }
        };

    }


    SensorEventListener listener= new SensorEventListener() {
        @Override
        public void onSensorChanged(SensorEvent event) {
                x = event.values[0];
                y = event.values[1];
                z = event.values[2];

                delta_x = Math.abs(last_x - x);
                delta_y = Math.abs(last_y - y);
                delta_z = Math.abs(last_z - z);

                if ((delta_x > threshold) || (delta_y > threshold) || (delta_z > threshold)) {
                    checkForAbort=false;
                    checkForAbort(checkForAbort);
                }


                last_x = event.values[0];
                last_y = event.values[1];
                last_z = event.values[2];

        }


        @Override
        public void onAccuracyChanged(Sensor sensor, int accuracy) {

        }
    };

    LocationListener locationListener=new LocationListener() {
        @Override
        public void onLocationChanged(Location location) {
            double x = location.getLongitude();
            double y = location.getLatitude();
            locationManager.removeUpdates(locationListener);
            for(String Number:Numbers) {
                String text = "Βρίσκομαι στην τοποθεσία με γεωγραφικό μήκος: " + String.valueOf(x) + " και γεωγραφικό πλάτος: " + String.valueOf(y) + " και παρατηρώ μία πυρκαγιά.";
                ArrayList<String> parts = smsManager.divideMessage(text);
                smsManager.sendMultipartTextMessage(Number, null, parts, null, null);

            }

            Toast.makeText(MainActivity.this, R.string.sms, Toast.LENGTH_SHORT).show();


            Notifications not=new Notifications("Fire",String.valueOf(x)+","+String.valueOf(y),new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            Notifications.push().setValue(not);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {

        }

        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };

    public void AbortOnClick(View view) {
        checkForAbort=true;
        checkForAbort(checkForAbort);
    }

    public void findLocation(View view) throws IOException {
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED &&
        ActivityCompat.checkSelfPermission(this, Manifest.permission.CAMERA) == PackageManager.PERMISSION_GRANTED) {
            Intent camera=new Intent(MediaStore.ACTION_IMAGE_CAPTURE);
            startActivityForResult(camera,CAMERA_REQUEST_CODE);
            locationManager.requestLocationUpdates(GPS_PROVIDER, 0, 0, locationListener);

            }
            else {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 123);

        }
    }

    public void showStatistics(View view){
        Intent intent = new Intent(this, Statistics.class);
        startActivity(intent);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode,Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode==CAMERA_REQUEST_CODE && resultCode==RESULT_OK){
            //selectedImage.setImageBitmap((Bitmap) data.getExtras().get("data"));
            String timeStamp = new SimpleDateFormat("yyyy_MM_dd_HH:mm").format(new Date());
            String imageFileName = "Fire_" + timeStamp;
            mProgress.setMessage(getString(R.string.uploading));
            mProgress.show();
            //selectedImage.setDrawingCacheEnabled(true);
            //selectedImage.buildDrawingCache();
            Bitmap bitmap = (Bitmap) data.getExtras().get("data");
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            bitmap.compress(Bitmap.CompressFormat.JPEG, 100, baos);
            byte[] bytes = baos.toByteArray();
            StorageReference filepath=mStorageRef.child("Fire Images/").child(imageFileName);
            UploadTask uploadTask=filepath.putBytes(bytes);
            uploadTask.addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                @Override
                public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                    mProgress.dismiss();
                    Toast.makeText(MainActivity.this, getString(R.string.uploaded), Toast.LENGTH_SHORT).show();
                }
            });


        }
    }
    public void checkForAbort(boolean abort){
        if(abort){
            //sensorManager.unregisterListener(listener);
            getWindow().getDecorView().setBackgroundColor(Color.WHITE);
            timer.cancel();
            mp.stop();
            textView1.setText("");
            CancelAlert.push().setValue(true);
        }
        else{
            getWindow().getDecorView().setBackgroundColor(Color.RED);
            Abort_btn.setEnabled(true);
            Notifications not = new Notifications("Fall", "null", new SimpleDateFormat("dd/MM/yyyy HH:mm").format(new Date()));
            Notifications.push().setValue(not);
            timer.start();
        }
    }

    public class Notifications{
        private String situation;
        private String position;
        private String timestamp;
        public Notifications(String situation,String Position,String Timestamp){
            this.situation=situation;
            this.position=Position;
            this.timestamp=Timestamp;

        }
        public String getSituation() {
            return situation;
        }
        public String getPosition() {
            return position;
        }
        public String getTimestamp() {
            return timestamp;
        }

        public void setSituation(String situation) {
            this.situation = situation;
        }

        public void setPosition(String position) {
            this.position = position;
        }
        public void setTimestamp(String timestamp) {
            this.timestamp = timestamp;
        }
    }





}
