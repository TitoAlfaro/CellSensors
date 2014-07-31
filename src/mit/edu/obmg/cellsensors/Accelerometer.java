package mit.edu.obmg.cellsensors;

import java.util.zip.Inflater;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Accelerometer extends Fragment implements SensorEventListener {

	SensorManager mSensorManager;
	Sensor mAccel;
	
	TextView mAccelValueX, mAccelValueY, mAccelValueZ;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accelerometer_fragment, container,
				false);

		mAccelValueX = (TextView) view.findViewById(R.id.textAccelX);
		mAccelValueY = (TextView) view.findViewById(R.id.textAccelY);
		mAccelValueZ = (TextView) view.findViewById(R.id.textAccelZ);
		
		return view;
	}


	@Override
	public void onSensorChanged(SensorEvent event) {

		float _rawValueX = event.values[0];
		float _rawValueY = event.values[1];
		float _rawValueZ = event.values[2];
		mAccelValueX.setText("Values X: " + _rawValueX);
		mAccelValueY.setText("Values Y: " + _rawValueY);
		mAccelValueZ.setText("Values Z: " + _rawValueZ);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mAccel, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	public void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	  }

}
