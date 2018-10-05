# Shaky Roads [WIP]

An app to record road quality, so here's the idea: read the accelerometer and GPS then save them to
a csv then you can plot the acceleration readings to a map or make pretty graphs and all that.

**Note:** It takes a while after you turn on your phones GPS till it displays your latitude and longitude.

### TODO

- [ ] save new acceleration readings only on location changes, and have the save
      function only save when the location data != 0
- [ ] add a calibration step for the accelerometer (since the app uses androids linear accelerometer)
- [ ] plot acceleration readings onto a map
- [x] tell the user when GPS is disabled
- [x] allow installing the app on external storage
- [x] dump readings to a csv file
- [x] Get the GPS to work
