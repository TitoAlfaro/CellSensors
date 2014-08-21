package mit.edu.obmg.cellsensors;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.LineGraphView;

import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
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
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView.FindListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;

public class LightLevel extends Fragment implements SensorEventListener {
	final String TAG = "LightLevel";

	// Sensor
	SensorManager mSensorManager;
	Sensor mLight;
	float _sensorValue;

	// UI
	TextView mLightValue;
	Button choiceIOIO, choiceGear;
	boolean choiceIOIOFlag;
	private RadioGroup radioG;
	private RadioButton btnIOIO, btnGear;
	private NumberPicker minValue, maxValue;
	int minPicker = 0;
	int maxPicker = 500;
	
	//Graph
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
	/**** ****/

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.lightlevel_fragment, container,
				false);
		mLightValue = (TextView) view.findViewById(R.id.textLight);
		radioG = (RadioGroup) view.findViewById(R.id.outChoice);
		btnIOIO = (RadioButton) view.findViewById(R.id.btnIOIO);
		btnGear = (RadioButton) view.findViewById(R.id.btnGear);

		String[] sensorNums = new String[maxPicker+1];
		for (int i = 0; i < sensorNums.length; i++) {
			sensorNums[i] = Integer.toString(i);
		}

		minValue = (NumberPicker) view.findViewById(R.id.minValue);
		minValue.setMinValue(minPicker);
		minValue.setMaxValue(maxPicker);
		minValue.setWrapSelectorWheel(false);
		minValue.setDisplayedValues(sensorNums);
		minValue.setValue(minPicker);
		minValue
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);

		maxValue = (NumberPicker) view.findViewById(R.id.maxValue);
		maxValue.setMinValue(minPicker);
		maxValue.setMaxValue(maxPicker);
		maxValue.setWrapSelectorWheel(false);
		maxValue.setDisplayedValues(sensorNums);
		maxValue.setValue(maxPicker);
		maxValue
				.setDescendantFocusability(NumberPicker.FOCUS_BLOCK_DESCENDANTS);
		
		/**** IOIO ****/
		IOIOIntent = new Intent(getActivity(), IOIOConnection.class);
		getActivity().startService(IOIOIntent);

		// Bind to the service
		getActivity().bindService(
				new Intent(getActivity(), IOIOConnection.class), mIOIOConnection,
				Context.BIND_AUTO_CREATE);

		/**** GRAPH VIEW ****/
		exampleSeries = new GraphViewSeries(new GraphViewData[] {
		    new GraphViewData(0, 0)
		});
		
		graphView = new LineGraphView(
		    getActivity() // context
		    , "GraphViewDemo" // heading
		);
		graphView.addSeries(exampleSeries); // data
        graphView.setViewPort(1, 8);
        graphView.setScalable(true);
		 
		LinearLayout layout = (LinearLayout) view.findViewById(R.id.Graph);
		layout.addView(graphView);
		/**** GRAPH VIEW ****/

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		mSensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);
		mLight = mSensorManager.getDefaultSensor(Sensor.TYPE_LIGHT);
		
	}
	
	public void onRadioButtonClicked(View view) {
	    // Is the button now checked?
	    boolean checked = ((RadioButton) view).isChecked();
	    
	    // Check which radio button was clicked
	    switch(view.getId()) {
	        case R.id.btnIOIO:
	            if (checked)
	            	choiceIOIOFlag = true;
	            break;
	        case R.id.btnGear:
	            if (checked)
	            	choiceIOIOFlag = false;
	            break;
	    }
	}

	public void sendData(float v) {
		if (!mBound)
			return;
		
		final float rate = mapValue.map(_sensorValue, minValue.getValue(),
				maxValue.getValue(),
				(float) 2000, (float) 5);

			// Create and send a message to the service, using a supported
			// 'what' value
			Message msg = Message.obtain(null, IOIOConnection.LIGHT_LEVEL,
					(int) rate, 0);
			try {
				mService.send(msg);
			} catch (RemoteException e) {
				e.printStackTrace();
			}

	}
	/****************************
	*    		IOIO			*
	*****************************/

	@Override
	public void onSensorChanged(SensorEvent event) {

		_sensorValue = event.values[0];
		mLightValue.setText("Values: " + _sensorValue);
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

	@Override
	public void onResume() {
		super.onResume();
		mSensorManager.registerListener(this, mLight,
				SensorManager.SENSOR_DELAY_NORMAL);
		getActivity().startService(IOIOIntent);
		
		/*** Graph ****/
		mTimer2 = new Runnable() {
            @Override
            public void run() {
                graph2LastXValue += 1d;
                exampleSeries.appendData(new GraphViewData(graph2LastXValue, _sensorValue), true, 10);
                mHandler.postDelayed(this, 2000);
            }
        };
        mHandler.postDelayed(mTimer2, 1000);
		/*** Graph ****/
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
