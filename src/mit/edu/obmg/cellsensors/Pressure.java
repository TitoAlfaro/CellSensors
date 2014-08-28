package mit.edu.obmg.cellsensors;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;

import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.os.RemoteException;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;

public class Pressure extends Fragment implements SensorEventListener {
	final String TAG = "Pressure Fragment";

	// Fragment
	Bundle fragmentFlag;
	Boolean testFlag = false;

	SensorManager mSensorManager;
	Sensor mPressure;
	float _sensorValue;

	TextView mPressureValue;
	private NumberPicker minValue, maxValue;
	int minPicker = 500;
	int maxPicker = 2000;
	int currentMinPicker = minPicker + 1000;
	int currentMaxPicker = maxPicker - 480;

	// Graph
	private final Handler mHandler = new Handler();
	private Runnable mTimer2;
	private GraphView graphView;
	private double graph2LastXValue = 5d;
	GraphViewSeries exampleSeries;

	/**** IOIOConnection ****/
	Intent IOIOIntent;
	// Messenger for communicating with the service. */
	Messenger mService = null;
	// Flag indicating whether we have called bind on the service. */
	boolean mBound;

	MapValues mapValue = new MapValues();

	/**** IOIOConnection ****/

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mPressure = mSensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE);

	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.pressure_fragment, container,
				false);

		if (savedInstanceState != null) {
			currentMinPicker = savedInstanceState.getInt("currentMinPicker");
			currentMaxPicker = savedInstanceState.getInt("currentMaxPicker");
		}

		testFlag = getTestFlag();

		mPressureValue = (TextView) view.findViewById(R.id.textPressure);

		String[] sensorNums = new String[maxPicker + 1];
		for (int i = 0; i < sensorNums.length; i++) {
			sensorNums[i] = Integer.toString(i);
		}

		minValue = (NumberPicker) view.findViewById(R.id.minValue);
		minValue.setMinValue(minPicker);
		minValue.setMaxValue(maxPicker);
		minValue.setWrapSelectorWheel(false);
		minValue.setDisplayedValues(sensorNums);
		minValue.setValue(currentMinPicker);
		minValue.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		maxValue = (NumberPicker) view.findViewById(R.id.maxValue);
		maxValue.setMinValue(minPicker);
		maxValue.setMaxValue(maxPicker);
		maxValue.setWrapSelectorWheel(false);
		maxValue.setDisplayedValues(sensorNums);
		maxValue.setValue(currentMaxPicker);
		maxValue.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		/**** IOIO ****/
		IOIOIntent = new Intent(getActivity(), IOIOConnection.class);
		// getActivity().startService(IOIOIntent);

		// Bind to the service
		getActivity().bindService(
				new Intent(getActivity(), IOIOConnection.class),
				mIOIOConnection, Context.BIND_AUTO_CREATE);
		/**** IOIO ****/

		/**** GRAPH VIEW ****/
		exampleSeries = new GraphViewSeries(
				new GraphViewData[] { new GraphViewData(0, 0) });

		graphView = new LineGraphView(getActivity() // context
				, "GraphViewDemo" // heading
		);
		graphView.addSeries(exampleSeries); // data
		graphView.setViewPort(1, 8);
		graphView.setScalable(true);
		graphView.setScrollable(true);
		graphView.getGraphViewStyle().setGridStyle(GridStyle.VERTICAL);
		graphView.setShowHorizontalLabels(false);
		graphView.setManualYAxisBounds(maxValue.getValue() - 500,
				minValue.getValue() - 500);

		LinearLayout layout = (LinearLayout) view.findViewById(R.id.Graph);
		layout.addView(graphView);
		/**** GRAPH VIEW ****/

		return view;
	}
	
	public boolean getTestFlag(){
		return getArguments().getBoolean("testFlag");
	}

	@Override
	public void onSensorChanged(SensorEvent event) {

		_sensorValue = event.values[0];
		if (testFlag == false) {
			mPressureValue.setText("Values: " + _sensorValue);
		}
		sendData(_sensorValue);
	}

	@Override
	public void onAccuracyChanged(Sensor sensor, int accuracy) {
		// TODO Auto-generated method stub

	}

	private ServiceConnection mIOIOConnection = new ServiceConnection() {

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

		final float rate = mapValue.map(_sensorValue,
				minValue.getValue() - 500, maxValue.getValue() - 500,
				(float) 2000, (float) 5);

		// Create and send a message to the service, using a supported 'what'
		// value
		Message msg = Message.obtain(null, IOIOConnection.PRESSURE_LEVEL,
				(int) rate, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}
	}

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mPressure,
				SensorManager.SENSOR_DELAY_NORMAL);

		getActivity().startService(IOIOIntent);

		/*** Graph ****/
		mTimer2 = new Runnable() {
			@Override
			public void run() {
				graph2LastXValue += 1d;
				if (testFlag == false) {
					exampleSeries.appendData(new GraphViewData(
							graph2LastXValue, _sensorValue), true, 10);
				}else{
					exampleSeries.appendData(new GraphViewData(
							graph2LastXValue, 0), true, 10);
					
				}
				mHandler.postDelayed(this, 500);
				graphView.setManualYAxisBounds(maxValue.getValue() - 500,
						minValue.getValue() - 500);
			}
		};
		mHandler.postDelayed(mTimer2, 1000);
		/*** Graph ****/

	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);

		outState.putInt("currentMaxPicker", maxValue.getValue());
		outState.putInt("currentMinPicker", minValue.getValue());
	}

	@Override
	public void onPause() {
		super.onPause();
		mSensorManager.unregisterListener(this);
		getActivity().stopService(IOIOIntent);
		mHandler.removeCallbacks(mTimer2);
	}

	@Override
	public void onStop() {
		super.onStop();
		// Unbind from the service
		if (mBound) {
			getActivity().unbindService(mIOIOConnection);
			mBound = false;
		}
		getActivity().stopService(IOIOIntent);

	}

	@Override
	public void onDestroyView() {
		super.onDestroyView();
		// Unbind from the service
		if (mBound) {
			getActivity().unbindService(mIOIOConnection);
			mBound = false;
		}
		getActivity().stopService(IOIOIntent);
	}
}
