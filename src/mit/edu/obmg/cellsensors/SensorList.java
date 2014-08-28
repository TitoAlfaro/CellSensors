package mit.edu.obmg.cellsensors;

import java.util.ArrayList;
import java.util.List;

import android.content.Context;
import android.hardware.Sensor;
import android.hardware.SensorManager;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentTransaction;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CompoundButton;
import android.widget.ToggleButton;

public class SensorList extends Fragment {
	private final String TAG = "CellSensor_SensorList";

	// Sensor List
	SensorManager sensorManager;
	List<Sensor> sensorList;
	List<String> listSensorType = new ArrayList<String>();

	// UI
	Button tempButton, lightButton, pressButton, magnetButton, accelButton,
			proximityButton, humidityButton, soundButton;
	ToggleButton testButton;
	int buttonId;
	// boolean testFlag = false;

	// Fragment
	Bundle fragmentFlag = new Bundle();
	Boolean testFlag;
	int fragNum;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sensorlist_fragment, container,
				false);

		testButton = (ToggleButton) view.findViewById(R.id.btnTest);
		testButton
				.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {

					@Override
					public void onCheckedChanged(CompoundButton buttonView,
							boolean isChecked) {
						if (isChecked) {
							testFlag = true;
							fragmentFlag.putBoolean("testFlag", true);
							newFragment(fragNum);
						} else {
							testFlag = false;
							fragmentFlag.putBoolean("testFlag", false);
							newFragment(fragNum);
						}
					}
				});

		tempButton = (Button) view.findViewById(R.id.btnTemp);
		tempButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 0;
				newFragment(fragNum);
			}
		});

		pressButton = (Button) view.findViewById(R.id.btnPress);
		pressButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 1;
				newFragment(fragNum);
			}
		});
		lightButton = (Button) view.findViewById(R.id.btnLight);
		lightButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 2;
				newFragment(fragNum);
			}
		});
		magnetButton = (Button) view.findViewById(R.id.btnMagnet);
		magnetButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 3;
				newFragment(fragNum);
			}
		});
		accelButton = (Button) view.findViewById(R.id.btnAccel);
		accelButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 4;
				newFragment(fragNum);
			}
		});
		proximityButton = (Button) view.findViewById(R.id.btnProx);
		proximityButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 5;
				newFragment(fragNum);
			}
		});
		humidityButton = (Button) view.findViewById(R.id.btnHumid);
		humidityButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 6;
				newFragment(fragNum);
			}
		});
		soundButton = (Button) view.findViewById(R.id.btnSound);
		soundButton.setOnClickListener(new View.OnClickListener() {
			public void onClick(View v) {
				fragNum = 7;
				newFragment(fragNum);
			}
		});

		return view;
	}

	@Override
	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

		sensorManager = (SensorManager) getActivity().getSystemService(
				Context.SENSOR_SERVICE);

		if (sensorManager.getDefaultSensor(Sensor.TYPE_AMBIENT_TEMPERATURE) != null) {
			tempButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_PRESSURE) != null) {
			pressButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_LIGHT) != null) {
			lightButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_MAGNETIC_FIELD) != null) {
			magnetButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_ACCELEROMETER) != null) {
			accelButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_PROXIMITY) != null) {
			proximityButton.setVisibility(View.VISIBLE);
		}
		if (sensorManager.getDefaultSensor(Sensor.TYPE_RELATIVE_HUMIDITY) != null) {
			humidityButton.setVisibility(View.VISIBLE);
		}

		sensorList = sensorManager.getSensorList(Sensor.TYPE_ALL);

		List<String> listSensorType = new ArrayList<String>();

		for (int i = 0; i < sensorList.size(); i++) {
			listSensorType.add(sensorList.get(i).getName());
			Log.i(TAG, " Sensor: " + sensorList.get(i).getName());
		}

	}

	public void newFragment(int buttonId) {
		Fragment newFragment = null;
		FragmentManager fm = getFragmentManager();
		switch (buttonId) {
		case 0:
			newFragment = new Temperature();
			break;
		case 1:
			newFragment = new Pressure();
			break;
		case 2:
			newFragment = new LightLevel();
			break;
		case 3:
			newFragment = new Magnetometer();
			break;
		case 4:
			newFragment = new Accelerometer();
			break;
		case 5:
			newFragment = new Proximity();
			break;
		case 6:
			newFragment = new Humidity();
			break;
		case 7:
			newFragment = new SoundLevel();
			break;
		}

		newFragment.setArguments(fragmentFlag);

		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.fragmentPlaceHolder, newFragment);
		// transaction.addToBackStack(null);
		transaction.commit();
	}

}
