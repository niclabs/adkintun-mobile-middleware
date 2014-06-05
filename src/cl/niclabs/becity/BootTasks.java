package cl.niclabs.becity;

import android.content.Context;
import android.content.Intent;
import cl.niclabs.adkmobile.monitor.Device;

/**
 * Perform boot and shutdown actions
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class BootTasks extends Device {
	@Override
	public void onBootCompleted(final Context context) {
		super.onBootCompleted(context);
		
		// Start synchronization service
		if (!SynchronizationService.isRunning()) 
			context.startService(new Intent(context, SynchronizationService.class));
	}

	@Override
	public void onShutdown(Context context) {
		super.onShutdown(context);
		
		// Stop synchronization service
		if (SynchronizationService.isRunning())
			context.stopService(new Intent(context, SynchronizationService.class));
	}
}
	