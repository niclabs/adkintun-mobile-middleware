## 1.3.9b (2017-07-03)
Features:
  - Fix SignalStrength notifications
  
## 1.3.8b (2017-05-09)
Features:
  - TelephonyObservations are now sent as individial samples
  
## 1.3.7b (2017-04-21)
Features:
  - Fix traffic monitoring in devices with android version > 6

## 1.3.6b (2016-07-27)

Features:
   - Add support for 4g's signal strength measurement.

## 1.3.5b (2015-10-23)

Features:
   - Fix traffic monitoring per application to work with API changes in Lollipop.

## 1.3.4b (2015-10-19)

Features:
  - Added Gradle 1.3.0 compatibility
  - Updated SDK support to version 21

## 1.3.3b (2015-03-05)

Features:
  - refactored code be compatible with android studio. Release jars can now be downloaded at the [niclabs bintray repository](https://bintray.com/niclabs-cl/maven/adkintun-mobile-middleware/1.3.3b/view)
  - updated library to use latest gson (2.3)

## 1.3.2b (2015-03-02)

Features:

  - refactored code to use [niclabs-commons](https://github.com/niclabs/commons-android) library, kept old classes for backwards compatibility


## 1.3.1b (2015-02-26)

Features:

  - Separated ClockService functionality into Clock service (under utils) and ClockSynchronization service to maintain synchronization status
