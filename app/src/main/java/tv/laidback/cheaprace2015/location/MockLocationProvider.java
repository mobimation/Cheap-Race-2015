package tv.laidback.cheaprace2015.location;

import java.io.IOException;
import java.util.Iterator;
import java.util.List;

import android.location.Location;
import android.location.LocationManager;
import android.util.Log;

/**
 * A mock location provider for simulated trip recording.
 * The provider reads location coordinates
 * from a text file and presents fake location data
 * at the same interval as the real GPS provider.
 * 
 * @author gunnarforsgren
 *
 */
public class MockLocationProvider extends Thread {

private List data;

private boolean alive;

private LocationManager locationManager;

private String mockLocationProvider;
private static String TAG = MockLocationProvider.class.getName();

public MockLocationProvider(LocationManager locationManager,
        String mockLocationProvider, List data) throws IOException {

    this.locationManager = locationManager;
    this.mockLocationProvider = mockLocationProvider;
    this.data = data;
    alive=true;
}
/**
 * Causes the mock provider loop to end
 * and the thread exits.
 */
public void kill() {
  this.interrupt(); // Terminates any sleep and run loop exits
}
/**
 * For each location of fixed data file, sleep a while and 
 * then output that location as GPS location event, until end of file
 * or until kill() method called. In both cases the
 * thread will exit.
 * kill() will result in an InterruptedException that
 * ends any sleep and the thread will die right away.
 */
  @Override
  public void run() {
	Log.i(TAG, "Mock GPS provider thread launched");
    Iterator itr=data.iterator();
    while (itr.hasNext()&&(alive)) {
        try {
            Thread.sleep(LocalisationService.MIN_TIME_BW_UPDATES);  // Interval between points
            
        } catch (InterruptedException e) {
        	    // We end up here is thread is killed.
            alive=false;
            Log.i(TAG, "Mock GPS provider thread sleep exited");
        }
        if (alive) { // Position data only if not quitting
         String str=(String)itr.next();
         // Set one position
         String[] parts = str.split(",");
         Double latitude = Double.valueOf(parts[0]);
         Double longitude = Double.valueOf(parts[1]);
         Double altitude = Double.valueOf(parts[2]);
         Location location = new Location(mockLocationProvider);
         location.setLatitude(latitude);
         location.setLongitude(longitude);
         location.setAltitude(altitude);

         // set the time in the location. If the time on this location
         // matches the time on the one in the previous set call, it will be
         // ignored
         location.setTime(System.currentTimeMillis());
         // Output the location as a GPS event
         locationManager.setTestProviderLocation(mockLocationProvider,
                location);
        }
        else
            Log.i(TAG, "Mock GPS provider thread exits");
    }
  }
}