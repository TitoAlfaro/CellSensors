package mit.edu.obmg.cellsensors;

import java.util.zip.Inflater;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class Accelerometer extends Fragment implements SensorEventListener {
	final String TAG = "Accelerometer";
	
	//Sensor
	SensorManager mSensorManager;
	Sensor mAccel;
	float _sensorValue;
	
	//UI
	TextView mAccelValueX, mAccelValueY, mAccelValueZ;

	//UI
	TextView mLightValue;
	
	//IOIOConnection
	Intent IOIOIntent;
	
	/** Messenger for communicating with the service. */
	Messenger mService = null;

	/** Flag indicating whether we have called bind on the service. */
	boolean mBound;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accelerometer_fragment, container,
				false);

		mAccelValueX = (TextView) view.findViewById(R.id.textAccelX);
		mAccelValueY = (TextView) view.findViewById(R.id.textAccelY);
		mAccelValueZ = (TextView) view.findViewById(R.id.textAccelZ);
		
		IOIOIntent = new Intent(getActivity(), IOIOConnection.class);
		getActivity().startService(IOIOIntent);

		// Bind to the service
		getActivity().bindService(
				new Intent(getActivity(), IOIOConnection.class), mConnection,
				Context.BIND_AUTO_CREATE);
		
		return view;
	}
	
	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);
		
		mSensorManager = (SensorManager) getActivity().getSystemService(Context.SENSOR_SERVICE);
		mAccel = mSensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER);
		
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		_sensorValue = event.values[0];
		float _rawValueY = event.values[1];
		float _rawValueZ = event.values[2];
		mAccelValueX.setText("Values X: " + _sensorValue);
		mAccelValueY.setText("Values Y: " + _rawValueY);
		mAccelValueZ.setText("Values Z: " + _rawValueZ);
		sendData(_sensorValue);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub
	}

	private ServiceConnection mConnection = new ServiceConnection() {

		@Override
		public void onServiceConnected(ComponentName name, IBinder service) {
			mService = new Messenger(service);
			mBound = true;
		}

		@Override
		public void onServiceDisconnected(ComponentName arg0) {
			mService = null;
			mBound = false;

		}
	};

	public void sendData(float v) {
		if (!mBound)
			return;
		// Create and send a message to the service, using a supported 'what'
		// value
		Message msg = Message.obtain(null, IOIOConnection.ACCELEROMETER_LEVEL, (int) _sensorValue, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mAccel,
				SensorManager.SENSOR_DELAY_NORMAL);
		getActivity().startService(IOIOIntent);
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		getActivity().stopService(IOIOIntent);
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			getActivity().unbindService(mConnection);
			mBound = false;
		}
		getActivity().stopService(IOIOIntent);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Unbind from the service
		if (mBound) {
			getActivity().unbindService(mConnection);
			mBound = false;
		}
		getActivity().stopService(IOIOIntent);
	}
}