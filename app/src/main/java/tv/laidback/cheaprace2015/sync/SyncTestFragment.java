package tv.laidback.cheaprace2015.sync;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.ProgressBar;
import android.widget.TextView;

import tv.laidback.cheaprace2015.R;
import tv.laidback.cheaprace2015.transfer.AsyncTransferJob;

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
        // Set up so we can run the progress bar
        progress = (ProgressBar)v.findViewById(R.id.progressBar);
        progress.setMax(100);
        // Prepare status message display
        status=(TextView)v.findViewById(R.id.statusLine);
        status.setText("Idle..");
        // ..and the percentage indicator
        progressValue=(TextView)v.findViewById(R.id.statusLine);
        progressValue.setText("0 %");

        defaultButton.setOnClickListener(new View.OnClickListener() {
            public void onClick(View v) {
                new AsyncTransferJob(getActivity().getBaseContext(),"192.168.0.187",progress,status,progressValue).execute();
            }
        });
        return v;
    }
}
