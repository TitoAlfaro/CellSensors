package mit.edu.obmg.cellsensors;

import ioio.lib.api.DigitalOutput;
import ioio.lib.api.IOIO;
import ioio.lib.api.exception.ConnectionLostException;
import ioio.lib.util.BaseIOIOLooper;
import ioio.lib.util.IOIOLooper;
import ioio.lib.util.android.IOIOService;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
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
	float _sensorValue = 0;

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

	@Override
	protected IOIOLooper createIOIOLooper() {
		return new BaseIOIOLooper() {
			private DigitalOutput led_;
			private Vibration vibThread;

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

			@Override
			protected void setup() throws ConnectionLostException,
					InterruptedException {
				led_ = ioio_.openDigitalOutput(IOIO.LED_PIN);

				vibThread = new Vibration(ioio_);
				vibThread.start();
			}

			@Override
			public void loop() throws ConnectionLostException,
					InterruptedException {
				led_.write(false);
				Thread.sleep(500);
				led_.write(true);
				Thread.sleep((long) _sensorValue);
			}
		};
	}

	@Override
	public void onStart(Intent intent, int startId) {
		super.onStart(intent, startId);
		NotificationManager nm = (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
		if (intent != null && intent.getAction() != null
				&& intent.getAction().equals("stop")) {
			// User clicked the notification. Need to stop the service.
			nm.cancel(0);
			stopSelf();
		} else {
			// Service starting. Create a notification.
			Notification notification = new Notification(
					R.drawable.ic_launcher, "IOIO service running",
					System.currentTimeMillis());
			notification
					.setLatestEventInfo(this, "IOIO Service", "Click to stop",
							PendingIntent.getService(this, 0, new Intent(
									"stop", null, this, this.getClass()), 0));
			notification.flags |= Notification.FLAG_ONGOING_EVENT;
			nm.notify(0, notification);
		}
	}

	class Vibration extends Thread {
		private final String TAG = "TempSensingVibThread";
		private DigitalOutput led;

		private IOIO ioio_;
		private boolean run_ = true;
		int vibPin_;
		int threadNum_;
		float inTemp_;

		public Vibration(IOIO ioio) throws InterruptedException {
			ioio_ = ioio;
		}

		@Override
		public void run() {
			Log.d(TAG, "Thread [" + getName() + "] is running.");

			while (run_) {
				try {
					out = ioio_.openDigitalOutput(vibPin, true);
					while (true) {

						out.write(true);
						sleep((long) _sensorValue);
						out.write(false);
						sleep((long) 100);

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
