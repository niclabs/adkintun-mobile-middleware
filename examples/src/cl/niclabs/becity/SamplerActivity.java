package cl.niclabs.becity;

import android.annotation.SuppressLint;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import cl.niclabs.adkmobile.monitor.data.Journey;
import cl.niclabs.adkmobile.monitor.data.Journey.Transport;
import cl.niclabs.adkmobile.utils.Time;
import cl.niclabs.becity.StopTransportDialogFragment.StopTransportDialogListener;
import cl.niclabs.becity.TransportChoiceDialogFragment.TransportChoiceDialogListener;
import cl.niclabs.becity.sampler.R;

public class SamplerActivity extends android.support.v4.app.FragmentActivity implements OnClickListener, TransportChoiceDialogListener, StopTransportDialogListener {

	protected String TAG = "BeCity::SamplerActivity";

	public static final int VERSION = 2;
	
	private Button toggleServicesButton;

	protected void dismissNotification(int mId) {
		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		mNotificationManager.cancel(mId);
	}
	
	/**
	 * Create a notification
	 * 
	 * @param mId
	 * @param title
	 * @param message
	 */
	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	protected void notifyUser(int mId, String title, String message,
			String longMessage) {
		// Creates an explicit intent for an Activity in your app
		Intent resultIntent = new Intent(this, SamplerActivity.class);

		Notification notification;
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.HONEYCOMB) {
			notification = new Notification(R.drawable.ic_launcher, message, 0);
			notification.flags = Notification.DEFAULT_LIGHTS
					| Notification.FLAG_NO_CLEAR;

			PendingIntent contentIntent = PendingIntent.getActivity(this, 0,
					resultIntent, 0);
			notification
					.setLatestEventInfo(this, title, message, contentIntent);
		} else {
			NotificationCompat.Builder mBuilder = new NotificationCompat.Builder(this)
					.setSmallIcon(R.drawable.ic_launcher)
					.setContentTitle(title).setContentText(message)
					.setDefaults(Notification.DEFAULT_LIGHTS)
					.setAutoCancel(true);

			if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
				if (longMessage != null) {
					mBuilder.setStyle(new NotificationCompat.BigTextStyle()
							.bigText(longMessage));
				}

				// The stack builder object will contain an artificial back
				// stack for the
				// started Activity.
				// This ensures that navigating backward from the Activity leads
				// out of
				// your application to the Home screen.
				TaskStackBuilder stackBuilder = TaskStackBuilder.create(this);
				// Adds the back stack for the Intent (but not the Intent
				// itself)
				stackBuilder.addParentStack(SamplerActivity.class);
				// Adds the Intent that starts the Activity to the top of the
				// stack
				stackBuilder.addNextIntent(resultIntent);
				PendingIntent resultPendingIntent = stackBuilder
						.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
				mBuilder.setContentIntent(resultPendingIntent);
			}

			notification = mBuilder.build();
		}

		NotificationManager mNotificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);

		// mId allows you to update the notification later on.
		mNotificationManager.notify(mId, notification);
	}
	
	@Override
	public void onClick(View v) {
		if (v.getId() == R.id.toggle_services) {
			if (SamplerService.isRunning()) {
				Log.d(TAG, "Sampler Service is running");
				showStopTransportDialog();
			} else {
				// Prompt user to select choice
				showTransportChoiceDialog();
				Log.d(TAG, "Sampler Service is NOT running");
			}
		}
	}
		
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		// Perform tasks on update
		SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
		if (prefs.getInt("Version", 0) < VERSION) {
			// Perform update tasks
			
			// Update version
			SharedPreferences.Editor editor = prefs.edit();
			editor.putInt("Version", VERSION);
			editor.commit();
		}
		
		setContentView(R.layout.activity_monitor);
		
		// Start synchronization service
		if (!SynchronizationService.isRunning()) 
			startService(new Intent(this, SynchronizationService.class));
		
		/* the buttons */
		toggleServicesButton = (Button) findViewById(R.id.toggle_services);
		if (SamplerService.isRunning()) {
			toggleServicesButton.setText(R.string.stop_services);
		}
		toggleServicesButton.setOnClickListener(this);
	}

	@Override
	protected void onDestroy() {
		super.onDestroy();
	}

	@Override
	public void onStopTransportAccept(DialogFragment dialog) {
		Journey journey = SamplerService.getJourney();

		stopService(new Intent(this, SamplerService.class));

		if (journey == null) {
			Log.e(TAG, "Journey is null");
			return; // DO NOTHING in this case
		}

		// Save the journey
		journey.setEndTime(Time.currentTimeMillis());
		journey.setFinished(true);
		journey.save();

		toggleServicesButton.setText(R.string.start_services);
		
		// Close notification
		dismissNotification(0);
	}

	@Override
	public void onStopTransportCancel(DialogFragment dialog) {
		// Do nothing
		
	}

	@Override
	public void onTransportChoiceCancelled(DialogFragment dialog) {
		// Do nothing
	}

	@Override
	public void onTransportSelected(DialogFragment dialog, Transport transport) {
		Log.d(TAG, "Activando Servicios, transporte seleccionado "+transport);
		
		Journey journey = new Journey();
		journey.setStartTime(Time.currentTimeMillis());
		journey.setTransport(transport);
		journey.save();

		Intent intent = new Intent(this, SamplerService.class);
		Log.d(TAG, "Created journey "+journey.getId());
		intent.putExtra("journey", journey.getId());
		startService(intent);

		toggleServicesButton.setText(R.string.stop_services);
		
		// Show sticky notification
		notifyUser(0, getString(R.string.notify_title), getString(R.string.notify_message), null);
	}
	
	/**
	 * Open the stop dialog
	 */
	private void showStopTransportDialog() {
		DialogFragment newFragment = new StopTransportDialogFragment();
	    newFragment.show(getSupportFragmentManager(), "StopTransportDialog");
	}

	
	/**
	 * Open the start dialog
	 */
	private void showTransportChoiceDialog() {
	    DialogFragment newFragment = new TransportChoiceDialogFragment();
	    newFragment.show(getSupportFragmentManager(), "TransportChoiceDialog");
	}
}
