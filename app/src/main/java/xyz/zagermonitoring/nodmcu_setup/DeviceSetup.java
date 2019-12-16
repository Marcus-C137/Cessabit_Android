package xyz.zagermonitoring.nodmcu_setup;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.HashMap;
import java.util.Map;

public class DeviceSetup extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private String email;
    private String UID;
    private String DeviceName;
    private WifiManager wifiManager;
    private WifiInfo connection;
    private RequestQueue queue;
    private StringRequest stringRequest;
    private String SSID;
    private String Password;
    private String url="http://192.168.1.4";
    EditText password;
    Button Connect;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_setup);

        Connect = findViewById(R.id.ConnectBtn);
        password = findViewById(R.id.passwordText);

        Bundle bundle = getIntent().getExtras();
        if(bundle != null){
            connectToDevice(bundle);
            Connect.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    sendRequestAndPrintResponse();
                }
            });
        }


    }

    private void connectToDevice(Bundle bundle){

        DeviceName = bundle.getString("DeviceName");
        Log.d("DEVICE SETUP BUNDLE","IT IS " + DeviceName);
        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\""+DeviceName+"\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        wifiManager = (WifiManager)getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        int netID = wifiManager.addNetwork(conf);
        wifiManager.enableNetwork(netID,true);
        connection = wifiManager.getConnectionInfo();
        SSID = connection.getSSID();
        Log.d("SSID : ", SSID);


    }

    private void sendRequestAndPrintResponse() {

        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        email=currentUser.getEmail();
        UID=currentUser.getUid();

        Map<String, String>  params = new HashMap<String, String>();
        params.put("email", email);
        params.put("UID", UID);
        params.put("SSID", SSID);
        params.put("password",password.getText().toString());

        JSONObject parameters = new JSONObject(params);

        queue = Volley.newRequestQueue(this);
        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, parameters,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d("Response", response.toString());
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d("Error.Response", error.toString());
                    }
                }
        );

        Volley.newRequestQueue(this).add(postRequest);
    }

}
