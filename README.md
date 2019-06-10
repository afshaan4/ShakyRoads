# Shaky Roads [WIP]

An app to record road quality, so here's the idea: read the accelerometer and GPS then save them to
a csv so you can plot the acceleration readings to a map or make pretty graphs and all that.

Uses androids [linear accelerometer](https://developer.android.com/guide/topics/sensors/sensors_motion#sensors-motion-linear)
 to filter out acceleration from gravity
**Note:** It takes a while after you turn on your phones GPS till it gets your location


### TODO

- [ ] separate UI logic and sensor logic, so MainActivity stays small
- [ ] finding the logged file is a pain, somehow make it better
- [ ] add a calibration step for the accelerometer (since the app uses the "linear accelerometer")
- [ ] plot acceleration readings onto a map, and other UI stuff
- [x] kotlin not java
- [x] save new readings only when location changes
- [x] tell the user when GPS is disabled
- [x] allow installing the app on external storage
- [x] dump readings to a csv file
- [x] Get the GPS to work
