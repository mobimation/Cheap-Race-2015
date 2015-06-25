package tv.laidback.cheaprace2015.teams.member;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.TextView;

import java.util.ArrayList;

import tv.laidback.cheaprace2015.R;
import tv.laidback.cheaprace2015.sync.SyncService;

public class MembersActivity extends Activity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_members);
        Intent intent = getIntent();
        // Set team name header
        final String team=intent.getStringExtra("team");
        final String[] members=intent.getStringArrayExtra("members");
        TextView teamName=(TextView) findViewById(R.id.teamName);
        teamName.setText(team);
        // Add name of team members to list
        ListView memberList = (ListView) findViewById(R.id.teamMembers);
        ArrayList<String> listItems=new ArrayList<String>();
        ArrayAdapter<String> adapter;
        for (int n=0; n<members.length; n++)
            listItems.add(n,members[n]);
        adapter=new ArrayAdapter<String>(this,
                android.R.layout.simple_list_item_1,
                listItems);
        memberList.setAdapter(adapter);
        memberList.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                String member = members[position];
                Log.d(TAG, "Member=" + member + " is " + position);
                // Show photo of selected team member
                Intent i=new Intent(MembersActivity.this, MemberPhoto.class);
                i.putExtra("team", team);
                i.putExtra("member",member);
                getApplicationContext().startActivity(i);
            }
        });
    }
}
