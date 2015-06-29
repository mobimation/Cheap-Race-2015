package tv.laidback.cheaprace2015.sync;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import tv.laidback.cheaprace2015.R;
import tv.laidback.cheaprace2015.transfer.AsyncDownloadJob;
/*
import org.apache.commons.net.ftp.FTP;
import org.apache.commons.net.ftp.FTPReply;
import org.apache.commons.net.ftp.FTPSClient;
*/


/**
 * A simple {@link Fragment} subclass.
 */
public class SyncTestFragment2 extends Fragment {
    private static final String TAG = SyncTestFragment2.class.getSimpleName();
    ProgressBar progress=null;
    TextView status=null;
    TextView progressValue=null;
    ImageView cancelButton=null;
    AsyncTask transferJob=null;
    /**
     * The fragment argument representing the section number for this
     * fragment.
     */
    static String ARG_SECTION_NUMBER = "section_number";
    SyncTestFragment2 parent=null;
    /**
     * Returns a new instance of this fragment for the given section
     * number.
     */
    public static SyncTestFragment2 newInstance(int sectionNumber) {
        SyncTestFragment2 fragment = new SyncTestFragment2();
        Bundle args = new Bundle();
        args.putInt(ARG_SECTION_NUMBER, sectionNumber);
        fragment.setArguments(args);
        return fragment;
    }

    public SyncTestFragment2() {
        // Required empty public constructor
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v=inflater.inflate(R.layout.fragment_synctest2, container, false);
        // Connect a button that launch the transfer test as a thread
        Button defaultButton=(Button)v.findViewById(R.id.buttonSyncTestStart);
        parent=this;
        // Set up so we can run the progress bar
        progress = (ProgressBar)v.findViewById(R.id.progressBar);
        progress.setMax(100);
        progress.setProgress(0);
        // Prepare status message display
        status=(TextView)v.findViewById(R.id.statusLine);
        status.setText("Idle..");
        // ..and the percentage indicator
        progressValue=(TextView)v.findViewById(R.id.percentage);
        progressValue.setText("0 %");
        // ..and the Cancel button
        cancelButton=(ImageView)v.findViewById(R.id.buttonFetchCancel);
        cancelButton.setVisibility(View.INVISIBLE);
        cancelButton.setOnClickListener(
                new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        if (transferJob != null) {
                            cancelButton.setVisibility(View.INVISIBLE);
                            transferJob.cancel(true);
                            //      progressValue.setText("0%");
                            //      progress.setProgress(0);
                        }
                    }
                }
        );

        defaultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                if ((transferJob==null) ||
                    (!transferJob.getStatus().equals(AsyncTask.Status.RUNNING))) {
                        cancelButton.setVisibility(View.VISIBLE);
                        transferJob = new AsyncDownloadJob("192.168.0.187", progress, status, progressValue, cancelButton).execute();
                    }
                    else
                        Log.d(TAG, "Limited to one job");
            }
        });
        return v;
    }
}
