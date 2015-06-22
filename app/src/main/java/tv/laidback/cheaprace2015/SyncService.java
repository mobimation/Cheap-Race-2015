package tv.laidback.cheaprace2015;

import android.app.Service;
import android.content.Intent;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

/**
 * SyncService periodically checks for proximity to the Cheap Race 2015 Hub server.
 * If the server is found within range a sync operation is initiated.
 * After a successful sync operation the service will back off for 15 minutes before
 * attempting a further sync operation. The user can manually request a sync operation
 * when so desired which will occur upon network availability.
 *
 */
public class SyncService extends Service {
    private static String TAG = SyncService.class.getName();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private int NOTIFICATION = 101;

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private long elapsedTime;
    public static long START_TIME = 0;
    public static int TIMER_INTERVAL = 30000; // Once per 30 seconds

    private Handler timeHandler = new Handler();

    public SyncService() {
    }

    /**
     * Class for clients to access. Because we know this service always runs in
     * the same process as its clients, we don't need to deal with IPC.
     */
    public class LocalBinder extends Binder {
        public SyncService getService() {
            return SyncService.this;
        }
    }

    @Override
    public IBinder onBind(Intent intent) {
        Log.d(TAG,"onBind()");
        return mBinder;
    }

    @Override
    public void onCreate() {
        START_TIME = System.currentTimeMillis();
        timeHandler.postDelayed(timerRunnable, TIMER_INTERVAL);

      Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start id " + startId + ": Intent=" + intent);

        /* ride = intent.getExtras().getInt("ride", 0);

        getLocation();
        trip = createTrip(); */

        showNotification();

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    @Override
    public void onDestroy() {
        // transferEnds();
        timeHandler.removeCallbacks(timerRunnable);
        Log.d(TAG,"onDestroy()");
    }

    /**
     * Run a timer that periodically updates the UI
     */
    private Runnable timerRunnable = new Runnable() {
        @Override
        public void run() {
            setElapsedTime(getElapsedTime() + TIMER_INTERVAL);
            timeHandler.postDelayed(this, TIMER_INTERVAL);
            updateNotification();
        }
    };
    public long getElapsedTime() {
        return elapsedTime;
    }

    private void setElapsedTime(long elapsedTime) {
        this.elapsedTime = elapsedTime;
    }
    /**
     * Show a notification while this service is running.
     */
    private void showNotification() {

        // TODO Implement service communication interface

        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(new Intent().setAction("ping").putExtra("Greeting", "Service Running"));
        /*
        Intent contentIntent = new Intent(this, MainActivity.class);
        contentIntent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent contentPendingIntent = PendingIntent.getActivity(this, 0, contentIntent, PendingIntent.FLAG_CANCEL_CURRENT);


        mBuilder = new NotificationCompat.Builder(this).setContentIntent(contentPendingIntent).setSmallIcon(R.drawable.elcykel_and_bike).setOngoing(true)
                .setTicker(getString(R.string.local_service_started)).setContentInfo("Eck").setContentTitle("Loggar resa")
                .setContentText(String.format("Tid: %.0f min Längd: %.2f km", (float) (getElapsedTime() / 1000 / 60), 0f));

        Notification note = mBuilder.build();
        startForeground(NOTIFICATION, note);
*/

    }

    private void updateNotification() {
        Log.d(TAG,"Sync, updating UI..");
        setElapsedTime(getElapsedTime()+TIMER_INTERVAL);  // Increment time
        showNotification();

        // Experimental, simple test of stopping
//        if (getElapsedTime()>4000)
//            transferEnds();

        // TODO Insert code for updating UI
        /* mBuilder = mBuilder.setContentText(String.format("Tid: %.0f min Längd: %.2f km", (float) (getElapsedTime() / 1000 / 60), trip.getDistance() / 1000));
        Notification note = mBuilder.build();
        startForeground(NOTIFICATION, note);  */
    }

    private void transferEnds() {
            long END_TIME = System.currentTimeMillis();
            elapsedTime = (END_TIME - START_TIME);
            Log.d(TAG,"Transfer done, "+elapsedTime+" milliseconds");
            timeHandler.removeCallbacks(timerRunnable);
            setElapsedTime(0);

        // broadcastDataChanged();

    }
}
