package cl.niclabs.adkmobile.utils;

import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

/**
 * Schedule tasks on centralized pool
 * 
 * @author Felipe Lalanne <flalanne@niclabs.cl>
 */
public class Scheduler {

	private static Scheduler scheduler;
	private ScheduledThreadPoolExecutor taskPool;
	
	private Scheduler() {
		taskPool = new ScheduledThreadPoolExecutor(0);
	}
	
	public static Scheduler getInstance() {
		if (scheduler == null)
			scheduler = new Scheduler();
		return scheduler;
	}
	
	/**
	 * Schedule the given task after the defined delay
	 * @param command
	 * @param delay
	 * @param unit
	 * @return
	 */
	public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
		return taskPool.schedule(command, delay, unit);
	}
	
	/**
	 * Scheduled a task to be run at a fixed rate
	 * @param command
	 * @param initialDelay
	 * @param period
	 * @param unit
	 * @return
	 */
	public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
		return taskPool.scheduleAtFixedRate(command, initialDelay, period, unit);
	}
	
	/**
	 * Execute a task immediately
	 * @param command
	 */
	public void execute(Runnable command) {
		taskPool.execute(command);
	}
	
	/**
	 * Initiates shutdown of the Scheduler and no new tasks are allowed
	 */
	public void shutdown() {
		taskPool.shutdown();
	}
}
