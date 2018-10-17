# Shaky Roads [WIP]

An app to record road quality, so here's the idea: read the accelerometer and GPS then save them to
a csv then you can plot the acceleration readings to a map or make pretty graphs and all that.

**Note:** It takes a while after you turn on your phones GPS till it displays your latitude and longitude.

### TODO

- [ ] add a calibration step for the accelerometer (since the app uses androids linear accelerometer)
- [x] kotlin not java
- [ ] plot acceleration readings onto a map, and other UI stuff
- [ ] make the save function save asynchronously or save to a buffer then to the file
- [x] save new readings only when location changes
- [x] tell the user when GPS is disabled
- [x] allow installing the app on external storage
- [x] dump readings to a csv file
- [x] Get the GPS to work
