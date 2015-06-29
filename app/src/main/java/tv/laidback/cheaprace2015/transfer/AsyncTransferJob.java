package tv.laidback.cheaprace2015.transfer;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;


/**
 * Created by gunnarforsgren on 2015-06-15.
 */
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.vngx.jsch.Channel;
import org.vngx.jsch.ChannelSftp;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.SftpProgressMonitor;
import org.vngx.jsch.config.SessionConfig;

import java.util.List;

/**
 * Transfer files to/from Secure FTP server in an asynchronous way.
 */
public class AsyncTransferJob extends AsyncTask<View, String, String> {
    final String PI_IP_ADDRESS="192.168.1.106";         // Pi IP address assigned by Wifi router
    // final String HUB_SSID = "Cheap Race 2015 Sync Hub"; // SSID of Wifi router
    final String HUB_SSID = "Solna003"; // SSID of Wifi router
    final int PI_SFTP_PORT=22;                          // Pi SFTP server port
    private static final String TAG = AsyncTransferJob.class.getSimpleName();
    private String ftpServer;
    private ProgressBar progressBar;
    private TextView statusLine;
    private TextView progressValue;
    private Context context;
    private WifiManager wm=null;

    public AsyncTransferJob(Context context, final String server, View... ui) {
        this.context=context;
        ftpServer = server;
        progressBar=(ProgressBar)ui[0];
        statusLine=(TextView)ui[1];
        progressValue=(TextView)ui[2];
        wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    }

    protected String doInBackground(View... params) {
        Log.d(TAG, "doInBackground");
        return testSFTP2();
    }

    protected void onProgressUpdate(String... status) {
        if (status[0]!=null)
          statusLine.setText(status[0]);
        if (status[1]!=null)
        progressValue.setText(status[1]);
    }

    protected void onPostExecute(String result) {
        statusLine.setText(result);
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

    /**
     * Check if the Cheap Race 2015 Sync Hub Wifi is detected by the device.
     * This should be polled periodically to allow spontaneous sync or run if a user
     * initiates a sync manually.
     * @param context
     * @return list of networks or null if no Wifi detected
     */
    private boolean cheapRaceWifiPresent(Context context) {
        List<ScanResult> results = wm.getScanResults();
        ScanResult bestSignal = null;
        int count = 1;
        String etWifiList = "";
        for (ScanResult result : results) {
            if (result.SSID.equals("Cheap Race 2015 Sync Hub")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Wifi Manager scan result receiver code
     *
     */
     private void scanReceive() {
         IntentFilter i = new IntentFilter();
         i.addAction(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION);
         context.registerReceiver(new BroadcastReceiver() {
             public void onReceive(Context c, Intent i) {
                 // Code to execute when SCAN_RESULTS_AVAILABLE_ACTION event occurs
                 scanResultHandler(wm.getScanResults()); // your method to handle Scan results
             //    if (ScanAsFastAsPossible) w.startScan(); // relaunch scan immediately
             //    else { /* Schedule the scan to be run later here */}
             }
         }, i);
     }

     private void scanResultHandler(List<ScanResult> results) {
        Log.d(TAG,"Scan result");
     }
/*
    // Launch  wifiscanner the first time here (it will call the broadcast receiver above)
    WifiManager wm = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
    boolean a = wm.startScan();
*/
    /**
     * Attempt to change Wifi connectivity over to the Sync Hub router
     * Keep note of any existing Wifi connection so it can be restored when sync done.
     */
    private boolean connectHub() {
        WifiInfo currentWifi = getConnectedWifi(context); // Keep note of any existing Wifi connection
        if (currentWifi!=null)
            if (!currentWifi.getSSID().equals(HUB_SSID)) {
                // Device connected to Wifi and it´s not the hub
                // Preserve connection info for later restore and change to hub Wifi
                final WifiManager wifiManager = (WifiManager) context.getSystemService(Context.WIFI_SERVICE);
                // TODO implement......
            }
        return true;
    }

    private String testSFTP2() {
   //     String PI_IP_ADDRESS="192.168.0.187";
            JSch jsch = JSch.getInstance();
            Session session;

            try {
                // Session createSession(String username, String host, int port, SessionConfig config) throws JSchException
                session = jsch.createSession("pi", PI_IP_ADDRESS, PI_SFTP_PORT);
                SessionConfig config = session.getConfig();
                config.setProperty("StrictHostKeyChecking", "no");
                // session.setConfig("StrictHostKeyChecking", "no");
                publishProgress("Connecting..", null);
                session.connect("raxabaxa".getBytes());
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;
                String dir = sftpChannel.lpwd();
                // sftpChannel.setPtyType("dumb");
                sftpChannel.cd("/media/cheaprace/video");
                List files = sftpChannel.ls("/media/cheaprace/video");
                publishProgress("Starts", null);
                sftpChannel.get("video.mp4", Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");
//              sftpChannel.get("video.mp4",Environment.getExternalStorageDirectory().getAbsolutePath()+"/video.mp4",new ProgressMonitor());
                publishProgress("Done.", null);
                sftpChannel.exit();
                session.disconnect();
            } catch ( org.vngx.jsch.exception.JSchException je) {
                je.printStackTrace();
            } catch (org.vngx.jsch.exception.SftpException e) {
                e.printStackTrace();
            }
    return "ok";
    }
    class ProgressMonitor implements SftpProgressMonitor
    {
        long n=0;
        long total=0;
        public ProgressMonitor() {}

        public void init(int op, java.lang.String src, java.lang.String dest, long max)
        {
            publishProgress("Started", null);
        }

        public boolean count(long bytes)
        {
            n=n + bytes;

            if (n>1000000) {
                total=total+n;
                publishProgress(""+total,null);
                n=0;
            }
            return(true);
        }

        public void end()
        {
            publishProgress("FINISHED!",null);
        }
    }

    /**
     *  Maintain awareness of connectivity changes
     */
    public static class ConnectivityChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent){
            // TODO implement actions upon connectivity change
        }
    }
}

