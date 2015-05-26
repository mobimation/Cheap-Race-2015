package tv.laidback.cheaprace2015;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import tv.laidback.cheaprace2015.enteties.RaceLocation;
import tv.laidback.cheaprace2015.enteties.Trip;
import tv.laidback.cheaprace2015.sql.LocationDataSource;
import tv.laidback.cheaprace2015.sql.TripDataSource;
import android.app.AlertDialog;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.location.Criteria;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.Ringtone;
import android.os.Binder;
import android.os.Bundle;
import android.os.Handler;
import android.os.IBinder;
import android.os.Looper;
import android.provider.Settings;
import android.support.v4.app.NotificationCompat.Builder;
import android.util.Log;

public class LocalisationService extends Service implements LocationListener {
	private static String TAG = LocalisationService.class.getName();
	private NotificationManager mNM;
	
	private static LocalisationService instance = null;

	public static boolean isInstanceCreated() { 
	  return instance != null; 
	}
	   
	// The minimum distance to change Updates in meters
	private static final long MIN_DISTANCE_CHANGE_FOR_UPDATES = 1; // in meters

	// The minimum time between updates in milliseconds
	public static final long MIN_TIME_BW_UPDATES = 1000 * 5 * 1; // in
																	// milliseconds

	// Unique Identification Number for the Notification.
	// We use it on Notification start, and to cancel it.
	private int NOTIFICATION = R.string.local_service_started;
	private LocationManager locationManager;

	// flag for GPS status
	boolean isGPSEnabled = false;

	// flag for network status
	boolean isNetworkEnabled = false;

	// flag for GPS status
	boolean canGetLocation = false;

	private long elapsedTime;

	public static long START_TIME = 0;
	public static int TIMER_INTERVAL = 1000;
	private Handler timeHandler = new Handler();

	Location location; // location
	double latitude; // latitude
	double longitude; // longitude
	private TripDataSource tripDatasource;
	private LocationDataSource locationDatasource;
	private Trip trip;
	private MockLocationProvider mp=null; 
	/**
	 * ride and compare holds the vehicle types of the
	 * driven car type and the one to compare with.
	 * Both are stored in the trip database so that
	 * when a trip is later selected for map viewing
	 * it is possible to show a comparison table
	 * where calculations are based on 
	 * vehicle types and trip distance.
	 */
	private int ride;    // The type of vehicle driven
	private int compare; // The comparison type

	/**
	 * Class for clients to access. Because we know this service always runs in
	 * the same process as its clients, we don't need to deal with IPC.
	 */
	public class LocalBinder extends Binder {
		public LocalisationService getService() {
			return LocalisationService.this;
		}
	}

	@Override
	public void onCreate() {

		mNM = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
		// Display a notification about us starting. We put an icon in the
		// status bar.

		openDatabase();
		showNotification();
		instance=this;
	}

	private void openDatabase() {
		
//		try {
//			Dao<se.sust.laddbilar.data.Trip, Integer> tripDao = DatabaseHelper.getInstance(LocalisationService.this).getTripDao();
//		
//			se.sust.laddbilar.data.Trip new_trip = new se.sust.laddbilar.data.Trip();
//			new_trip.setStartdate(new Date());
//		
//		int id = tripDao.create(new_trip);
//		
//		se.sust.laddbilar.data.Trip xxtrip = tripDao.queryForId(id);
//		
//		
//		} catch (SQLException e) {
//			// TODO Auto-generated catch block
//			e.printStackTrace();
//		}
		
		tripDatasource = new TripDataSource(this);
		tripDatasource.open();
		locationDatasource = new LocationDataSource(this);
		locationDatasource.open();
		Log.i(TAG,"database opened");
	}

	private void closeDatabase() {
		tripDatasource.close();
		tripDatasource = null;
		locationDatasource.close();
		locationDatasource = null;
		Log.i(TAG,"database closed");
	}

	private Trip createTrip() {
		Date now = new Date();

		Trip created = tripDatasource.createTrip(ride, compare, now.toString());
		Log.v(TAG, "trip " + created.getId() + " created, vehicle type="+ride+".");
		START_TIME = System.currentTimeMillis();
		timeHandler.postDelayed(timerRunnable, TIMER_INTERVAL);

		// SharedPreferences sharedPreferences = getSharedPreferences("gps",
		// Context.MODE_PRIVATE);
		// SharedPreferences.Editor editor = sharedPreferences.edit();
		// editor.putInt("activeTripId", created.getId());
		// editor.commit();

		// LocationLibrary.startAlarmAndListener(MainActivity.this);
		// LocationLibrary.forceLocationUpdate(MainActivity.this);

		// stats_wrapper.setVisibility(View.VISIBLE);
		//
		// setStats(0, 0, 0, 0, 0, 0, 0, 0);
		//
		// registerLocationReceiver();

		return created;
	}

	private void stopTrip(Trip trip) {
		long END_TIME = System.currentTimeMillis();
		elapsedTime = (END_TIME - START_TIME);

		if (trip.getDistance() > 0) {
			tripDatasource.open();
			tripDatasource.setEnded(trip.getId(), elapsedTime);
		} else {
			removeCurrentTrip();
		}
		trip = null;

		timeHandler.removeCallbacks(timerRunnable);
		setElapsedTime(0);

		broadcastDataChanged();
	}

	@Override
	public int onStartCommand(Intent intent, int flags, int startId) {
		Log.i("LocalService", "Received start id " + startId + ": " + intent);
        // Selected vehicle types passed to the service
		ride = intent.getExtras().getInt("ride", 0);
		compare = intent.getExtras().getInt("compare", 0);

		getLocation();
		trip = createTrip();

		showNotification();

		// We want this service to continue running until it is explicitly
		// stopped, so return sticky.
		return START_STICKY;
	}

	@Override
	public void onDestroy() {
		stopTrip(trip);
		// added closeDatabase();
		stopUsingGPS();
		// Cancel the persistent notification.
		mNM.cancel(NOTIFICATION);

/*		// Tell the user we stopped.
		Toast.makeText(this, R.string.local_service_stopped,
		Toast.LENGTH_SHORT).show(); */
		Log.i(TAG,"Trip recording service stopped.");
		instance=null;
		super.onDestroy();
	}

	@Override
	public void onLowMemory() {
		Log.w(TAG,"System indicates low memory, trip recording might be at risk...");
	}
	@Override
	public IBinder onBind(Intent intent) {
		return mBinder;
	}

	// This is the object that receives interactions from clients. See
	// RemoteService for a more complete example.
	private final IBinder mBinder = new LocalBinder();
	private RaceLocation firstSavedPosition;
	private RaceLocation lastSavedPosition;
	private Builder mBuilder;

	/**
	 * Show a notification while this service is running.
	 */
	private void showNotification() {
		Intent contentIntent = new Intent(this, MainActivity.class);
		contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
		PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);

		mBuilder = new Builder(this).setContentIntent(contentPendingIntent).setSmallIcon(R.drawable.elcykel_and_bike).setOngoing(true)
				.setTicker(getString(R.string.local_service_started)).setContentInfo("Eck").setContentTitle("Loggar resa med "+MainActivity.vehicleNames[ride]+"...")
				.setContentText(String.format("Tid: %.0f s Längd: %.2f km", (float) (getElapsedTime() / 1000), 0f));
        mBuilder.setProgress(0, 0, true);
		Notification note = mBuilder.build();

		startForeground(NOTIFICATION, note);
	}

	private void updateNotification() {
		mBuilder = mBuilder.setContentText(String.format("Tid: %.0f s Längd: %.2f km", (float) (getElapsedTime() / 1000 ), trip.getDistance() / 1000));
		Notification note = mBuilder.build();
		startForeground(NOTIFICATION, note);
	}

	public Location getLocation() {
		try {
		  final SharedPreferences sharedPreferences = getSharedPreferences("gps", Context.MODE_PRIVATE);	
		  if (sharedPreferences.getBoolean("mock", false)) {
			   // We set up a simulating "mock" GPS provider
				locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);
				try {
			    locationManager.addTestProvider("mockgps", false, false, false, false, true, false, false, Criteria.POWER_LOW, Criteria.ACCURACY_FINE);
				}
				catch (IllegalArgumentException iae) {
			        Log.i(TAG,"GPS mock provider already defined");
				}
			    locationManager.setTestProviderEnabled("mockgps", true);
			    locationManager.requestLocationUpdates("mockgps", 0, 0, this);

			    // Read the locations to present from a text file.
			    try {
			        List data = new ArrayList();
			        InputStream is = getAssets().open("trip.txt");
			        BufferedReader reader = new BufferedReader(new InputStreamReader(is));
			        String line = null;
			        while ((line = reader.readLine()) != null) {

			            data.add(line);
			        }
			        Log.i(TAG,"Mock positions loaded = "+data.size()+ " lines");

			        if (mp!=null) 
			        	 if (mp.isAlive()) 
			        		mp.kill();
			        
			        mp=new MockLocationProvider(locationManager, "mockgps", data);
			        mp.start();

			    } catch (IOException e) {
			        e.printStackTrace();
			        Log.e(TAG, "Mock position file read failure: "+e.getMessage());
			    } 
		  }
		  else { // We need to run real GPS
			locationManager = (LocationManager) getSystemService(LOCATION_SERVICE);

			// First take care of turning off any mock provider

			boolean mockExist=true;
			try {
			  LocationProvider lp=locationManager.getProvider("mockgps");
			  if (lp==null)
				  mockExist=false;
			}
			catch (IllegalArgumentException iae) {
				mockExist=false;
			}
			if (mockExist) {  // Deal with the mock provider only if it exists
			      locationManager.setTestProviderEnabled("mockgps", false);
			      locationManager.removeTestProvider("mockgps"); // Very important to disable the mock provider...
			}
			
			// Check if GPS enabled
			isGPSEnabled = locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER);
			isNetworkEnabled = locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER);
			// Now set up GPS usage
			Criteria myCriteria = new Criteria();
			myCriteria.setAccuracy(Criteria.ACCURACY_FINE);
			myCriteria.setPowerRequirement(Criteria.POWER_LOW);

			// myCriteria.setPowerRequirement(Criteria.POWER_LOW); // let
			// Android select the right location provider for you
			// String myProvider = locationManager.getBestProvider(myCriteria,
			// true); // finally require updates at -at least- the desired rate

			// long minTimeMillis = 600000; // 600,000 milliseconds make 10
			// minutes
			locationManager.requestLocationUpdates(MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, myCriteria, this, Looper.getMainLooper());
			location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
			// 0, 0, this);
			// Log.d("Network", "Network");
			// if (locationManager != null) {
			// location =
			// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			// if (location != null) {
			// latitude = location.getLatitude();
			// longitude = location.getLongitude();
			// }
			// }

			// if (!isGPSEnabled && !isNetworkEnabled) {
			// // no network provider is enabled
			// } else {
			// this.canGetLocation = true;
			// // First get location from Network Provider
			// if (isNetworkEnabled) {
			// locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER,
			// MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			// Log.d("Network", "Network");
			// if (locationManager != null) {
			// location =
			// locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
			// if (location != null) {
			// latitude = location.getLatitude();
			// longitude = location.getLongitude();
			// }
			// }
			// }
			// // if GPS Enabled get lat/long using GPS Services
			// if (isGPSEnabled) {
			// if (location == null) {
			// locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
			// MIN_TIME_BW_UPDATES, MIN_DISTANCE_CHANGE_FOR_UPDATES, this);
			// Log.d("GPS Enabled", "GPS Enabled");
			// if (locationManager != null) {
			// location =
			// locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
			// if (location != null) {
			// latitude = location.getLatitude();
			// longitude = location.getLongitude();
			// }
			// }
			// }
			// }
			// }
		  }

		} catch (Exception e) {
			Log.e(TAG, "FATAL ERROR: ",e);
			e.printStackTrace();
		}

		return location;
	}

	/**
	 * Stop using GPS listener Calling this function will stop using GPS in your
	 * app
	 * */
	public void stopUsingGPS() {
		if (locationManager != null) {
			locationManager.removeUpdates(LocalisationService.this);
		}
		stopForeground(true);
		if (mp != null)
			if (mp.isAlive()) {
				mp.kill();
			}
			else
				mp=null;
	}

	/**
	 * Function to get latitude
	 * */
	public double getLatitude() {
		if (location != null) {
			latitude = location.getLatitude();
		}

		// return latitude
		return latitude;
	}

	/**
	 * Function to get longitude
	 * */
	public double getLongitude() {
		if (location != null) {
			longitude = location.getLongitude();
		}

		// return longitude
		return longitude;
	}

	/**
	 * Function to check GPS/wifi enabled
	 * 
	 * @return boolean
	 * */
	public boolean canGetLocation() {
		return this.canGetLocation;
	}

	/**
	 * Function to show settings alert dialog On pressing Settings button will
	 * lauch Settings Options
	 * */
	public void showSettingsAlert() {
		AlertDialog.Builder alertDialog = new AlertDialog.Builder(LocalisationService.this);

		// Setting Dialog Title
		alertDialog.setTitle("GPS is settings");

		// Setting Dialog Message
		alertDialog.setMessage("GPS is not enabled. Do you want to go to settings menu?");

		// On pressing Settings button
		alertDialog.setPositiveButton("Settings", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				Intent intent = new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
				LocalisationService.this.startActivity(intent);
			}
		});

		// on pressing cancel button
		alertDialog.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
			public void onClick(DialogInterface dialog, int which) {
				dialog.cancel();
			}
		});

		// Showing Alert Message
		alertDialog.show();
	}

	private void removeCurrentTrip() {
		tripDatasource.open();
		tripDatasource.deleteTrip(trip.getId());
		locationDatasource.open();
		locationDatasource.deleteTrip(trip.getId());

	}

	private Runnable timerRunnable = new Runnable() {
		@Override
		public void run() {
			setElapsedTime(getElapsedTime() + TIMER_INTERVAL);
			timeHandler.postDelayed(this, TIMER_INTERVAL);
			updateNotification();
		}
	};

	@Override
	public void onLocationChanged(Location location) {
		long ageInMS = System.currentTimeMillis() - location.getTime();
		if (ageInMS > 5000) {
			Log.w(TAG, "OLD BAD LOCATION!");
			return;
		}

		if (trip == null) {
			Log.w(TAG, "No active trip, can't save location");
			return;
		}
		if (locationDatasource == null) {
			Log.w(TAG, "No active locationDatasource, can't save location");
			return;
		}
		if (location.getAccuracy() > 75) {
			Log.w(TAG, "Accuracy is greater than 75 meters (" + location.getAccuracy() + "m), do not count it!");
			return;
		}

		Log.v(TAG, "Got location update lng:" + location.getLongitude() + " lat:" + location.getLatitude());
		// Toast.makeText(this, "Got location update lng:" +
		// location.getLongitude() + " lat:" + location.getLatitude(),
		// Toast.LENGTH_SHORT).show();
		RaceLocation created = locationDatasource.createLocation(new RaceLocation(trip.getId(), location));

		if (firstSavedPosition == null) {
			firstSavedPosition = created;
		}

		Number distanceSinceLastLocation = 0;
		Number tripTime = 0;
		if (lastSavedPosition != null) {
			distanceSinceLastLocation = TripCalculator.calculateDistanceOfLocations(lastSavedPosition, created);
			tripTime = created.getTimestamp().getTime() - firstSavedPosition.getTimestamp().getTime();
		} else {
			distanceSinceLastLocation = 0;
			tripTime = 0;
		}
		trip.setDistance(trip.getDistance() + distanceSinceLastLocation.doubleValue());

		trip.setTime(tripTime.intValue());

		Log.v(TAG, "Trip time is now " + tripTime.intValue());
		if (distanceSinceLastLocation.doubleValue() > 0) {
			Log.v(TAG, "Adding " + distanceSinceLastLocation.doubleValue() + " meters to trip");
			Log.v(TAG, "distance on this trip is  " + trip.getDistance() + " meters");
			tripDatasource.updateTrip(trip.getId(), trip.getDistance(), trip.getTimeMs());
		}
		lastSavedPosition = created;
		broadcastDataChanged();
	}

	private void broadcastDataChanged() {
		Intent bintent = new Intent();
		bintent.setAction("se.sust.laddbilar.DATA_CHANGED");
		sendBroadcast(bintent);
		Log.i(TAG, "broadcasts DATA_CHANGED");
	}

	@Override
	public void onProviderDisabled(String provider) {
		Log.v(TAG, "provider disabled (onProviderDisabled):" + provider);

	}

	@Override
	public void onProviderEnabled(String provider) {
		Log.v(TAG, "provider enabled (onProviderEnabled):" + provider);
	}

	@Override
	public void onStatusChanged(String provider, int status, Bundle extras) {
		Log.v(TAG, "status changed(onStatusChanged):" + provider);
	}

	public int getCurrentTripId() {
		// TODO Auto-generated method stub
		return trip.getId();
	}

	public Trip getCurrentTrip() {
		// TODO Auto-generated method stub
		return trip;
	}

	public long getElapsedTime() {
		return elapsedTime;
	}

	private void setElapsedTime(long elapsedTime) {
		this.elapsedTime = elapsedTime;
	}
}