package tv.laidback.cheaprace2015;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import java.io.IOException;


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

import java.io.FileOutputStream;

import java.io.OutputStream;
import java.security.cert.CertificateException;
import java.security.cert.X509Certificate;
import java.util.List;
import java.util.Vector;

import javax.net.ssl.SSLHandshakeException;
import javax.net.ssl.X509TrustManager;

/**
 * Transfer files to/from a Secure FTP server in an asynchronous way.
 */
public class AsyncTransferJob extends AsyncTask<View, String, String> {

    private static final String TAG = AsyncTransferJob.class.getSimpleName();
    private String ftpServer;
    private ProgressBar progressBar;
    private TextView statusLine;
    private TextView progressValue;

    public AsyncTransferJob(final String server, View... ui) {
        ftpServer = server;
        progressBar=(ProgressBar)ui[0];
        statusLine=(TextView)ui[1];
        progressValue=(TextView)ui[2];
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

/*
    private String testSFTP() {
        String PI_IP_ADDRESS="192.168.0.187";
        int PI_FTPS_PORT=22;


        FTPSClient ftps;
        ftps = new FTPSClient(false); */
 /*       try {
            KeyStore keyStore = KeyStore.getInstance("AndroidKeyStore");
            keyStore.load(null);
            Enumeration<String> aliases=keyStore.aliases();

            Log.d(TAG,"----CERTIFICATES------");
            while (aliases.hasMoreElements())
                Log.d(TAG,aliases.nextElement());
            Log.d(TAG, "----------------------");
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        */

        // ftps.addProtocolCommandListener(new PrintCommandListener(new PrintWriter(System.out)));
        //KeyManager keyManager = org.apache.commons.net.util.KeyManagerUtils.createClientKeyManager(new File(keystorePath), keystorePass);
        //KeyManagerFactory kmf = getInstance(KeyManagerFactory.getDefaultAlgorithm());
        // ftps.setDefaultTimeout(60 * 1000);
 /*       try {
            X509TrustManager easyTrustManager = new X509TrustManager() {

                public void checkClientTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                    // Oh, I am easy!
                }

                public void checkServerTrusted(X509Certificate[] chain,
                                               String authType) throws CertificateException {
                    // Oh, I am easy!
                }

                public X509Certificate[] getAcceptedIssuers() {
                    return null;
                }

            };
            ftps.setTrustManager(easyTrustManager);

            ftps.connect(PI_IP_ADDRESS, PI_FTPS_PORT);
            int reply = ftps.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftps.disconnect();
                Log.d(TAG,"FTP server refused connection.");
            }
            else {
                if (!ftps.login("pi", "raxabaxa")) {
                    Log.e(TAG,"Login failed");
                    ftps.logout();
                }
                else {
                    ftps.setFileType(FTP.BINARY_FILE_TYPE);
                    // Retrieve a video file over Wifi from Cheap Race 2015 Sync Server Hub
                    String filepath = Environment.getExternalStorageDirectory().getAbsolutePath();
                    OutputStream os = new FileOutputStream(filepath + "/localvideo.mp4");
                    // Navigate to repository
                    ftps.cwd("/home/pi/media/cheaprace/video");
                    // Retrieve sample video file
                    ftps.retrieveFile("Новости - YouTube.MP4", os);
                    // TODO Continue here
                    onProgressUpdate("100","Transfer done.");
                    Log.d(TAG, "File transferred.");
                    return "Success";
                }
            }
        }
        catch(SSLHandshakeException she) {
            Log.d(TAG, "SSLHandshakeException - " + she.getMessage());
        }
        catch(IOException ioe) {
            Log.d(TAG, "IOException - " + ioe.getMessage());
            if (ftps.isConnected())
            {
                try
                {
                    ftps.disconnect();
                }
                catch (IOException f)
                {
                    // do nothing
                }
            }
            System.err.println("Could not connect to server.");
        }
        return "Failed";
    }
*/
    private String testSFTP2() {
        String PI_IP_ADDRESS="192.168.0.187";
        int PI_FTPS_PORT=22;
            JSch jsch = JSch.getInstance();
            Session session;
            try {
                // Session createSession(String username, String host, int port, SessionConfig config) throws JSchException
                session = jsch.createSession("pi", PI_IP_ADDRESS, PI_FTPS_PORT);
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
        public ProgressMonitor() {;}

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
}

