package mit.edu.obmg.cellsensors;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import mit.edu.obmg.cellsensors.RangeSeekBar.OnRangeSeekBarChangeListener;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.ServiceConnection;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorManager;
import android.media.MediaRecorder;
import android.media.Ringtone;
import android.media.RingtoneManager;
import android.net.Uri;
import android.os.Bundle;
import android.os.CountDownTimer;
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
import android.widget.CompoundButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.TextView;
import android.widget.ToggleButton;

import com.jjoe64.graphview.GraphView;
import com.jjoe64.graphview.GraphView.GraphViewData;
import com.jjoe64.graphview.GraphViewSeries;
import com.jjoe64.graphview.GraphViewStyle.GridStyle;
import com.jjoe64.graphview.LineGraphView;

public class SoundLevel extends Fragment {
	final String TAG = "Sound";

	// Fragment
	Bundle fragmentFlag;
	Boolean testFlag = false;

	// Sound
	private MediaRecorder mRecorder = null;
	float _sensorValue;

	// UI
	TextView fragmentTitle, timer;
	TextView mSoundValue;
	ToggleButton timerStartStop;
	private NumberPicker minValue, maxValue;
	int minPicker = 0;
	int maxPicker = 40000;
	int currentMinPicker = minPicker;
	int currentMaxPicker = maxPicker;

	CountDownTimer clockTimer = new CountDownTimer(900000, 1000) {

		public void onTick(long millisUntilFinished) {
			timer.setText(""+ String.format("%d:%d",
					TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished),
					TimeUnit.MILLISECONDS.toSeconds(millisUntilFinished) - 
					TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(millisUntilFinished))));
		}

		public void onFinish() {
			timer.setText("Please Return to E15-445");
			try {
			    Uri notification = RingtoneManager.getDefaultUri(RingtoneManager.TYPE_NOTIFICATION);
			    Ringtone r = RingtoneManager.getRingtone(getActivity(), notification);
			    r.play();
			} catch (Exception e) {
			    e.printStackTrace();
			}
		}
	};

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
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sound_fragment, container, false);

		if (savedInstanceState != null) {
			currentMinPicker = savedInstanceState.getInt("currentMinPicker");
			currentMaxPicker = savedInstanceState.getInt("currentMaxPicker");
		}

		testFlag = getTestFlag();
		
		fragmentTitle = (TextView) view.findViewById(R.id.textView1);
		fragmentTitle.setText("User Study");
		mSoundValue = (TextView) view.findViewById(R.id.textSound);

		LinearLayout layoutTimer = (LinearLayout) view.findViewById(R.id.layoutTimer);
		timer = (TextView) view.findViewById(R.id.textTimer);
		timerStartStop = (ToggleButton) view.findViewById(R.id.btnTimer);
		timerStartStop.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
		    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
		        if (isChecked) {
		        	clockTimer.start();
		        } else {
		        	clockTimer.cancel();
		        }
		    }
		});

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

		maxValue = (NumberPicker) view.findViewById(R.id.maxValue);
		maxValue.setMinValue(minPicker);
		maxValue.setMaxValue(maxPicker);
		maxValue.setWrapSelectorWheel(false);
		maxValue.setDisplayedValues(sensorNums);
		maxValue.setValue(currentMaxPicker);

		/**** IOIO ****/
		IOIOIntent = new Intent(getActivity(), IOIOConnection.class);
		getActivity().startService(IOIOIntent);

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
		graphView
				.setManualYAxisBounds(maxValue.getValue(), minValue.getValue());

		LinearLayout graphLayout = (LinearLayout) view.findViewById(R.id.Graph);
		
		if (testFlag == false) {
			graphLayout.setVisibility(View.VISIBLE);
			timer.setVisibility(View.GONE);
		}
		
		graphLayout.addView(graphView);
		/**** GRAPH VIEW ****/

		if (testFlag == true) {
			graphLayout.setVisibility(View.GONE);
			layoutTimer.setVisibility(View.VISIBLE);
			timer.setVisibility(View.VISIBLE);
		}			

		try {
			if (mRecorder == null) {
				mRecorder = new MediaRecorder();
				mRecorder.setAudioSource(MediaRecorder.AudioSource.MIC);
				mRecorder.setOutputFormat(MediaRecorder.OutputFormat.THREE_GPP);
				mRecorder.setAudioEncoder(MediaRecorder.AudioEncoder.AMR_NB);
				mRecorder.setOutputFile("/dev/null");
				mRecorder.prepare();
				mRecorder.start();
			}
		} catch (IllegalStateException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

		/****  RANGE BAR  ****/
		// create RangeSeekBar as Integer range between 20 and 75
		RangeSeekBar<Integer> seekBar = new RangeSeekBar<Integer>(minValue.getValue(), maxValue.getValue(),
				getActivity());
		seekBar.setOnRangeSeekBarChangeListener(new OnRangeSeekBarChangeListener<Integer>() {
			@Override
			public void onRangeSeekBarValuesChanged(RangeSeekBar<?> bar,
					Integer minRangeValue, Integer maxRangeValue) {
				// handle changed range values

				minValue.setValue(minRangeValue);
				maxValue.setValue(maxRangeValue);
				
				Log.i(TAG, "User selected new range values: MIN=" + minValue
						+ ", MAX=" + maxValue);
			}
		});

		// add RangeSeekBar to pre-defined layout
		ViewGroup layout = (ViewGroup) view.findViewById(R.id.rangeBar);
		layout.addView(seekBar);
		/****  RANGE BAR  ****/

		return view;
	}

	public boolean getTestFlag() {
		return getArguments().getBoolean("testFlag");
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}

	public double getAmplitude() {
		if (mRecorder != null){
			return mRecorder.getMaxAmplitude();
		}else
			return 0;

	}

	public void sendData(float v) {
		if (!mBound)
			return;

		final float rate = mapValue.map(_sensorValue, minValue.getValue(),
				maxValue.getValue(), (float) 1000, (float) 5);

		// Create and send a message to the service, using a supported
		// 'what' value
		Message msg = Message.obtain(null, IOIOConnection.SOUND_LEVEL,
				(int) rate, 0);
		try {
			mService.send(msg);
		} catch (RemoteException e) {
			e.printStackTrace();
		}

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

		getActivity().startService(IOIOIntent);

		/*** Graph ****/
		mTimer2 = new Runnable() {
			@Override
			public void run() {
				graph2LastXValue += 1d;

				_sensorValue = (float) getAmplitude();
				sendData(_sensorValue);
				if (testFlag == false) {
					fragmentTitle.setText("Sound Level");
					mSoundValue.setText("Values: " + _sensorValue);
				}
				
				if (testFlag == false) {
					exampleSeries.appendData(new GraphViewData(
							graph2LastXValue, _sensorValue), true, 10);
				} else {
					exampleSeries.appendData(new GraphViewData(
							graph2LastXValue, 0), true, 10);

				}
				mHandler.postDelayed(this, 500);
				graphView.setManualYAxisBounds(maxValue.getValue(),
						minValue.getValue());
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
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}

		getActivity().stopService(IOIOIntent);
		mHandler.removeCallbacks(mTimer2);
	}

	@Override
	public void onStop() {
		super.onStop();
		if (mRecorder != null) {
			mRecorder.stop();
			mRecorder.release();
			mRecorder = null;
		}

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
