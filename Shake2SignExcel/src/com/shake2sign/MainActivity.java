package com.shake2sign;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

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
import android.widget.Button;
import android.widget.Chronometer;
import android.widget.EditText;
import android.widget.TextView;

public class MainActivity extends Activity implements SensorEventListener {

	private SensorManager sensorManager;
	
    private boolean recording = false;
    private long initTime;
    private File sd;
    private File file;
    private Chronometer chronometer;
    private EditText name;
    private TextView recordingText;
    private Button button;
    private String value;
    
    private List<float[]> dataAcc;
    private List<Object[]> dataGir;



    
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_main);
		setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
		
		sensorManager = (SensorManager) getSystemService(SENSOR_SERVICE);
		chronometer = (Chronometer) findViewById(R.id.chronometer);
		name = (EditText) findViewById(R.id.editText1);
		recordingText = (TextView) findViewById(R.id.textView3);
		button = (Button) findViewById(R.id.button1);
		button.setText("Start recording");
		dataAcc = new ArrayList<float[]>();
		dataGir = new ArrayList<Object[]>();
		initListeners();

	}

	public void initListeners(){
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_LINEAR_ACCELERATION),
            SensorManager.SENSOR_DELAY_FASTEST);
     
        sensorManager.registerListener(this,
            sensorManager.getDefaultSensor(Sensor.TYPE_GYROSCOPE),
            SensorManager.SENSOR_DELAY_FASTEST);
    }
	
	
	public void startRecording(View view) {
		
		if(!recording){
			value = name.getText().toString();
			if(value == null || value.compareTo("") == 0) {
				AlertDialog alertDialog = new AlertDialog.Builder(this).create();
				alertDialog.setTitle("Name Missing");
				alertDialog.setMessage("Write your name in the appropriate field");
				alertDialog.setCancelable(true);
				alertDialog.show();
			}
			else {
				button.setText("Stop recording");
				
				dataAcc = new ArrayList<float[]>();
				dataGir = new ArrayList<Object[]>();
				
				try {
	                chronometer.setBase(SystemClock.elapsedRealtime());
					chronometer.start();
					recording = true;
					initTime = Calendar.getInstance().getTimeInMillis();
					recordingText.setText("Recording");
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		}
		else {
			try {
				long deltaTime = (Calendar.getInstance().getTimeInMillis() - this.initTime);
				
				HSSFWorkbook workbook = new HSSFWorkbook();
				HSSFSheet sheet = workbook.createSheet(value);
				
				int rownum = 0;
				for (int i=0; i < dataAcc.size(); i++) {
				    Row row = sheet.createRow(rownum++);
				    float[] objArr = dataAcc.get(i);
				    Object[] objArrGir = dataGir.get(i);
				    int cellnum = 0;
				    for (float obj : objArr) {
				        row.createCell(cellnum++).setCellValue((double)obj);
				    }
				    for (Object objG : objArrGir) {
				        Cell cell = row.createCell(cellnum++);
				        if(objG instanceof Float) {
				            cell.setCellValue((Float)objG);
				        }
				        else
				            cell.setCellValue((Long)objG);
				    }
				}
			    Row row = sheet.createRow(rownum++);
		        row.createCell(0).setCellValue((double)deltaTime);

				String filename = value + " " + Calendar.getInstance().getTime().toString().replace(":", "_") + ".xls";
				sd = Environment.getExternalStorageDirectory();
			    file = new File(sd, filename);
				try {
				    FileOutputStream out = new FileOutputStream(file);
				    workbook.write(out);
				    out.flush();
				    out.close();				     
				} catch (FileNotFoundException e) {
				    e.printStackTrace();
				} catch (IOException e) {
				    e.printStackTrace();
				}
				
				recording = false;
				chronometer.stop();
				
				recordingText.setText("Not recording");
				button.setText("Start recording");
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
		    	dataAcc.add(new float[] {event.values[0], event.values[1], event.values[2]});
		    	break;
		 
		    case Sensor.TYPE_GYROSCOPE:
		    	long time = System.currentTimeMillis();
		    	dataGir.add(new Object[] {event.values[0], event.values[1], event.values[2], time});
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