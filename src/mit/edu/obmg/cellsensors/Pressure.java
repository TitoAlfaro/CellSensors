package mit.edu.obmg.cellsensors;

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

public class Pressure extends Fragment  implements SensorEventListener{

	SensorManager mSensorManager;
	Sensor mPressure;
	
	TextView mPressureValue;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pressure_fragment, container,
				false);

		mPressureValue = (TextView) view.findViewById(R.id.textPressure);
		
		return view;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		float lux = event.values[0];
		mPressureValue.setText("Values: " + lux);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mPressure, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	public void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	  }

}
