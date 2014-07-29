package cl.niclabs.adkmobile.monitor.data;

import cl.niclabs.adkmobile.data.Persistent;

public class Journey extends Persistent<Journey> {
	public static enum Transport {
		BICYCLE(3), BUS(1), CAR(5), METRO(2), OTHER(0), WALKING(4);
		
		public static Transport getInstance(int value) {
			for (Transport n: Transport.values()) {
				if (n.value() == value) {
					return n;
				}
			}
			return OTHER;
		}
		
		private int value;
		
		private Transport(int value) {
			this.value = value;
		}
		
		public int value() {
			return value;
		}
	}
	
	
	private long endTime;
	private boolean finished;
	private long startTime;
	private boolean syncedAcceleration;
	private boolean syncedLocation;
	private int transport;
	
	/**
	 * Required by Sugar ORM. 
	 */
	public Journey() {
	}

	public long getEndTime() {
		return endTime;
	}

	public long getStartTime() {
		return startTime;
	}

	public Transport getTransport() {
		return Transport.getInstance(transport);
	}

	public boolean isFinished() {
		return finished;
	}

	public boolean isSyncedAcceleration() {
		return syncedAcceleration;
	}

	public boolean isSyncedLocation() {
		return syncedLocation;
	}

	public void setEndTime(long endTime) {
		this.endTime = endTime;
	}

	public void setFinished(boolean finished) {
		this.finished = finished;
	}

	public void setStartTime(long startTime) {
		this.startTime = startTime;
	}

	public void setSyncedAcceleration(boolean syncedAcceleration) {
		this.syncedAcceleration = syncedAcceleration;
	}

	public void setSyncedLocation(boolean syncedLocation) {
		this.syncedLocation = syncedLocation;
	}

	public void setTransport(Transport transport) {
		this.transport = transport.value();
	}

}
