package mit.edu.obmg.cellsensors;

import mit.edu.obmg.cellsensors.consumer.service.AccessoryConsumerService;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.ServiceConnection;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

public class AccessoryConsumer extends Fragment {
	public static final String TAG = "HelloAccessoryActivity";

	static final String SAP_ACTION_ATTACHED = "android.accessory.device.action.ATTACHED";

	static final String SAP_ACTION_DETACHED = "android.accessory.device.action.DETACHED";

	private AccessoryConsumerService mConsumerService = null;

	private boolean mIsBound = false;

	//UI
	Button connectButton, disconnectButton, sendButton;
	public static TextView mTextView;

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View view = inflater.inflate(R.layout.accessory_fragment, container,
				false);


		mTextView = (TextView) view.findViewById(R.id.text1);

		connectButton = (Button) view.findViewById(R.id.btnConnect);
		connectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    			startConnection();
            }
        });
		disconnectButton = (Button) view.findViewById(R.id.btnDisconnect);
		disconnectButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    			closeConnection();
            }
        });
		sendButton = (Button) view.findViewById(R.id.btnSend);
		sendButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
    			sendHelloAccessory();
            }
        });

		doBindService();

		IntentFilter filter = new IntentFilter();

		filter.addAction(SAP_ACTION_ATTACHED);

		filter.addAction(SAP_ACTION_DETACHED);

		getActivity().registerReceiver(mBroadcastReceiver, filter);

		return view;
	}

	@Override
	public void onDestroy() {
		Log.d(TAG, "HelloAccessoryActivity.onDestroy()");

		getActivity().unregisterReceiver(mBroadcastReceiver);

		closeConnection();

		doUnbindService();

		super.onDestroy();
	}

	void doBindService() {
		mIsBound = getActivity().bindService(new Intent(getActivity(),
				AccessoryConsumerService.class), mConnection,

		Context.BIND_AUTO_CREATE);
	}

	void doUnbindService() {
		if (mIsBound) {
			getActivity().unbindService(mConnection);

			mIsBound = false;
		}
	}

/*	public void mOnClick(View v) {
		switch (v.getId()) {
		case R.id.button1: {
			startConnection();
			break;
		}
		case R.id.button2: {
			closeConnection();
			break;
		}
		case R.id.button3: {
			sendHelloAccessory();
			break;
		}
		}
	}
*/
	private void startConnection() {
		if (mIsBound == true && mConsumerService != null) {
			mTextView.setText("startConnection");

			mConsumerService.findPeers();
		}
	}

	private void closeConnection() {
		Log.d(TAG, "closeConnection(), mIsBound=" + mIsBound);

		if (mIsBound == true && mConsumerService != null) {
			mTextView.setText("closeConnection");

			mConsumerService.closeConnection();
		}
	}

	private void sendHelloAccessory() {
		if (mIsBound == true && mConsumerService != null) {
			mTextView.setText("sending HelloGear!");

			mConsumerService.sendHelloAccessory();
		}
	}

	BroadcastReceiver mBroadcastReceiver = new BroadcastReceiver() {
		@Override
		public void onReceive(Context context, Intent intent) {
			String action = intent.getAction();

			Log.d(TAG, "BroadcastReceiver.onReceive()");

			if (SAP_ACTION_ATTACHED.equals(action)) {
				Log.d(TAG, "doBindService()");

				doBindService();
			} else if (SAP_ACTION_DETACHED.equals(action)) {
				Log.d(TAG, "doUnbindService()");

				closeConnection();

				doUnbindService();
			}
		}
	};

	private final ServiceConnection mConnection = new ServiceConnection() {
		@Override
		public void onServiceConnected(ComponentName className, IBinder service) {
			Log.d(TAG, "onServiceConnected()");

			mConsumerService = ((AccessoryConsumerService.LocalBinder) service)
					.getService();

			mConsumerService.findPeers();

			mTextView.setText("onServiceConnected");
		}

		@Override
		public void onServiceDisconnected(ComponentName className) {
			Log.d(TAG, "onServiceDisconnected()");

			mConsumerService = null;

			mIsBound = false;

			mTextView.setText("onServiceDisconnected");
		}
	};
}