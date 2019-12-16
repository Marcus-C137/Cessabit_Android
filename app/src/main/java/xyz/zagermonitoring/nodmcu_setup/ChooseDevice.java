package xyz.zagermonitoring.nodmcu_setup;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;


import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseDevice extends AppCompatActivity {

    private WifiManager wifiManager;
    private FirebaseAuth mAuth;
    private ListView listView;
    private ArrayList<String> arrayList = new ArrayList<>();
    private TextView Title;
    private ProgressBar mProgressBar;
    private ArrayAdapter adapter;
    private String email;
    private String password;
    private String UID;
    private String Wifi_SSID;
    private Boolean connectedToCessabit = false;
    private Boolean cessabitDeviceFound = false;
    TextView TV_noDevicesFound;
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PASSWORD = "password";

    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            List<ScanResult> results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult : results) {
                Log.d("scan result: ", scanResult.SSID);
                if (scanResult.SSID.equals("Cessabit")) {
                    Log.d("wifiReciever ", "found Cessabit");
                    connectToDevice("Cessabit");
                    cessabitDeviceFound = true;
                }
            }
            updateTitle(cessabitDeviceFound);

            if (arrayList.size()==0){
                TV_noDevicesFound.setVisibility(View.VISIBLE);
            }else{
                TV_noDevicesFound.setVisibility(View.INVISIBLE);
            }
        }
    };

    private BroadcastReceiver connectionChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            WifiInfo connection;
            connection = wifiManager.getConnectionInfo();
            String ConnectedSSID = connection.getSSID();
            if (ConnectedSSID.equals("\"Cessabit\"")){
                connectedToCessabit = true;
                Log.d("Connected to Cessabit", " true");
            }else{
                connectedToCessabit = false;
                Log.d("Connected to Cessabit", " false");
            }
            Log.d("ConnectedSSID ", ConnectedSSID);
            updateTitle(connectedToCessabit);
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_device);
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        password = sharedPreferences.getString(PASSWORD, "");
        email=currentUser.getEmail();
        UID=currentUser.getUid();
        TV_noDevicesFound = findViewById(R.id.TV_noDevicesFound);
        Title = findViewById(R.id.Title);
        mProgressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.deviceList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Wifi_SSID = listView.getItemAtPosition(position).toString();
                //openDialog();

            }
        });
//        Intent intent = new Intent(ChooseDevice.this, ChooseWifi.class);
//        startActivity(intent);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, R.layout.layout_list_item_device, R.id.DeviceTxtView, arrayList);
        listView.setAdapter(adapter);
    }

    private void checkConnection(){

    }

    private void connectToDevice(String SSID) {

        WifiConfiguration conf = new WifiConfiguration();
        conf.SSID = "\"" + SSID + "\"";
        conf.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.NONE);
        int netID = wifiManager.addNetwork(conf);
        wifiManager.disconnect();
        wifiManager.enableNetwork(netID, true);
        wifiManager.reconnect();

    }

    public void updateTitle(Boolean connected){
        if (connected){
            Title.setText("Connected To Cessabit");
        }else{
            Title.setText("Not Connected");
            TV_noDevicesFound.setVisibility(View.VISIBLE);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

//    @Override
//    public void applyTexts(String WIFI_password) {
//
//        RequestQueue queue;
//
//        Map<String, String> params = new HashMap<String, String>();
//        params.put("email", email);
//        params.put("password", password);
//        params.put("UID", UID);
//        params.put("Wifi_SSID",Wifi_SSID);
//        params.put("Wifi_password",WIFI_password);
//
//        String url = "http://192.168.1.4";
//
//
//        JSONObject parameters = new JSONObject(params);
//
//        queue = Volley.newRequestQueue(this);
//        JsonObjectRequest postRequest = new JsonObjectRequest(Request.Method.POST, url, parameters,
//                new Response.Listener<JSONObject>()
//                {
//                    @Override
//                    public void onResponse(JSONObject response) {
//                        // response
//                        Log.d("Response", response.toString());
//                    }
//                },
//                new Response.ErrorListener()
//                {
//                    @Override
//                    public void onErrorResponse(VolleyError error) {
//                        // error
//                        Log.d("Error.Response", error.toString());
//                    }
//                }
//        );
//
//        Volley.newRequestQueue(this).add(postRequest);
//        Intent intent = new Intent(ChooseWifi.this, HomePage.class);
//        startActivity(intent);
//    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            unregisterReceiver(wifiReceiver);
            unregisterReceiver(connectionChange);

        }catch(final Exception exception){
            Log.d("Receiver try catch","cannot unregister receiver");
        }

    }
    @Override
    protected void onStart(){
        super.onStart();
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        registerReceiver(connectionChange, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning for Devices ..", Toast.LENGTH_SHORT).show();

    }


}

