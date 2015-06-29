package tv.laidback.cheaprace2015.transfer;

import android.os.AsyncTask;
import android.os.Environment;
import android.util.Log;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.vngx.jsch.Channel;
import org.vngx.jsch.ChannelSftp;
import org.vngx.jsch.JSch;
import org.vngx.jsch.Session;
import org.vngx.jsch.SftpProgressMonitor;
import org.vngx.jsch.config.SessionConfig;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InterruptedIOException;
import java.io.OutputStream;
import java.util.List;

/**
 *
 * Created by gunnarforsgren on 2015-06-15.
 *
 * Uploads a file to a Secure FTP server in an asynchronous way.
 */
public class AsyncUploadJob extends AsyncTask<View, String, String> {

    private static final String TAG = AsyncUploadJob.class.getSimpleName();
    private String      ftpServer;
    private ProgressBar progressBar;
    private TextView    statusLine;
    private TextView    progressValue;
    private final byte[] buffer = new byte[16384];

    public AsyncUploadJob(final String server, View... ui) {
        ftpServer = server;
        progressBar=(ProgressBar)ui[0];
        statusLine=(TextView)ui[1];
        progressValue=(TextView)ui[2];
    }

    @Override
    protected String doInBackground(View... params) {
        Log.d(TAG, "doInBackground");
        String ret=testSFTP(1);
        return ret;
    }

    @Override
    protected void onProgressUpdate(String... status) {
        if (status[0]!=null)
          statusLine.setText(status[0]);
        if (status[1]!=null)
            progressBar.setProgress(new Integer(status[1]).intValue());
        if (status[2]!=null)
            progressValue.setText(status[2]);

    }

    @Override
    protected void onCancelled(String result) {
        statusLine.setText(result);
        //TODO In a list of jobs the proper action
        //TODO is to remove that list entry.
        // Set progress bar to zero
        progressBar.setProgress(0);
        progressValue.setText("0%");
    }

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
                publishProgress("Connecting..", "0","0%");
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
                publishProgress("Starts", "0","0%");
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
                        publishProgress("Downloading: "+TESTFILE+" "+total+" ("+fileSize+" bytes)",""+progressValue,""+progressValue+"%");
                    }
                    is.close();
                    bos.close();
                }
                else {
                    // Use internal implementation
                    sftpChannel.get(TESTFILE, Environment.getExternalStorageDirectory().getAbsolutePath() + "/video.mp4");
//              sftpChannel.get("video.mp4",Environment.getExternalStorageDirectory().getAbsolutePath()+"/video.mp4",new ProgressMonitor());
                }
                publishProgress("Done.",null,"100%");
                sftpChannel.exit();
                session.disconnect();
            }
            catch(InterruptedIOException ie) {
                // Transfer interrupted
                if (isCancelled()) {
                    videoFile.delete();
                    result = "Cancelled";
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

        public void init(int op, String src, String dest, long max)
        {
            publishProgress("Started","0","0%");
        }

        public boolean count(long bytes)
        {
            n=n + bytes;

            if (n>1000000) {
                total=total+n;
                publishProgress(""+total,null,null);
                n=0;
            }
            return(true);
        }

        public void end()
        {
            publishProgress("FINISHED!","100","100%");
        }
    }
}

