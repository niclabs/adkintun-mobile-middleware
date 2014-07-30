Adkintun Mobile Middleware
==========================

Android Middleware for performing mobile sensing and monitoring. Originally created for the [Adkintun Mobile](http://www.adkintunmobile.cl) project, it is released under the [Apache Open Source License](http://www.apache.org/licenses/LICENSE-2.0.html) to be used by developers in their own software projects.


Software Architecture
----------------------

The following requirements guided the actual design and development of the middleware:
* extensibility, in order to add monitoring for new events with ease and requiring minimal modification of the existing code,
* small memory footprint, by storing the sensor data in memory the shortest time possible,
* ease of use, to integrate monitoring in an application as quickly as possible,
* component independence, we wanted the different components of the application to be as independent as possible. As an example, even though we may want to be able to save monitoring data to a database, we would not want to be restricted to using [SQLite](http://www.sqlite.org/) or to be obligated to use a database by design.
* Data exporting capabilities, in order to easily export data from different devices into multiple formats,
* reliability of data, we need to be able to trust that events occur when the library says they are monitored, thus the sequence of reporting and the time of reporting must be reliable.

The general design of the middleware is inspired by the [AWARE Framework](http://www.awareframework.com/), and some of the code for the different monitors was based on that project, given that they already had resolved many of the issues related to Android development. However that framework did not fulfill all of our requirements, motivating the development the present library.

![Main Library Classes](https://raw.githubusercontent.com/niclabs/adkintun-mobile-middleware/master/doc/img/software-design.png "Main Library Classes")

The structure of the main entities in the middleware is shown on Figure above, where the architecture of the central classes follows the observer and factory patterns. A general description of the different entities is provided below.

* The central class of the library is `Monitor`, defining the general structure for all monitoring classes, which will perform the task of listening to OS events. This class extends from the Android [Service](http://developer.android.com/guide/components/services.html) class, thus allowing each monitor to run as a daemon of the system. 
* A monitor can observe one or more `Events` of the Operating System. Events define different aspects of a monitoring task. For instance, the `Traffic` monitor can observe events of mobile traffic, WiFi traffic or application traffic, and a developer using the library may choose to activate a different one depending on the application.
* A `Listener` can be attached to a monitor, in order to be notified of new observations from an event,
* the notification usually comes in the form of an `Observation` object, carrying data about the event as well as the timestamp, and event type (a code to identify the event). 
* Observations can be `Persistent`, meaning they can be saved to a local database (SQLite for now) if desired, 
* they are also `Serializable`, meaning they can be exported to different formats (JSON, CSV, etc.).
* Preprocessing of observations to establish a context can be performed through the definition of `Proxy` objects, which can listen to multiple monitors in order to notify their own listeners of a specific context. One example of these objects is the `ConnectivityStatusProxy` which compares two consecutive connectivity observations and notifies its listeners of a change in connectivity or roaming status.

Scheduling of notifications is performed to ensure that observations arrive in the correct order, and periodical NTP synchronization is implemented to ensure that the reported timestamp of the events is correct.


Dependencies
------------

The library only has two software dependencies, included as jars under the `libs/` directory (we know is not good practice but maven is a pain)

* [GSON](https://code.google.com/p/google-gson/) (tested with version 2.2.4), for object serialization to JSON.
* [Sugar ORM](http://satyan.github.io/sugar/) (tested with version 1.3), as a SQLite ORM. 


Implemented Monitors
--------------------

* Accelerometer. Monitors raw accelerometer data in the [device coordinate system](http://developer.android.com/reference/android/hardware/SensorEvent.html)
* GlobalAccelerometer. Monitors device accelerometer data in the Earth coordinate system. It implements a Low pass filter to isolate device accelerometer data from the earth gravity.
* Connectivity. Monitors changes in Internet connectivity of the device. If supported by the device, it provides detailed connection status change data (authenticating, connecting, connected)
* Location. Monitors position changes on the device. Monitoring can be enabled using coarse or fine location.
* Screen. Monitors screen status change (on, unlocked, locked, off).
* Telephony. Monitors antenna, signal power, airplane mode and connection status changes. A new instance of `TelephonyObservation` (can be GSM or CDMA) is provided for each change, except on the case of signal power, where the instance is kept as long as there are no antena changes and only the signal strenght `Sample` is updated.
* Traffic, monitor device traffic statistics. It can perform monitoring of WiFi, Mobile and per Application statistics independently. Monitoring is perform periodically, with the period being configurable.
* Device Data. Although not a Monitor, the class `DeviceInfo` provides a summary of all device information (IMEI, baseband, model, brand, etc.)
* Boot detection. If configured, the library can monitor device and shutdown through the class 'Device'.

Usage
-----

A full example of a working application is provided under the `examples/` folder in the code. However, here is a quick start.

* First, create a monitor controller for binding your application to, although you can bind to a `Monitor` as you would [bind to any other Android Service](http://developer.android.com/guide/components/bound-services.html), controllers simplify the task. Here is how you create a `Traffic` monitor controller

```java
Controller<TrafficListener> trafficController = Traffic.bind(Traffic.class, context);
```

* Asign a listener to the controller, which will append it to the Traffic monitor when this is activated.

```java
trafficController.listen(trafficListener, true);
```

* Configure the monitor, creating a Bundle with the configuration data. Here is how you configure the sampling frequency of the Traffic monitor 

```java
Bundle bundle = new Bundle();

/* Configure the sampling frequency to 20 seconds */
bundle.putInt(Traffic.TRAFFIC_UPDATE_INTERVAL_EXTRA, 20);
```

* Activate the monitor, defining the events you wish to activate. 

```java
trafficController.activate(Monitor.TRAFFIC_MOBILE | Monitor.TRAFFIC_WIFI, bundle);
```

* Done! The listener will receive traffic data through the methods `onMobileTrafficChange` and `onWiFiTrafficChange`.


Below is a full example Activity

```java
package cl.niclabs.adkmobile;

import android.app.Activity;
import android.os.Bundle;
import android.util.Log;
import cl.niclabs.adkmobile.monitor.Monitor;
import cl.niclabs.adkmobile.monitor.Monitor.Controller;
import cl.niclabs.adkmobile.monitor.Traffic;
import cl.niclabs.adkmobile.monitor.data.TrafficObservation;
import cl.niclabs.adkmobile.monitor.listeners.TrafficListener;

public class MonitorActivity extends Activity implements TrafficListener {
	private static final String TAG = "MonitorActivity";
	
	/* Monitor controllers make easier binding to a Monitor */
	private Controller<TrafficListener> trafficController;
	
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		
		/* Bind controller to the current application context */
		trafficController = Traffic.bind(Traffic.class, this);
		
		/* Append this class as listener */
		trafficController.listen(this, true);
		
		Bundle bundle = new Bundle();
		
		/* Configure the sampling frequency to 20 seconds */
		bundle.putInt(Traffic.TRAFFIC_UPDATE_INTERVAL_EXTRA, 20);
		
		/* Activate controller for monitoring mobile and WiFi traffic */
		trafficController.activate(Monitor.TRAFFIC_MOBILE | Monitor.TRAFFIC_WIFI, bundle);
	}

	@Override
	protected void onDestroy() {
		// TODO Auto-generated method stub
		super.onDestroy();
		
		/* Unbind from the monitor and destroy the service if there are
		 * no other classes bound to it  */
		trafficController.unbind();
	}

	@Override
	public void onMobileTrafficChange(TrafficObservation trafficState) {
		/* The state will be serialized to JSON */
		Log.i(TAG, "Received new mobile traffic state "+trafficState);
	}

	@Override
	public void onWiFiTrafficChange(TrafficObservation trafficState) {
		Log.i(TAG, "Received new WiFi traffic state "+trafficState);
	}

	@Override
	public void onApplicationTrafficChange(TrafficObservation trafficState) {
		// This will never be used
	}
}
```


Configuration
-------------

The permissions required for each monitor are specified in the code documentation. The following configuration is required in the `AndroidManifest.xml` of the application in order to activate clock synchronization (implemented on `cl.niclabs.adkmobile.services.ClockService`) and persistence with Sugar ORM.

```xml
<application android:name="cl.niclabs.adkmobile.AdkintunMobileApp"><!-- android:name is required to activate clock synchronization and persistance !-->
	   <!-- Give permission to the Traffic monitor. It must be added for each service required in the platform !-->
        <service android:name="cl.niclabs.adkmobile.monitor.Traffic" ></service>
</application>
```

The same configuration as [Sugar ORM](http://satyan.github.io/sugar/) is required on the manifest to enable persistance

```xml
<!-- Database configuration -->
<meta-data android:name="DATABASE" android:value="mydb.db" />
<meta-data android:name="VERSION" android:value="1" />
<meta-data android:name="QUERY_LOG" android:value="true" />

<!-- Do not change, required to store monitor observations !-->
<meta-data android:name="DOMAIN_PACKAGE_NAME" android:value="cl.niclabs.adkmobile.monitor.data" />
```

In order to listen to boot status changes, the following code must be added inside `<application></application>` on the manifest.

```xml
<!-- Register receiver in order to monitor device boot state -->
<receiver android:name="cl.niclabs.adkmobile.monitor.Device" 
  android:enabled="true"
  android:permission="android.permission.RECEIVE_BOOT_COMPLETED" >
  <intent-filter>
    <action android:name="android.intent.action.BOOT_COMPLETED" />
    <action android:name="android.intent.action.ACTION_SHUTDOWN" />
    <category android:name="android.intent.category.DEFAULT" />
  </intent-filter>
</receiver>
```