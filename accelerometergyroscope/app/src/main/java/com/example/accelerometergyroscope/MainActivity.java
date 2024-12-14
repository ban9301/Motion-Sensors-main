package com.example.accelerometergyroscope;

import static android.content.ContentValues.TAG;
import static android.util.Half.EPSILON;
import static java.lang.Math.cos;
import static java.lang.Math.sin;
import static java.lang.Math.sqrt;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.content.FileProvider;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.icu.text.SimpleDateFormat;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.StrictMode;
import android.util.Log;
import android.view.View;
import android.widget.Button;
//import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Random;
import java.util.logging.Logger;


public class MainActivity extends AppCompatActivity  implements SensorEventListener {
    //private SensorManager ACCELEROMETER_sensorManager;
    //private SensorManager Gyroscope_sensorManager;
    //private Sensor mLight;
    //private boolean isGyroscope;
    //private boolean isAccelerometer;
    public StringBuffer csvText;
    private Button Btn_SENSOR_DELAY_low;
    private Button Btn_SENSOR_DELAY_normal;
    private Button Btn_SENSOR_DELAY_High;
    private Button Btn_SENSOR_DELAY_Very_High;
    private Button Btn_save_with_csv;
    private Button Btn_start;
    private Button Btn_Pause;

    private int count = 0;
    private SensorManager sensorManager;

    private Sensor ACCELEROMETER_sensor;
    private Sensor Gyroscope_sensor;

    private boolean isWriting = false;
    private boolean start = false;

    //Gyroscope
    // Create a constant to convert nanoseconds to seconds.
    private static final float NS2S = 1.0f / 1000000000.0f;
    private final float[] deltaRotationVector = new float[4];
    private float timestamp;
    //*****//

    TextView TextView_x_ACCELEROMETER;
    TextView TextView_y_ACCELEROMETER;
    TextView TextView_z_ACCELEROMETER;

    TextView TextView_x_GYROSCOPE;
    TextView TextView_y_GYROSCOPE;
    TextView TextView_z_GYROSCOPE;

    //EditText Editext_input;
    //gravity
    private float[] gravity = new float[3];
    private float[] linear_acceleration = new float[3];
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        StrictMode.VmPolicy.Builder builder = new StrictMode.VmPolicy.Builder();
        StrictMode.setVmPolicy(builder.build());
        builder.detectFileUriExposure();


      /**get the permission of read and read csv file*/
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M
                && checkSelfPermission(Manifest.permission.WRITE_EXTERNAL_STORAGE)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, 1000);
        }

        //set sensor Manager
        sensorManager = (SensorManager) getSystemService(Context.SENSOR_SERVICE);

        //set Gyroscope & ACCELEROMETER sensor
        Gyroscope_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        ACCELEROMETER_sensor = sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);

        // register Listener for the Gyroscope & ACCELEROMETER sensor
        sensorManager.registerListener(this, Gyroscope_sensor, 50000);
        sensorManager.registerListener(this, ACCELEROMETER_sensor, 50000);

        //set button
        Btn_SENSOR_DELAY_Very_High =(Button) findViewById(R.id.btn_SENSOR_DELAY_Very_High);
        Btn_SENSOR_DELAY_High =(Button) findViewById(R.id.btn_SENSOR_DELAY_High);
        Btn_SENSOR_DELAY_normal =(Button) findViewById(R.id.btn_SENSOR_DELAY_normal);
        Btn_SENSOR_DELAY_low =(Button) findViewById(R.id.btn_SENSOR_DELAY_low);
        Btn_save_with_csv = (Button) findViewById(R.id.btn_save_with_csv);
        Btn_start = (Button) findViewById(R.id.btn_start);
        Btn_Pause = (Button) findViewById(R.id.btn_Pause);
        //Editext_input = (EditText) findViewById(R.id.editext_input);

        //set ACCELEROMETER_textView
        TextView_x_GYROSCOPE= (TextView) findViewById(R.id.TextView_X_ACCELEROMETER);
        TextView_y_GYROSCOPE = (TextView) findViewById(R.id.TextView_Y_ACCELEROMETER);
        TextView_z_GYROSCOPE = (TextView) findViewById(R.id.TextView_Z_ACCELEROMETER);

        //set ACCELEROMETER_textView
        TextView_x_ACCELEROMETER= (TextView) findViewById(R.id.TextView_X_GYROSCOPE);
        TextView_y_ACCELEROMETER = (TextView) findViewById(R.id.TextView_Y_GYROSCOPE);
        TextView_z_ACCELEROMETER = (TextView) findViewById(R.id.TextView_Z_GYROSCOPE);

        //button SENSOR change delay
        Btn_SENSOR_DELAY_Very_High.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorManager.registerListener(MainActivity.this, Gyroscope_sensor, SensorManager.SENSOR_DELAY_NORMAL);
                sensorManager.registerListener(MainActivity.this, ACCELEROMETER_sensor, SensorManager.SENSOR_DELAY_NORMAL);
            }
        });
        //button SENSOR change delay
        Btn_SENSOR_DELAY_High.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorManager.registerListener(MainActivity.this, Gyroscope_sensor, SensorManager.SENSOR_DELAY_UI);
                sensorManager.registerListener(MainActivity.this, ACCELEROMETER_sensor, SensorManager.SENSOR_DELAY_UI);
            }
        });
        //button SENSOR change delay
        Btn_SENSOR_DELAY_normal.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorManager.registerListener(MainActivity.this, Gyroscope_sensor, SensorManager.SENSOR_DELAY_GAME);
                sensorManager.registerListener(MainActivity.this, ACCELEROMETER_sensor, SensorManager.SENSOR_DELAY_GAME);
            }
        });
        //button SENSOR change delay
        Btn_SENSOR_DELAY_low.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorManager.registerListener(MainActivity.this, Gyroscope_sensor, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, ACCELEROMETER_sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });
        Btn_SENSOR_DELAY_low.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                // Do something in response to button click
                sensorManager.registerListener(MainActivity.this, Gyroscope_sensor, SensorManager.SENSOR_DELAY_FASTEST);
                sensorManager.registerListener(MainActivity.this, ACCELEROMETER_sensor, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });
        //save button
        Btn_save_with_csv.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start = true;
                isWriting = true;

            }
        });
        //Start button
        Btn_start.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start = true;
            }
        });
        //Pause button
        Btn_Pause.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                start = false;
                isWriting = false;
            }
        });
    }


    //when sensor changed
    public void onSensorChanged(SensorEvent event) {
        if(start == true) {
            String acc_string_xValue;
            String acc_string_yValue;
            String acc_string_zValue;
            String GYROSCOPE_string_xValue = null;
            String GYROSCOPE_string_yValue = null;
            String GYROSCOPE_string_zValue = null;
            //if(isGyroscope != true) {
            // This timestep's delta rotation to be multiplied by the current rotation
            // after computing it from the gyro sample data.
            if (timestamp != 0) {
                final float dT = (event.timestamp - timestamp) * NS2S;
                // Axis of the rotation sample, not normalized yet.
                float axisX = event.values[0];
                float axisY = event.values[1];
                float axisZ = event.values[2];

                // Calculate the angular speed of the sample
                float omegaMagnitude = (float) sqrt(axisX * axisX + axisY * axisY + axisZ * axisZ);

                // Normalize the rotation vector if it's big enough to get the axis
                // (that is, EPSILON should represent your maximum allowable margin of error)
                if (omegaMagnitude > EPSILON) {
                    axisX /= omegaMagnitude;
                    axisY /= omegaMagnitude;
                    axisZ /= omegaMagnitude;
                }

                // Integrate around this axis with the angular speed by the timestep
                // in order to get a delta rotation from this sample over the timestep
                // We will convert this axis-angle representation of the delta rotation
                // into a quaternion before turning it into the rotation matrix.
                float thetaOverTwo = omegaMagnitude * dT / 2.0f;
                float sinThetaOverTwo = (float) sin(thetaOverTwo);
                float cosThetaOverTwo = (float) cos(thetaOverTwo);
                deltaRotationVector[0] = sinThetaOverTwo * axisX;
                deltaRotationVector[1] = sinThetaOverTwo * axisY;
                deltaRotationVector[2] = sinThetaOverTwo * axisZ;
                deltaRotationVector[3] = cosThetaOverTwo;
                axisX = deltaRotationVector[0];
                axisY = deltaRotationVector[1];
                axisZ = deltaRotationVector[2];
                GYROSCOPE_string_xValue = Float.toString(axisX);
                GYROSCOPE_string_yValue = Float.toString(axisY);
                GYROSCOPE_string_zValue = Float.toString(axisZ);

                TextView_x_GYROSCOPE.setText(GYROSCOPE_string_xValue);
                TextView_y_GYROSCOPE.setText(GYROSCOPE_string_yValue);
                TextView_z_GYROSCOPE.setText(GYROSCOPE_string_zValue);
            }
            timestamp = event.timestamp;
            float[] deltaRotationMatrix = new float[9];
            sensorManager.getRotationMatrixFromVector(deltaRotationMatrix, deltaRotationVector);
            // User code should concatenate the delta rotation we computed with the current rotation
            // in order to get the updated rotation.
            // rotationCurrent = rotationCurrent * deltaRotationMatrix;
            //}
            //if(isAccelerometer != true){
            final float alpha = (float) 0.8;

            // Isolate the force of gravity with the low-pass filter.
            gravity[0] = alpha * gravity[0] + (1 - alpha) * event.values[0];
            gravity[1] = alpha * gravity[1] + (1 - alpha) * event.values[1];
            gravity[2] = alpha * gravity[2] + (1 - alpha) * event.values[2];

            // Remove the gravity contribution with the high-pass filter.

            linear_acceleration[0] = event.values[0] - gravity[0];
            linear_acceleration[1] = event.values[1] - gravity[1];
            linear_acceleration[2] = event.values[2] - gravity[2];

            float acceleration_xValue = linear_acceleration[0];
            float acceleration_yValue = linear_acceleration[1];
            float acceleration_zValue = linear_acceleration[2];

            acc_string_xValue = Float.toString(acceleration_xValue);
            acc_string_yValue = Float.toString(acceleration_yValue);
            acc_string_zValue = Float.toString(acceleration_zValue);
            TextView_x_ACCELEROMETER.setText(acc_string_xValue);
            TextView_y_ACCELEROMETER.setText(acc_string_yValue);
            TextView_z_ACCELEROMETER.setText(acc_string_zValue);
            String finalGYROSCOPE_string_xValue = GYROSCOPE_string_xValue;
            String finalGYROSCOPE_string_yValue = GYROSCOPE_string_yValue;
            String finalGYROSCOPE_string_zValue = GYROSCOPE_string_zValue;
            String[] title = {"time", "x_ACCELEROMETER", "y_ACCELEROMETER", "z_ACCELEROMETER", "x_GYROSCOPE", "y_GYROSCOPE", "z_GYROSCOPE"};

            csvText = new StringBuffer();
            for (int i = 0; i < title.length; i++) {
                csvText.append(title[i] + ",");
            }

            Calendar calendar = Calendar.getInstance();
            String currentTime = Integer.toString(calendar.get(Calendar.MINUTE));
            csvText.append("\n" + currentTime + "," + acc_string_xValue + "," + acc_string_yValue + "," + acc_string_zValue + "," + finalGYROSCOPE_string_xValue + "," + finalGYROSCOPE_string_yValue + "," + finalGYROSCOPE_string_zValue);
            Log.d(TAG, "makeCSV: \n" + csvText);//check the result of the output


            /**create csv file*/

            if (isWriting == true) {
                count +=1;
                String fileName = "[" + count + "]acc&gyr.csv";
                FileOutputStream out = null;
                try {
                    out = openFileOutput(fileName, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    out.write((csvText.toString().getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    out.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                File fileLocation = new File(Environment.getExternalStorageDirectory().getAbsolutePath(), fileName);
                FileOutputStream fos = null;
                try {
                    fos = new FileOutputStream(fileLocation);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    fos.write(csvText.toString().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
                Uri path = Uri.fromFile(fileLocation);
                FileOutputStream output = null;
                try {
                    output = openFileOutput(fileName, Context.MODE_PRIVATE);
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                }
                try {
                    output.write((csvText.toString().getBytes()));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                try {
                    output.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
                //writing the data to a CSV fil
                Intent fileIntent = new Intent(Intent.ACTION_SEND);
                fileIntent.setType("text/csv");
                fileIntent.putExtra(Intent.EXTRA_SUBJECT, "Calendar Data");
                fileIntent.putExtra(Intent.EXTRA_STREAM, path);
                fileIntent.addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION | Intent.FLAG_GRANT_WRITE_URI_PERMISSION);
                saveFile();
                isWriting = false;
                start =false;
                csvText = new StringBuffer();


            }
        }
    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int i) {

    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (sensorManager != null) {
            sensorManager.unregisterListener(this);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();
        sensorManager.registerListener(this, Gyroscope_sensor, 50000);
        sensorManager.registerListener(this, ACCELEROMETER_sensor, 50000);
    }

    @Override
    protected void onPause() {
        super.onPause();
        sensorManager.unregisterListener(this);

    }

    @Override
    public void onPointerCaptureChanged(boolean hasCapture) {
        super.onPointerCaptureChanged(hasCapture);
    }
    //save csv file
    private void saveFile()
    {
        String ordnerName = "Sensordaten";
        String date = new SimpleDateFormat("yyyy-MM-dd",
                Locale.getDefault()).format(System.currentTimeMillis());
        String fileName = "[" + date + "]acc&gyr.csv";
        String dateiName = fileName + Build.MODEL + "_" + new Date() + ".csv";

        File path = null;
        File file = null;
        BufferedWriter bwr = null;

        try
        {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                path = new File(getExternalFilesDir(Environment.DIRECTORY_DOCUMENTS), ordnerName);
            }
            file = new File(path, dateiName);
            bwr = new BufferedWriter(new FileWriter(file));
        }
        catch (FileNotFoundException e)
        {
            e.printStackTrace();
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }

        if(!path.mkdirs())
        {
            path.mkdir();
        }

        if (!file.exists())
        {
            try
            {
                file.createNewFile();
            }
            catch (IOException e)
            {
                e.printStackTrace();
            }
        }

        try
        {
            if (bwr != null)
            {
                bwr.append(csvText.toString());
                bwr.flush();
            }
        }
        catch (IOException e)
        {
            e.printStackTrace();
        }
        finally
        {
            if (bwr != null)
            {
                try
                {
                    bwr.close();
                }
                catch (IOException e)
                {
                    e.printStackTrace();
                }
            }
        }

        Toast.makeText(getApplicationContext(), "save csv file " + path.getAbsolutePath(),
                Toast.LENGTH_LONG).show();

    }
}