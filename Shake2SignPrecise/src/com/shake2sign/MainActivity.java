package com.shake2sign;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Calendar;

import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Environment;
import android.os.SystemClock;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.pm.ActivityInfo;
import android.view.Menu;
import android.view.View;
import android.widget.Chronometer;
import android.widget.EditText;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	
    private String toPrint;
    private boolean recording = false;
    private long initTime;
    private File sd;
    private File file;
    private FileWriter fw;
    private BufferedWriter bw;
    private Chronometer chronometer;
    private EditText name;
    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
	}

	public void initListeners(){
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            20000); // 20000 microseconds between each sample. It was SensorManager.SENSOR_DELAY_FASTEST
     
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            20000); // 20000 microseconds between each sample. It was SensorManager.SENSOR_DELAY_FASTEST
    }
	
	
	public void startRecording(View view) {
		if(!recording){
			initListeners();
			name = (EditText) findViewById(R.id.editText1);
			String value = name.getText().toString();
			if(value == null || value.compareTo("") == 0) {
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Name Missing");
				alertDialog.setMessage("Write your name in the appropriate field");
				alertDialog.setCancelable(true);
				alertDialog.show();
			}
			else {
				Calendar calendar = Calendar.getInstance();
				String filename = value + " " + calendar.getTime().toString().replace(":", "_") + ".txt";
				sd = Environment.getExternalStorageDirectory();
			    file = new File(sd, filename);
				try {
					fw = new FileWriter(file, false);
	                bw = new BufferedWriter(fw);
	                chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();
					recording = true;
					initTime = calendar.getTimeInMillis();
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				String deltaTime = String.valueOf((Calendar.getInstance().getTimeInMillis() - this.initTime));
				bw.write(deltaTime);
                bw.close();
                fw.close();
				recording = false;
				chronometer.stop();
				onStop();
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Stop");
				alertDialog.setMessage("Recording has been stopped as you requested");
				alertDialog.setCancelable(true);
				alertDialog.show();
			}
			catch (Exception e) {
		    	 e.printStackTrace();
			}
		}
	}
	
	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		// Inflate the menu; this adds items to the action bar if it is present.
		getMenuInflater().inflate(R.menu.main, menu);
		return true;
	}

	@Override
	public void onAccuracyChanged(Sensor arg0, int arg1) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onSensorChanged(SensorEvent event) {
		if(recording) {
			switch(event.sensor.getType()) {
		    case Sensor.TYPE_LINEAR_ACCELERATION:
		    	toPrint = event.values[0] + " " + event.values[1] + " " + event.values[2] + " ";
		        try {
		        	bw.write(toPrint);
				} catch (IOException e) {
					e.printStackTrace();
				}
		        break;
		 
		    case Sensor.TYPE_GYROSCOPE:
		    	long time = System.nanoTime();
		        toPrint = event.values[0] + " " + event.values[1] + " " + event.values[2] + " " + time + "\n";
		        try {
		        	bw.write(toPrint);
		        } catch (IOException e) {
					e.printStackTrace();
				}
		        break;
			}
		}
	}
	
	public void onStop() {
    	super.onStop();
    	// unregister sensor listeners to prevent the activity from draining the device's battery.
    	sensorManager.unregisterListener(this);
    }
	
    @Override
    protected void onPause() {
        super.onPause();
        // unregister sensor listeners to prevent the activity from draining the device's battery.
        sensorManager.unregisterListener(this);
    }
    
    public void onResume() {
    	super.onResume();
    	// restore the sensor listeners when user resumes the application.
    	initListeners();
    }
}
