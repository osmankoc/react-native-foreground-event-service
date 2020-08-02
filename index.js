//
// import { NativeModules } from 'react-native';
//
// const { RNForegroundEventService } = NativeModules;
//
// export default RNForegroundEventService;

import {
  NativeModules,
  DeviceEventEmitter
} from 'react-native';
const RNForegroundEventService = NativeModules.RNForegroundEventService;
//RNevents

var ForegroundEventService = RNForegroundEventService; //? RNEkoLocationManager : {};

ForegroundEventService.on = function(event, callbackFn) {

  if (typeof callbackFn !== 'function') {
    throw 'ForegroundEventService: callback function must be provided';
  }

  if (this.RNevents.indexOf(event) < 0) {
    throw 'Unknown event "' + event + '"';
  }

  return DeviceEventEmitter.addListener(event, callbackFn);
}

ForegroundEventService.removeAllListeners = function(event) {
  if (event)
    DeviceEventEmitter.removeAllListeners(event);
  else {
    this.RNevents.forEach(function(RNevent) {
      DeviceEventEmitter.removeAllListeners(RNevent);
    });

  }
}

export default ForegroundEventService;
