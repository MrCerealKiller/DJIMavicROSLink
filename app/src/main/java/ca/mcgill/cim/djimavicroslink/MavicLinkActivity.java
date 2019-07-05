package ca.mcgill.cim.djimavicroslink;

import android.annotation.SuppressLint;
import android.os.AsyncTask;
import android.os.Bundle;
import android.util.Log;
import android.widget.TextView;
import android.widget.Toast;

import org.ros.android.MessageCallable;
import org.ros.android.RosActivity;
import org.ros.node.NodeConfiguration;
import org.ros.node.NodeMainExecutor;

import java.net.URI;
import java.net.URISyntaxException;

import ca.mcgill.cim.djimavicroslink.services.Listener;

public class MavicLinkActivity extends RosActivity {

    private static final String TAG = "MavicLinkActivity";

    private String mRosMasterUriStr;
    private String mLocalAddrStr;

    private URI mRosMasterUri;

    private TextView mMessageView;

    public MavicLinkActivity() {
        super("MavicLinkNode", "MavicLinkNode");
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_mavic_link);

        mMessageView = findViewById(R.id.rosmavic_data_data);
    }

    @Override
    protected void init(NodeMainExecutor nodeMainExecutor) {
        // Setup Node Configuration
        NodeConfiguration nodeConfiguration = NodeConfiguration.newPublic(mLocalAddrStr);
        nodeConfiguration.setMasterUri(mRosMasterUri);
        nodeConfiguration.setNodeName("ListenerTestNode");

        // Create a Listener
        Listener<std_msgs.Float64> listener = new Listener<>("/nums", std_msgs.Float64._TYPE);
        listener.setMessageToStringCallable(new MessageCallable<String, std_msgs.Float64>() {
            @Override
            public String call(std_msgs.Float64 message) {
                Log.d(TAG, "call: " + message.getData());
                setData(String.valueOf(message.getData()));
                return null;
            }
        });

        nodeMainExecutor.execute(listener, nodeConfiguration);
    }

    /**
     * NOTE:
     * The below function is overridden to avoid using the standard master chooser.
     * It is ugly and we only need text input for the IP.
     */
    @SuppressLint("StaticFieldLeak")
    @Override
    public void startMasterChooser() {
        try {
            mRosMasterUriStr = getIntent().getStringExtra("ros_master_uri");
            mLocalAddrStr = getIntent().getStringExtra("local_inet_addr");

            mRosMasterUri = new URI(mRosMasterUriStr);
            nodeMainExecutorService.setMasterUri(mRosMasterUri);
            new AsyncTask<Void, Void, Void>() {
                @Override
                protected Void doInBackground(Void... params) {
                    MavicLinkActivity.this.init(nodeMainExecutorService);
                    return null;
                }
            }.execute();
        } catch (NullPointerException e) {
            toast("Please try a reconnection");
            Log.e(TAG, "startMasterChooser: Could not retrieve ros_master_uri from intent");
            finish();
        } catch (URISyntaxException e) {
            toast("Malformed URI");
            finish();
        }
    }

    protected void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(MavicLinkActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }

    protected void setData(final String data) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                mMessageView.setText(data);
            }
        });
    }
}
