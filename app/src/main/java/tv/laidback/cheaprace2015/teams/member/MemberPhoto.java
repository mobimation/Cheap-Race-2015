package tv.laidback.cheaprace2015.teams.member;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;

import tv.laidback.cheaprace2015.R;

public class MemberPhoto extends Activity {

    private final String TAG = getClass().getSimpleName();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_member_photo);
        TextView teamHeader=(TextView)findViewById(R.id.labelTeamName);
        TextView teamMember=(TextView)findViewById(R.id.memberName);
        Intent intent = getIntent();
        // Set team name header
        final String team=intent.getStringExtra("team");
        teamHeader.setText(team);
        final String member=intent.getStringExtra("member");
        teamMember.setText(member);
        // Set up photo capture button
        // TODO Only the selected team member should be able to change photo
        ImageButton button = (ImageButton)findViewById(R.id.imageButton);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // Launch photo capture activity
                Intent intent = new Intent(MemberPhoto.this, MemberPhotoCapture.class);
                startActivityForResult(intent, 1);
            }
        });
    }
    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        // Check which request we're responding to
        if (requestCode == 1) {
            // Make sure the request was successful
            if (resultCode == RESULT_OK) {
                // The user captured a new photo
                // The Intent's data Uri identifies which contact was selected.
                Uri uri=data.getData();
                // TODO Do something with the contact here
            }
        }
    }
}
