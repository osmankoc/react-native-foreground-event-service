
# react-native-foreground-event-service

This library enables you to run a foreground service and schedule a job in given interval. In the library AlarmManager is used to schedule the jobs. So you may want to have a look at the Android AlarmManager documentation at this url; https://developer.android.com/reference/android/app/AlarmManager

Note: As you might know IOS does not have such property as Foreground service, so this library for only Android development.

## Getting started

`$ npm install react-native-foreground-event-service --save`

### Mostly automatic installation

`$ react-native link react-native-foreground-event-service`

### Manual installation


#### Android

1. Open up `android/app/src/main/java/[...]/MainActivity.java`
  - Add `import com.ForegroundEventLib.RNForegroundEventServicePackage;` to the imports at the top of the file
  - Add `new RNForegroundEventServicePackage()` to the list returned by the `getPackages()` method
2. Append the following lines to `android/settings.gradle`:
  	```
  	include ':react-native-foreground-event-service'
  	project(':react-native-foreground-event-service').projectDir = new File(rootProject.projectDir, 	'../node_modules/react-native-foreground-event-service/android')
  	```
3. Insert the following lines inside the dependencies block in `android/app/build.gradle`:
  	```
      compile project(':react-native-foreground-event-service')
  	```


## Usage
```javascript
import ForegroundEventService from 'react-native-foreground-event-service';

// Register the event;
    ForegroundEventService.on('serviceEvent', function () {
      console.log("ForegroundEventService serviceEvent is here");
    });
// Start the foreground service;

    ForegroundEventService.startBackgroundService({title: "My Application", body: "My application is working foreground!", interval: 60000, icon: "your_icon_drawable_name"}).then((res) => {
      console.log("ForegroundEventService", res);
    }).catch((err) => {
      console.error("ForegroundEventService start Error", err);
    });

    ForegroundEventService.stopBackgroundService().then(() => {
      console.log("ForegroundEventService", res);
    }).catch((err) => {
      console.error("ForegroundEventService stop Error", err);      
    });

```
