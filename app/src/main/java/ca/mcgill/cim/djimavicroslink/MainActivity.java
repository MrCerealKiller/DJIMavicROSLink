package ca.mcgill.cim.djimavicroslink;

import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;


public class MainActivity extends AppCompatActivity {

    private static final String TAG = "MainActivity";

    private static final int CONNECT_INTENT_REQ = 1;
    private static final int NODE_START_INTENT_REQ = 2;

    String mRosMasterUri;
    String mLocalAddr;

    TextView mMasterUriView;
    TextView mLocalAddrView;

    Button mNodeStartButton;
    Button mRemasterButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mMasterUriView = findViewById(R.id.rosmain_master_data);
        mLocalAddrView = findViewById(R.id.rosmain_local_data);

        mNodeStartButton = findViewById(R.id.rosmain_start_node_button);
        mNodeStartButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startNodeActivity();
            }
        });

        mRemasterButton = findViewById(R.id.rosmain_remaster_button);
        mRemasterButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startConnectionActivity();
            }
        });

        if (mRosMasterUri == null || mLocalAddr == null) {
            startConnectionActivity();
        } else {
            update();
        }
    }

    private void startConnectionActivity() {
        Intent connectionIntent = new Intent(this, ConnectActivity.class);
        startActivityForResult(connectionIntent, CONNECT_INTENT_REQ);
    }

    private void startNodeActivity() {
        if (mRosMasterUri == null || mRosMasterUri.trim().equals("") ||
                mLocalAddr == null || mLocalAddr.trim().equals("")) {
            toast("Unable to Start Node. Please reset connection.");
            Log.w(TAG, "startNodeActivity: mRosMasterUri or mLocalAddr was empty");
            return;
        }

        Intent rosNodeActivityIntent = new Intent(this, MavicLinkActivity.class);
        rosNodeActivityIntent.putExtra("ros_master_uri", mRosMasterUri);
        rosNodeActivityIntent.putExtra("local_inet_addr", mLocalAddr);
        startActivityForResult(rosNodeActivityIntent, NODE_START_INTENT_REQ);
    }

    private void update() {
        if (mRosMasterUri == null || mLocalAddr == null) {
            Log.e(TAG, "update: Attempted update without Master URI or Local IP");
            return;
        }
        mMasterUriView.setText(mRosMasterUri);
        mLocalAddrView.setText(mLocalAddr);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(requestCode == CONNECT_INTENT_REQ && resultCode == RESULT_OK) {
            Bundle connectResult = data.getExtras();
            if (connectResult != null) {
                mRosMasterUri = connectResult.getString("ros_master_uri");
                mLocalAddr = connectResult.getString("local_inet_addr");
                update();
            }
        }
    }

    protected void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MainActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
