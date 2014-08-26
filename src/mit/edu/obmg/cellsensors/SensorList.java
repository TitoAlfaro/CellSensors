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
import android.support.v4.app.ListFragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

public class SensorList extends Fragment {
	private final String TAG = "CellSensor_SensorList";

	// Sensor List
	SensorManager sensorManager;
	List<Sensor> sensorList;
	List<String> listSensorType = new ArrayList<String>();

	// UI
	Button tempButton, lightButton, pressButton, magnetButton, accelButton;
	int buttonId;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.sensorlist_fragment, container, false);

		tempButton = (Button) view.findViewById(R.id.btnTemp);
		tempButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	newFragment(0);
            }
        });
		
		pressButton = (Button) view.findViewById(R.id.btnPress);
		pressButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	newFragment(1);
            }
        });
		lightButton = (Button) view.findViewById(R.id.btnLight);
		lightButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	newFragment(2);
            }
        });
		magnetButton = (Button) view.findViewById(R.id.btnMagnet);
		magnetButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	newFragment(3);
            }
        });
		accelButton = (Button) view.findViewById(R.id.btnAccel);
		accelButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
            	newFragment(4);
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
//		case 5:
//			newFragment = new AccessoryConsumer();
//			break;
		}

		FragmentTransaction transaction = fm.beginTransaction();
		transaction.replace(R.id.fragmentPlaceHolder, newFragment);
		//transaction.addToBackStack(null);
		transaction.commit();
	}

}
