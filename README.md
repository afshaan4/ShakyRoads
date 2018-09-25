# Shaky Roads [WIP]

This code uses your phones accelerometer and GPS to measure movement and your location
and dumps those readings to a csv file, then using that data you can make a map of road quality.

**Note:** It takes a while after you turn on your phones GPS till it displays your latitude and longitude.

### TODO

- [ ] save new acceleration readings only on location changes
- [ ] tell the user when GPS signal is weak
- [ ] add a calibration step for the accelerometer (since the app uses androids linear accelerometer)
- [ ] plot acceleration readings onto a map
- [ ] allow installing the app on external storage
- [x] dump readings to a csv file
- [x] Get the GPS to work