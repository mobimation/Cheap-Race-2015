package tv.laidback.cheaprace2015.transfer;
import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.List;

import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import org.vngx.jsch.Channel;
import org.vngx.jsch.ChannelSftp;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.SftpProgressMonitor;
import org.vngx.jsch.config.SessionConfig;

/**
 *
 * Created by gunnarforsgren on 2015-06-15.
 *
 * Downloads a file from a Secure FTP server in an asynchronous way.
 */
public class AsyncDownloadJob extends AsyncTask<View, String, String> {

    private static final String TAG = AsyncDownloadJob.class.getSimpleName();
    private String      ftpServer;
    private ProgressBar progressBar;
    private TextView    statusLine;
    private TextView    progressValue;
    private View        cancelButton;
    private final byte[] buffer = new byte[16384];

    /**
     * Constructor
     * @param server
     * @param ui
     */
    public AsyncDownloadJob(final String server, View... ui) {
        ftpServer = server;
        progressBar=(ProgressBar)ui[0];
        statusLine=(TextView)ui[1];
        progressValue=(TextView)ui[2];
        cancelButton=ui[3];
    }

    /**
     * Run actual background job
     * @param params
     * @return
     */
    @Override
    protected String doInBackground(View... params) {
        Log.d(TAG, "doInBackground");
        String ret=testSFTP(1);
        return ret;
    }

    /**
     * UI update from asynchronous job
     * @param status
     */
    @Override
    protected void onProgressUpdate(String... status) {
        if (status[0]!=null)
        // Transfer job status line
          statusLine.setText(status[0]);
        // Progress bar value 0-99
        if (status[1]!=null)
            progressBar.setProgress(new Integer(status[1]).intValue());
        // Progress percentage string
        if (status[2]!=null)
            progressValue.setText(status[2]);
        // Cancel Button visibility control
        if (status[3]!=null) {
            if (status[3].length() == 0) // Empty string means not visible
                cancelButton.setVisibility(View.INVISIBLE);
            else
                cancelButton.setVisibility(View.VISIBLE);
        }
    }

    /**
     * Called when job is cancelled.
     * When it happend vngx.jsch throws an InterruptedException instead of
     * requiring polling of isCancelled(). In the interrupt handler, when
     * isCancelled() becomes called we end up here.
     * @param result
     */
    @Override
    protected void onCancelled(String result) {
        statusLine.setText(result);
        //TODO In a list of jobs the proper action
        //TODO is to remove that list entry.
        // Set progress bar to zero
        progressBar.setProgress(0);
        progressValue.setText("0%");
    }

    /**
     * Actual transfer job run
     * @param mode
     * @return
     */
    private String testSFTP(int mode) {
        String PI_IP_ADDRESS="192.168.0.187";
        int PI_FTPS_PORT=22;
        String TESTFILE="video.mp4";
        String result="Ok";
        File videoFile=null;
        long fileSize=0;
            JSch jsch = JSch.getInstance();
            Session session;
            try {
                // Session createSession(String username, String host, int port, SessionConfig config) throws JSchException
                session = jsch.createSession("pi", PI_IP_ADDRESS, PI_FTPS_PORT);
                SessionConfig config = session.getConfig();
                config.setProperty("StrictHostKeyChecking", "no");
                // session.setConfig("StrictHostKeyChecking", "no");
                publishProgress("Connecting..", "0","0%",null);
                session.connect("raxabaxa".getBytes());
                Channel channel = session.openChannel("sftp");
                channel.connect();
                ChannelSftp sftpChannel = (ChannelSftp) channel;

                String dir = sftpChannel.lpwd();
                // sftpChannel.setPtyType("dumb");
                sftpChannel.cd("/media/cheaprace/video");

                // Retrieve file size
                List li=sftpChannel.ls(TESTFILE);
                if (!li.isEmpty()) {
                    fileSize = ((ChannelSftp.LsEntry) li.get(0)).getAttrs().getSize();
                }

                // List files = sftpChannel.ls("/media/cheaprace/video");
                publishProgress("Starts", "0","0%",null);
                if (mode == 1) {
                    // Test of large buffer streaming
                    InputStream is =sftpChannel.get(TESTFILE);
                    videoFile = new File(Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");
                    OutputStream os = new FileOutputStream(videoFile);
                    BufferedOutputStream bos = new BufferedOutputStream(os);
                    int readCount;
                    long total=0;
                    while ((readCount = is.read(buffer)) > 0) {
                        bos.write(buffer, 0, readCount);
                        total=total+readCount;
                        float prog=((100*total)/fileSize);
                        int progressValue=Math.round(prog);
                        publishProgress("Downloading: "+TESTFILE+" "+total+" ("+fileSize+" bytes)",""+progressValue,""+progressValue+"%",null);
                    }
                    is.close();
                    bos.close();
                }
                else {
                    // Use internal implementation
                    sftpChannel.get(TESTFILE, Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");
//              sftpChannel.get("video.mp4",Environment.getExternalStorageDirectory().getAbsolutePath()+"/video.mp4",new ProgressMonitor());
                }
                publishProgress("Done.",null,"100%","");
                sftpChannel.exit();
                session.disconnect();
            }
            catch(InterruptedIOException ie) {
                // Transfer interrupted
                if (isCancelled()) {
                    videoFile.delete();
                    result = "Cancelled";
                    publishProgress(null,null,null,"");
                }
            }
            catch(IOException e){
                    e.printStackTrace();
            } catch ( org.vngx.jsch.exception.JSchException je) {
                je.printStackTrace();
            } catch (org.vngx.jsch.exception.SftpException e) {
                e.printStackTrace();
            }
    return result;
    }


    class ProgressMonitor implements SftpProgressMonitor
    {
        long n=0;
        long total=0;
        public ProgressMonitor() {;}

        public void init(int op, java.lang.String src, java.lang.String dest, long max)
        {
            publishProgress("Started","0","0%",null);
        }

        public boolean count(long bytes)
        {
            n=n + bytes;

            if (n>1000000) {
                total=total+n;
                publishProgress(""+total,null,null,null);
                n=0;
            }
            return(true);
        }

        public void end()
        {
            publishProgress("FINISHED!","100","100%",null);
        }
    }
}

