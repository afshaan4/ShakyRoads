# Shaky Roads [WIP]

An app to record road quality, so here's the idea: read the accelerometer and GPS then save them to
a csv then you can plot the acceleration readings to a map or make pretty graphs and all that.

**Note:** It takes a while after you turn on your phones GPS till it gets your location


### TODO

- [ ] add a calibration step for the accelerometer (since the app uses androids linear accelerometer)
- [ ] make the save function save asynchronously
- [ ] allow the app to run in the background
- [ ] put all the heavy stuff on a separate thread
- [ ] plot acceleration readings onto a map, and other UI stuff
- [x] kotlin not java
- [x] save new readings only when location changes
- [x] tell the user when GPS is disabled
- [x] allow installing the app on external storage
- [x] dump readings to a csv file
- [x] Get the GPS to work
