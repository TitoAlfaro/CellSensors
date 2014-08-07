package mit.edu.obmg.cellsensors.consumer.service;

import java.io.IOException;

import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.util.Log;
import android.widget.Toast;

import mit.edu.obmg.cellsensors.R;
import mit.edu.obmg.cellsensors.AccessoryConsumer;
import com.samsung.android.sdk.SsdkUnsupportedException;
import com.samsung.android.sdk.accessory.SA;
import com.samsung.android.sdk.accessory.SAAgent;
import com.samsung.android.sdk.accessory.SAPeerAgent;
import com.samsung.android.sdk.accessory.SASocket;

public class AccessoryConsumerService extends SAAgent {
	public static final String TAG = "AccessoryConsumerService";

	public static final int HELLOACCESSORY_CHANNEL_ID = 104;

	@Override
	protected void onError(String errorMessage, int errorCode) {
		super.onError(errorMessage, errorCode);

		Log.e(TAG, "AccessoryConsumerService.onError() errorCode: " + errorCode);
	}

	private final IBinder mBinder = new LocalBinder();

	Handler mHandler = new Handler();

	public AccessoryConsumerService() {
		super("AccessoryConsumerService", AccessoryConsumerConnection.class);
	}

	public class LocalBinder extends Binder {
		public AccessoryConsumerService getService() {
			return AccessoryConsumerService.this;
		}
	}

	private SASocket mConnectionHandler;

	public class AccessoryConsumerConnection extends SASocket {
		public AccessoryConsumerConnection() {
			super(AccessoryConsumerConnection.class.getName());
		}

		@Override
		public void onError(int channelId, String errorMessage, int errorCode) {
			Log.e(TAG, "SASocket.onError, errorCode:" + errorCode);
		}

		@Override
		public void onReceive(int channelId, byte[] data) {
			Log.d(TAG, "AccessoryConsumerConnection.onReceive()");

			final String strToUpdateUI = new String(data);

			mHandler.post(new Runnable() {
				@Override
				public void run() {
					Log.d(TAG, "HelloAccessoryConsumerConnection.run()");

					AccessoryConsumer.mTextView.setText(strToUpdateUI);
				}
			});
		}

		@Override
		protected void onServiceConnectionLost(int reason) {
			Log.e(TAG, "Service Connection Lost, Reason: " + reason);

			closeConnection();
		}
	}

	public void findPeers() {
		Log.d(TAG, "findPeerAgents()");

		findPeerAgents();
	}

    @Override
    public void onCreate() {
        super.onCreate();
        Log.i(TAG, "onCreate of smart view Provider Service");
        
        SA mAccessory = new SA();
        try {
        	mAccessory.initialize(this);
            boolean isFeatureEnabled = mAccessory.isFeatureEnabled(SA.DEVICE_ACCESSORY);
        } catch (SsdkUnsupportedException e) {
        	// Error Handling
        } catch (Exception e1) {
            Log.e(TAG, "Cannot initialize SAccessory package.");
            e1.printStackTrace();
			/*
			 * Your application can not use Samsung Accessory SDK. You
			 * application should work smoothly without using this SDK, or you
			 * may want to notify user and close your app gracefully (release
			 * resources, stop Service threads, close UI thread, etc.)
			 */
            stopSelf();
        }

     }		
    
    @Override 
    protected void onServiceConnectionRequested(SAPeerAgent peerAgent) { 
        acceptServiceConnectionRequest(peerAgent); 
    }     
	
	@Override
	protected void onFindPeerAgentResponse(SAPeerAgent remoteAgent, int result) {
		Log.e(TAG, "Service onFindPeerAgentResponse: result" + result);

		if (result == PEER_AGENT_FOUND) {
			onPeerFound(remoteAgent);
		}
	}

	@Override
	protected void onServiceConnectionResponse(SASocket thisConnection,
			int connResult) {
		Log.d(TAG, "onServiceConnectionResponse: connResult" + connResult);

		if (connResult == CONNECTION_SUCCESS) {
			Log.d(TAG, "Service connection CONNECTION_SUCCESS");

			this.mConnectionHandler = thisConnection;

			Toast.makeText(getBaseContext(), R.string.ConnectionEstablishedMsg,
					Toast.LENGTH_LONG).show();
		} else if (connResult == CONNECTION_ALREADY_EXIST) {
			Log.d(TAG, "Service connection CONNECTION_ALREADY_EXIST");

			Toast.makeText(getBaseContext(), R.string.ConnectionAlreadyExist,
					Toast.LENGTH_LONG).show();
		} else {
			Log.d(TAG, "Service connection establishment failed,iConnResult="
					+ connResult);

			Toast.makeText(getBaseContext(), R.string.ConnectionFailure,
					Toast.LENGTH_LONG).show();
		}
	}

	@Override
	public IBinder onBind(Intent intent) {
		Log.d(TAG, "onBind()");

		return mBinder;
	}

	public boolean sendHelloAccessory() {
		Log.d(TAG, "sendHelloAccessory()");

		boolean retvalue = false;

		String jsonStringToSend = "Phone says Hello";

		if (mConnectionHandler != null) {
			try {
				mConnectionHandler.send(HELLOACCESSORY_CHANNEL_ID,
						jsonStringToSend.getBytes());

				retvalue = true;
			} catch (IOException e) {
				e.printStackTrace();
			}
		} else {
			Log.d(TAG, "Requested when no connection");
		}

		return retvalue;
	}

	public void onPeerFound(SAPeerAgent remoteAgent) {
		Log.d(TAG, "onPeerFound enter");

		if (remoteAgent != null) {
			Log.d(TAG, "peer  agent is found  and  try to  connect");

			establishConnection(remoteAgent);
		} else {
			Log.d(TAG, "no peers  are  present  tell the UI");

			Toast.makeText(getApplicationContext(), R.string.NoPeersFound,
					Toast.LENGTH_LONG).show();
		}
	}

	public boolean closeConnection() {
		if (mConnectionHandler != null) {
			mConnectionHandler.close();

			mConnectionHandler = null;
		} else {
			Log.d(TAG, "closeConnection: Called when no connection");
		}

		return true;
	}

	public boolean establishConnection(SAPeerAgent peerAgent) {
		if (peerAgent != null) {
			Log.d(TAG, "Making peer connection");

			requestServiceConnection(peerAgent);

			return true;
		}

		return false;
	}
}