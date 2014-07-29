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

public class Temperature extends Fragment  implements SensorEventListener{

	SensorManager mSensorManager;
	Sensor mTemp;
	
	TextView mTempValue;
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		mTemp = mSensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE);
		
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.temperature_fragment, container,
				false);

		mTempValue = (TextView) view.findViewById(R.id.textTemp);
		
		return view;
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		float lux = event.values[0];
		mTempValue.setText("Values: " + lux);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
		
	}
	
	@Override
	public void onResume() {
	    super.onResume();
	    mSensorManager.registerListener(this, mTemp, SensorManager.SENSOR_DELAY_NORMAL);
	  }

	  @Override
	public void onPause() {
	    super.onPause();
	    mSensorManager.unregisterListener(this);
	  }

}
