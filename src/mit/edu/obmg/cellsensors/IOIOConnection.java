package mit.edu.obmg.cellsensors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.content.Intent;
import android.os.Handler;
import android.os.IBinder;
import android.os.Message;
import android.os.Messenger;
import android.util.Log;
import android.widget.Toast;

public class IOIOConnection extends IOIOService {
	final String TAG = "IOIOConnection";

	// MultiThreading
	private Thread Vibration1;
	Thread thread1 = new Thread(Vibration1);

	// Vibration
	float rate1 = 1000;
	int vibPin = 40;
	DigitalOutput out;

	/** Command to the service to display a message */
	static final int LIGHT_LEVEL = 1;
	static final int PRESSURE_LEVEL = 2;
	static final int TEMPERATURE_LEVEL = 3;
	static final int MAGNETOMETER_LEVEL = 4;
	static final int ACCELEROMETER_LEVEL = 5;
	long _sensorValue = 0;

	/**
	 * Handler of incoming messages from clients.
	 */
	class IncomingHandler extends Handler {
		@Override
		public void handleMessage(Message msg) {
			switch (msg.what) {
			case LIGHT_LEVEL:
				// Toast.makeText(getApplicationContext(), "Value: "+msg.arg1,
				// Toast.LENGTH_SHORT).show();
				_sensorValue = msg.arg1;
				break;
			case PRESSURE_LEVEL:
				// Toast.makeText(getApplicationContext(), "Value: "+msg.arg1,
				// Toast.LENGTH_SHORT).show();
				_sensorValue = msg.arg1;
				break;
			case TEMPERATURE_LEVEL:
				// Toast.makeText(getApplicationContext(), "Value: "+msg.arg1,
				// Toast.LENGTH_SHORT).show();
				_sensorValue = msg.arg1;
				break;
			case MAGNETOMETER_LEVEL:
				// Toast.makeText(getApplicationContext(), "Value: "+msg.arg1,
				// Toast.LENGTH_SHORT).show();
				_sensorValue = msg.arg1;
				break;
			case ACCELEROMETER_LEVEL:
				// Toast.makeText(getApplicationContext(), "Value: "+msg.arg1,
				// Toast.LENGTH_SHORT).show();
				_sensorValue = msg.arg1;
				break;
			default:
				super.handleMessage(msg);
			}
		}
	}

	/**
	 * Target we publish for clients to send messages to IncomingHandler.
	 */
	final Messenger mMessenger = new Messenger(new IncomingHandler());

	/**
	 * When binding to the service, we return an interface to our messenger for
	 * sending messages to the service.
	 */
	@Override
	public IBinder onBind(Intent intent) {
		Toast.makeText(getApplicationContext(), "binding", Toast.LENGTH_SHORT)
				.show();
		return mMessenger.getBinder();
	}

	class Looper extends BaseIOIOLooper {
		private Vibration vibThread;

		@Override
		protected void setup() throws ConnectionLostException,
				InterruptedException {

			vibThread = new Vibration(ioio_);
			vibThread.start();
		}

		@Override
		public void loop() throws ConnectionLostException, InterruptedException {
			Thread.sleep(100);
		}

		@Override
		public void disconnected() {
			super.disconnected();
			Log.i(TAG, "IOIO disconnected, killing workers");
			if (vibThread != null) {
				vibThread.abort();
			}
			try {
				if (vibThread != null) {
					vibThread.join();
				}

				Log.i(TAG, "All workers dead");
			} catch (InterruptedException e) {
				Log.w(TAG, "Interrupted. Some workers may linger.");
			}
		}
	}

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new Looper();
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
	}

	class Vibration extends Thread {
		private final String TAG = "IOIOConnectionVibThread";

		private IOIO ioio_;
		private boolean run_ = true;
		int vibPin_;
		int threadNum_;
		float inTemp_;
		private DigitalOutput led_;

		public Vibration(IOIO ioio) throws InterruptedException {
			ioio_ = ioio;
		}

		@Override
		public void run() {
			Log.d(TAG, "Thread [" + getName() + "] is running.");

			while (run_) {
				try {
					led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);
					out = ioio_.openDigitalOutput(vibPin, true);
					while (true) {

						out.write(true);
						led_.write(false);
						sleep((long) 100);
						out.write(false);
						led_.write(true);
						sleep((long) _sensorValue);
						Log.d(TAG, "Sensor Value " + _sensorValue);

					}
				} catch (ConnectionLostException e) {
				} catch (Exception e) {
					Log.e(TAG, "Unexpected exception caught in VibThread", e);
					ioio_.disconnect();
					break;
				} finally {
					try {
						ioio_.waitForDisconnect();
					} catch (InterruptedException e) {
					}
				}
			}
		}

		public void abort() {
			run_ = false;
			interrupt();
		}
	}

	@Override
	public boolean onUnbind(Intent intent) {
		Log.i(TAG, "onUnbind");
		stopSelf();
		return super.onUnbind(intent);
	}

	@Override
	public void onDestroy() {
		Log.i(TAG, "onDestroy");
		stopSelf();
		super.onDestroy();
	}
}
