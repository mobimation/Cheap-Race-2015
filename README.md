# Cheap-Race-2015
The Swedish Jaguar Club "Cheap Race 2015" event app - partial implementation

Cheap Race is an almost annually recurring competition where members of the Swedish Jaguar Club form teams
made up of friends, family and what have you, and 
(1) purchase as low cost as possible UK registered car of Jaguar manufacture
(2) drive it home to Sweden visa France, Germany and Denmark.
The 2015 event gathered about 80 people driving some 28 vehicles.
This Android app was intended to contribute social sharing of photos and video clips among team participants
also in areas where insufficient Internet access would be the case. The app scans for the proximity of a
specific Wifi access point identity (SSID) and tries to connect to an SFTP (Secure FTP) server runnng on
a Raspberry Pi board connected to a Wifi router, all installed in one of the Cheap Race vehicles.
When proximity is detected the app connects to the SFTP server and begins syncing any shared photos/clips to the server
as well as retreiving such files uploaded by other CR2015 attendees. This occurs in a reslilient way where losing
proximity means the transfer is paused to be resumed when the Wifi network is again in proximity.
An algorithm determines to what extent a sync operation is retried upon repeated proximity cases.
The goal is that apps used by every event participant will be kept updated with all shared content so that
everyone can access what has been produced during the trip.  This can help reinforce the team spirit and
build social bonding among participants.

The app was not fully completed in time for July 2015 and was never officially announced or committed for such a release.
The intention from a developer perspective was to experience and evaluate the implementation of such an app during the event
to experience the dynamics of multiuser sync going in/out of proxility.

The app also implements a Google Maps v2 based presentation of the target city and meetup location
for each day going through Europe. By pressing the circular button overlayed on the map the user is taken
to the destination location for each day.

The app includes a startup playback of the Cheap Race Trailer, 
a Youtube video made in earlier years of the Cheap Race event.

The app does not include several of the planned features such as
- realtime position on map of every participating vehicle as reported by
  apps of team members to a central server while travelling.
- Facebook wall posting of shared material.
- Team menber list of shared content

The app contains a list of team mambers and it shall be possible for each team mamber to fill in
personal data accessibe by other users. This also includes a list of shared material accessible by others.

One value of the app so far is realizing it is possible to develop a generic travel/team app (code name "Chameleon")
that can be configured at launch time to have the app morph into a specific app for an event.
Such configuring would be done by reading configuration artifacts from a server according to an "Event ID"
and populating the app with the desired behavior. 

Gunnar Forsgren
Mobimation AB
July 2015
