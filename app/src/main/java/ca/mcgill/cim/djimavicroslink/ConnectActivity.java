package ca.mcgill.cim.djimavicroslink;

import androidx.appcompat.app.AppCompatActivity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import org.ros.address.InetAddressFactory;
import org.ros.internal.node.client.MasterClient;
import org.ros.internal.node.xmlrpc.XmlRpcTimeoutException;
import org.ros.namespace.GraphName;

import java.net.URI;
import java.net.URISyntaxException;


public class ConnectActivity extends AppCompatActivity {

    private static final String TAG = "ConnectActivity";

    private MasterClient mMaster = null;
    private String mRosMasterUri = null;
    private String mLocalAddress = null;

    private static final String CONNECTION_EXCEPTION_TEXT = "ECONNREFUSED";
    private static final String UNKNOWN_HOST_TEXT = "UnknownHost";

    private static final String GRAPH_TAG = "android/main_activity";

    private EditText mURIView;
    private Button   mConnectButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_connect);

        mURIView = findViewById(R.id.rosconnect_text);
        mConnectButton = findViewById(R.id.rosconnect_button);
        mConnectButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptConnection();
            }
        });
    }

    @SuppressLint("StaticFieldLeak")
    private void attemptConnection() {
        final String uri = mURIView.getText().toString();

        if (TextUtils.isEmpty(uri)) {
            mURIView.setError(getString(R.string.rosconnect_no_uri_warning));
            mURIView.requestFocus();

            Log.w(TAG, "attemptConnection: No URI was given");

            return;
        }

        // TODO PERFORM VALIDATION ON URI HERE

        mURIView.setEnabled(false);
        mConnectButton.setEnabled(false);

        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected void onPreExecute() {
                // TODO Show Loader Here
            }
            @Override
            protected Boolean doInBackground(Void... params) {
                try {
                    mMaster = new MasterClient(new URI(uri));
                    mRosMasterUri = mMaster.getUri(GraphName.of(GRAPH_TAG)).getResult().toString();
                    mLocalAddress = InetAddressFactory.newNonLoopback().getHostAddress();
                    toast("Connected");
                    return true;
                } catch (URISyntaxException e) {
                    toast("Invalid URI");
                    Log.e(TAG, "doInBackground: " + e.getMessage());
                    return false;
                } catch (XmlRpcTimeoutException e) {
                    toast("Master unreachable!");
                    Log.e(TAG, "doInBackground: " + e.getMessage());
                    return false;
                } catch (Exception e) {
                    String exceptionMessage = e.getMessage();
                    if (exceptionMessage.contains(CONNECTION_EXCEPTION_TEXT)) {
                        toast("Unable to communicate with master!");
                    } else if (exceptionMessage.contains(UNKNOWN_HOST_TEXT)) {
                        toast("Unable to resolve URI hostname!");
                    } else {
                        toast("Unknown Error. Check Logger");
                        Log.e(TAG, "doInBackground: " + e.getMessage());
                    }
                    return false;
                }
            }

            @Override
            protected void onPostExecute(Boolean result) {
                // TODO Hide loader here
                if (result) {
                    Intent sendConnection = new Intent();
                    sendConnection.putExtra("ros_master_uri", mRosMasterUri);
                    sendConnection.putExtra("local_inet_addr", mLocalAddress);
                    setResult(RESULT_OK, sendConnection);
                    finish();
                } else {
                    mConnectButton.setEnabled(true);
                    mURIView.setEnabled(true);
                }
            }
        }.execute();
    }

    protected void toast(final String text) {
        runOnUiThread(new Runnable() {
            @Override
            public void run() {
                Toast.makeText(ConnectActivity.this, text, Toast.LENGTH_LONG).show();
            }
        });
    }
}
