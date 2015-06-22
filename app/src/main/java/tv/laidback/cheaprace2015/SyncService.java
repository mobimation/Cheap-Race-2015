package tv.laidback.cheaprace2015;

import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.Binder;
import android.os.Handler;
import android.os.IBinder;
import android.support.v4.content.LocalBroadcastManager;
import android.telephony.TelephonyManager;
import android.text.TextUtils;
import android.util.Log;

import java.util.List;

/**
 * SyncService periodically checks for proximity to the Cheap Race 2015 Hub server.
 * If the server is found within range a sync operation is initiated.
 * After a successful sync operation the service will back off for 15 minutes before
 * attempting a further sync operation. The user can manually request a sync operation
 * when so desired which will occur upon network availability.
 *
 */
public class SyncService extends Service {
    private static final String TAG = SyncService.class.getName();

    // Unique Identification Number for the Notification.
    // We use it on Notification start, and to cancel it.
    private static final int NOTIFICATION = 101;

    // This is the object that receives interactions from clients. See
    // RemoteService for a more complete example.
    private final IBinder mBinder = new LocalBinder();

    private long wifiMonitoringElapsedTime;
    private long wifiScanStartTime;
    private long wifiScanDuration;
    private static final String HUB_SSID="Cheap Race 2015 Sync Hub";
    private static long WIFI_POLL_START_TIME = 0;
    private static final int WIFI_POLL_INTERVAL = 30000; // Once per 30 seconds
    private boolean proximity=false;

    private Handler wifiScanTrigger = new Handler();

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

    /**
     * Sync service starts - do init
     */
    @Override
    public void onCreate() {
        scanReceive();  // Prepare for Wifi scanning
        // Start hub proximity scan
        WIFI_POLL_START_TIME = System.currentTimeMillis(); // Time of service start
        // wifiScanTrigger.postDelayed(wifiDetector, WIFI_POLL_INTERVAL); // First scan runs after delay
        wifiScanTrigger.post(wifiDetector);  // First scan runs at once

      Log.d(TAG, "onCreate()");
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "Start id " + startId + ": Intent=" + intent);

        // We want this service to continue running until it is explicitly
        // stopped, so return sticky.
        return START_STICKY;
    }

    /**
     * Run when service stops running
     */
    @Override
    public void onDestroy() {
        wifiDetectorStop();       // Stop interval hub proximity scan
        Log.d(TAG,"onDestroy()");
        super.onDestroy();
    }

    /**
     * Handler for interval timer - triggers periodic scans for Cheap Race 2015 Sync Hub WiFi network availability
     */
    private Runnable wifiDetector = new Runnable() {
        @Override
        public void run() {
            wifiScanTrigger.postDelayed(this, WIFI_POLL_INTERVAL);            // Arm next trigger
            proximityCheck();                                                       // Do check
        }
    };
    // Return elapsed time from wifiDetector job first launched until most recent scan
    public long getWifiMonitoringElapsedTime() {
        return wifiMonitoringElapsedTime;
    }

    /**
     * Present Sync Hub Wifi detection results
     */
    private void showNotification() {
        // TODO Report scan results
        LocalBroadcastManager.getInstance(this.getApplicationContext()).sendBroadcast(new Intent().setAction("ping").putExtra("Greeting", "Service Running"));
    }

    /**
     * Return a current SSID if any. If empty it means the device does not currently have an active
     * Wifi connection.
     * @param context
     * @return
     */
    public static String getCurrentSsid(Context context) {
        String ssid = null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            final WifiInfo connectionInfo = wifiManager.getConnectionInfo();
            if (connectionInfo != null && !TextUtils.isEmpty(connectionInfo.getSSID())) {
                ssid = connectionInfo.getSSID();
            }
        }
        return ssid;
    }
    /**
     * Scan for Cheap Race 2015 Sync Hub Wifi within range
     */
    private void proximityCheck() {
        // TODO Implement Wifi proximity scan
        String connected=getCurrentSsid(getApplicationContext());
        // Launch  wifiscanner the first time here (it will call the broadcast receiver above)
        WifiManager wm = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        wifiScanStartTime=System.currentTimeMillis();
        boolean a = wm.startScan();

        Log.d(TAG,"Checking for Sync Hub Wifi proximity");

        wifiMonitoringElapsedTime = System.currentTimeMillis()-WIFI_POLL_START_TIME;

        showNotification();
    }

    /**
     * Wifi Manager scan result receiver code
     *
     */
    private void scanReceive() {
        IntentFilter i = new IntentFilter();
        i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
        getApplicationContext().registerReceiver(new BroadcastReceiver() {

            public void onReceive(Context c, Intent i) {
                wifiScanDuration = System.currentTimeMillis() - wifiScanStartTime;  // Measure scan time
                // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
                WifiManager w = (WifiManager) c.getSystemService(Context.WIFI_SERVICE);
                scanResultHandler(w.getScanResults()); // your method to handle Scan results
                //    if (ScanAsFastAsPossible) w.startScan(); // relaunch scan immediately
                //    else { /* Schedule the scan to be run later here */}
            }
        }, i);
    }

    /**
     * Check if the Cheap Race 2015 Sync Hub Wifi router is in list of detected Wifi networks
     * @param results
     */
    private void scanResultHandler(List<ScanResult> results) {
        // loop that goes through list
        proximity=false;
        Log.d(TAG, "Scan duration=" + wifiScanDuration + " ms");
        for (ScanResult result : results) {
            if (result.SSID.equals(HUB_SSID)) {
                proximity=true;
                Log.d(TAG,result.SSID+" in proximity, level="+result.level+" dBm");
                // TODO Notify user of Wifi proximity
                break;
            }
        }
        if (!proximity)
            Log.d(TAG,"Sync Hub Wifi out of range");

    }

    /**
     * Stop proximity detection job
     */
    private void wifiDetectorStop() {
            long END_TIME = System.currentTimeMillis();
            wifiMonitoringElapsedTime = (END_TIME - WIFI_POLL_START_TIME);
            Log.d(TAG,"Wifi proximity detection stopped, "+ wifiMonitoringElapsedTime +" milliseconds");
            wifiScanTrigger.removeCallbacks(wifiDetector);
    }

    /**
     * Return info about the connected WiFi network if any
     * @param context
     * @return null = no Wifi network connected or
     * WifiInfo about the connected Wifi network
     */
    private WifiInfo getConnectedWifi(Context context) {
        WifiInfo connectionInfo=null;
        ConnectivityManager connManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);

        NetworkInfo networkInfo = connManager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
        if (networkInfo.isConnected()) {
            final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
            connectionInfo = wifiManager.getConnectionInfo();
        }
        return connectionInfo;
    }

    // TODO test this out
    private boolean isDataConnected() {
        try {
            ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            return cm.getActiveNetworkInfo().isConnectedOrConnecting();
        } catch (Exception e) {
            return false;
        }
    }
    // TODO test this out
    private int isHighBandwidth() {
        ConnectivityManager cm = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if (info.getType() == ConnectivityManager.TYPE_WIFI) {
            WifiManager wm = (WifiManager) getSystemService(Context.WIFI_SERVICE);
            return wm.getConnectionInfo().getLinkSpeed();
        } else if (info.getType() == ConnectivityManager.TYPE_MOBILE) {
            TelephonyManager tm = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
            return tm.getNetworkType();
        }
        return 0;
    }
}
