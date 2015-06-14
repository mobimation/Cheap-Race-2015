package tv.laidback.cheaprace2015;

import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.os.Handler;
import android.widget.ProgressBar;
import android.widget.TextView;

import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;


/**
 * A simple {@link Fragment} subclass.
 */
public class SyncTestFragment extends Fragment {
    private static final String TAG = SyncTestFragment.class.getSimpleName();
    ProgressBar progress=null;
    TextView status=null;
    TextView progressValue=null;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    static String ARG_SECTION_NUMBER = "section_number";
    SyncTestFragment parent=null;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SyncTestFragment newInstance(int sectionNumber) {
        SyncTestFragment fragment = new SyncTestFragment();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SyncTestFragment() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_synctest, container, false);
        // Connect a button that launch the transfer test as a thread
        Button defaultButton=(Button)v.findViewById(R.id.buttonSyncTestStart);
        parent=this;
        defaultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                Handler syncHandler = new Handler();
                syncHandler.post(retrieveJob);
            }
        });
        // Set up so we can run the progress bar
        progress = (ProgressBar)v.findViewById(R.id.progressBar);
        progress.setMax(100);
        // Prepare status message display
        status=(TextView)v.findViewById(R.id.statusLine);
        status.setText("Idle..");
        // ..and the percentage indicator
        progressValue=(TextView)v.findViewById(R.id.statusLine);
        progressValue.setText("0 %");
        return v;
    }

    final Runnable retrieveJob = new Runnable() {
        @Override
        public void run() {
            Log.d(TAG, "Transfer job begins");
            testSFTP();
        }
    };

    private void testSFTP() {
        String PI_IP_ADDRESS="192.168.0.187";
        int PI_FTPS_PORT=22;
        FTPSClient ftps;
        ftps = new FTPSClient(true);
        try {
            ftps.setFileType(FTP.BINARY_FILE_TYPE);
            ftps.setTrustManager(null);
            ftps.connect(PI_IP_ADDRESS,PI_FTPS_PORT);
            int reply = ftps.getReplyCode();
            if (!FTPReply.isPositiveCompletion(reply))
            {
                ftps.disconnect();
                Log.d(TAG,"FTP server refused connection.");
            }
            else {
                // Retrieve a video file over Wifi from Cheap Race 2015 Sync Server Hub
                String filepath= Environment.getExternalStorageDirectory().getAbsolutePath();
                OutputStream os = new FileOutputStream(filepath+"/localvideo.mp4");
                // Navigate to repository
                ftps.cwd("/home/pi/media/cheaprace/video");
                // Retrieve sample video file
                ftps.retrieveFile("Новости - YouTube.MP4", os);
                // TODO Continue here
                Log.d(TAG, "File transferred.");
                parent.getActivity().runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                      progress.setProgress(99);
                    }
                });
            }
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

    }
}
