package com.shake2check;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;

import org.apache.commons.math3.stat.correlation.SpearmansCorrelation;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;

import android.graphics.Color;
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
    private boolean checking = false;
    private long initTime;
    private File sd;
    private File file;
    private Chronometer chronometer;
    private EditText name;
    private TextView recordingText;
    private Button button;
    private Button checkingButton;
    private String value;
    
    private static int BLUE = Color.parseColor("#0099FF");
    private static int GRAY = Color.LTGRAY;
    
    private List<List<Float>> data;
    private List<Long> times;
    private List<List<Double>> checkingdata;
    
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
		button.setBackgroundColor(GRAY);
		button.setText("Start recording");
		checkingButton = (Button) findViewById(R.id.button2);
		checkingButton.setBackgroundColor(GRAY);
		checkingButton.setText("Start checking");
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
	
	public void stopListeners(){
    	sensorManager.unregisterListener(this);
    }
	
	public double correlation(List<List<Float>> newData, List<List<Double>> oldData, 
								double newTime, double oldTime){
		int size = (newData.get(0).size()>oldData.get(0).size()) ? oldData.get(0).size() : newData.get(0).size();
		
		double score = 0;
		int tot = 6;
		double maxNew, maxOld, minNew, minOld;

		SpearmansCorrelation corr = new SpearmansCorrelation();
		double[] toConvNew = new double[size];
		double[] toConvOld = new double[size];
		
		for(int i=0; i<6; i++) {
			maxNew = Double.MIN_VALUE; maxOld = Double.MIN_VALUE; minNew = Double.MAX_VALUE; minOld = Double.MAX_VALUE;
			List<Float> a = newData.get(i);
			List<Double> b = oldData.get(i);
			for(int j=0; j<size; j++) {
				double aValue = a.get(j);
				double bValue = b.get(j);
				toConvNew[j] = aValue;
				toConvOld[j] = bValue;
				if(aValue<minNew)
					minNew = aValue;
				if(aValue>maxNew)
					maxNew = aValue;
				if(bValue<minOld)
					minOld = bValue;
				if(bValue>maxOld)
					maxOld = bValue;
			}
			
			double partial = corr.correlation(toConvNew, toConvOld);
			if(!Double.isNaN(partial)) {
				score += corr.correlation(toConvNew, toConvOld);
				if(Math.abs((maxNew - maxOld)/maxOld)<0.1 && Math.abs((minNew - minOld)/minOld)<0.1)
					score += 0.1;
			}
			else
				tot--;
		}
		double result = score/tot;
		double difference = Math.abs(oldTime - newTime);
		if(difference < 300)
			result += 0.15;
		else if(difference < 600)
			result += 0.075;
		else
			result -= 0.1;
		return (result + 1)/2;	
    }
	
	public void startRecording(View view) {
		if(!checking) {
			if(!recording){
				//initListeners();
				
				value = name.getText().toString();
				if(value == null || value.compareTo("") == 0) {
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setTitle("Name Missing");
					alertDialog.setMessage("Write your name in the appropriate field");
					alertDialog.setCancelable(true);
					alertDialog.show();
				}
				else {
					String filename = value + ".xls";
					sd = Environment.getExternalStorageDirectory();
				    file = new File(sd, filename);
					if(file.exists()) { 
				    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle("Already registered");
						alertDialog.setMessage("You've already registered before. \n"
								+ "Please, check your identity.");
						alertDialog.setCancelable(true);
						alertDialog.show();
					}
					else {
						button.setBackgroundColor(BLUE);
						button.setText("Stop recording");
	
						data = new ArrayList<List<Float>>(6);
					    for(int i=0; i<6; i++){
					    	data.add(new ArrayList<Float>());
					    }
					    times = new ArrayList<Long>();
					    
		                chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.start();
						recording = true;
						initTime = Calendar.getInstance().getTimeInMillis();
						recordingText.setTextColor(BLUE);
						recordingText.setText("Recording");
					}
				}
			}
			else {
				long deltaTime = Calendar.getInstance().getTimeInMillis() - this.initTime;
				chronometer.stop();
	
				HSSFWorkbook workbook = new HSSFWorkbook();
				HSSFSheet sheet = workbook.createSheet(value);
				
				Row row = null;
				int rownum = 0;
		    	int cellnum = 0;
		    	for(int i=0; i<data.get(0).size(); i++) {
					row = sheet.createRow(rownum++);
			    	cellnum = 0;
			    	for(int j=0; j<6; j++) {
			    		List<Float> f = data.get(j);
			    		row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC).setCellValue(f.get(i));
			    	}
			    	row.createCell(cellnum++, Cell.CELL_TYPE_NUMERIC).setCellValue(times.get(i));
		    	}
		    	
				row = sheet.createRow(rownum++);
		    	row.createCell(0, Cell.CELL_TYPE_NUMERIC).setCellValue((double)deltaTime);
	
				String filename = value + ".xls";
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
				recordingText.setText("");
				button.setBackgroundColor(GRAY);
				button.setText("Start recording");
			}
		}
		else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Mind you!");
			alertDialog.setMessage("You're currently checking. Press STOP CHECKING before recording again.");
			alertDialog.setCancelable(true);
			alertDialog.show();
		}
	}
	
	public void startChecking(View view) {
		
		if(!recording) {
			if(!checking){
				//initListeners();
				value = name.getText().toString();
				if(value == null || value.compareTo("") == 0) {
					AlertDialog alertDialog = new AlertDialog.Builder(this).create();
					alertDialog.setTitle("Name Missing");
					alertDialog.setMessage("Write your name in the appropriate field");
					alertDialog.setCancelable(true);
					alertDialog.show();
				}
				else {
					
					reInitializeSensorData();
					
					String filename = value + ".xls";
					sd = Environment.getExternalStorageDirectory();
				    file = new File(sd, filename);
				    
				    if(!file.exists()) { 
				    	AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle("File Missing");
						alertDialog.setMessage("You've never registered before. \n"
								+ "Please, register before verifying your identity.");
						alertDialog.setCancelable(true);
						alertDialog.show();
					}
					else {
						checkingButton.setBackgroundColor(BLUE);
						checkingButton.setText("Stop checking");
		                chronometer.setBase(SystemClock.elapsedRealtime());
						chronometer.start();
						checking = true;
						initTime = Calendar.getInstance().getTimeInMillis();
						recordingText.setTextColor(BLUE);
						recordingText.setText("Checking");
					}
				}
			}
			else {
				try {
					long lasting = Calendar.getInstance().getTimeInMillis() - this.initTime;
					chronometer.stop();
					FileInputStream fInput = new FileInputStream(file);
					HSSFWorkbook workbook = new HSSFWorkbook(fInput);
					HSSFSheet sheet = workbook.getSheetAt(0);
					
					
					for (int j=0; j<sheet.getLastRowNum(); j++) {
				        Row row = sheet.getRow(j);
				        for(int i=0; i<6; i++) {
				        	checkingdata.get(i).add(row.getCell(i).getNumericCellValue());
				        }
				        
				    }
					
			        double recLast = sheet.getRow(sheet.getLastRowNum()).getCell(0).getNumericCellValue();
				    fInput.close();
					
				    
					double corr = correlation(data, checkingdata, lasting, recLast);
					
					
					reInitializeSensorData();
					checking = false;
					recordingText.setText("");
					checkingButton.setBackgroundColor(GRAY);
					checkingButton.setText("Start checking");
					NumberFormat formatter = new DecimalFormat("#0.00");
					if(corr>0.7) {
						AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle("PASS");
						alertDialog.setMessage("You've successfully authenticated."
								+ "\n Score: " + formatter.format(corr*100)  + "%.");
						alertDialog.setCancelable(true);
						alertDialog.show();
					}
					else {
						AlertDialog alertDialog = new AlertDialog.Builder(this).create();
						alertDialog.setTitle("FAIL");
						alertDialog.setMessage("Sorry. Try again."
								+ "\n Score: " + formatter.format(corr*100) + "%.");
						alertDialog.setCancelable(true);
						alertDialog.show();
					}
				}
				catch (Exception e) {
			    	 e.printStackTrace();
				}
			}
		}
		else {
			AlertDialog alertDialog = new AlertDialog.Builder(this).create();
			alertDialog.setTitle("Mind you!");
			alertDialog.setMessage("You're currently recording. Press STOP RECORDING before checking.");
			alertDialog.setCancelable(true);
			alertDialog.show();
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
		if(recording || checking) {
			switch(event.sensor.getType()) {
		    case Sensor.TYPE_LINEAR_ACCELERATION:
		    	data.get(0).add(event.values[0]);
		    	data.get(1).add(event.values[1]);
		    	data.get(2).add(event.values[2]);
		    	break;
		 
		    case Sensor.TYPE_GYROSCOPE:
		    	data.get(3).add(event.values[0]);
		    	data.get(4).add(event.values[1]);
		    	data.get(5).add(event.values[2]);
		    	times.add(System.currentTimeMillis());
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
        stopListeners();
    }
    
    public void onResume() {
    	super.onResume();
    	// restore the sensor listeners when user resumes the application.
    	initListeners();
    }
    
    private void reInitializeSensorData() {
    	data = new ArrayList<List<Float>>(6);
	    for(int i=0; i<6; i++){
	    	data.add(new ArrayList<Float>());
	    }
	    times = new ArrayList<Long>();
	    checkingdata = new ArrayList<List<Double>>(6);
	    for(int i=0; i<6; i++){
	    	checkingdata.add(new ArrayList<Double>());
	    }
    }
}