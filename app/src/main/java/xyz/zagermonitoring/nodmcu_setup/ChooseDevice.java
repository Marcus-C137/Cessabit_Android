package xyz.zagermonitoring.nodmcu_setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.NetworkInfo;
import android.net.NetworkRequest;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiInfo;
import android.net.wifi.WifiManager;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
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
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ChooseDevice extends AppCompatActivity implements EnterWifiDialog.EnterWifiListener {

    private static final String TAG = ChooseDevice.class.getSimpleName();
    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PASSWORD = "password";

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;
    private FirebaseAuth mAuth;
    private ListView listView;
    private ArrayList<String> wifiList = new ArrayList<>();
    private TextView Title;
    private ProgressBar mProgressBar;
    private ArrayAdapter adapter;
    private String email;
    private String password;
    private String UID;
    private String Wifi_SSID;
    private Boolean connectedToCessabit = false;
    private Boolean cessabitDeviceFound = false;
    private Boolean sentGetRequest = false;
    private Integer connectionCounter = 0;
    private TextView TV_chooseWIFI;


    private BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG, "In wifiReciever");
            List<ScanResult> results = wifiManager.getScanResults();
            unregisterReceiver(this);
            if (isConnectedToCessabit()){
                Log.d(TAG, "Already connected to Cessabit");
                getCessabitSeenWiFi();
                updateTitle(true);
                return;
            }
            Log.d(TAG, "NOT CONNECTED TO CESSABIT SEARCHING");

            for (ScanResult scanResult : results) {
                if (scanResult.SSID.equals("Cessabit")) {
                    Log.d(TAG, "FOUND CESSABIT");
                    connectToDevice("Cessabit");
                    cessabitDeviceFound = true;
                }
            }
        }
    };

    private BroadcastReceiver connectionChange = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();
            Log.d(TAG, action);
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            Log.d(TAG, info.toString());
            Log.d(TAG, "CONNECTION COUNTER " + connectionCounter.toString());
            if(info.getDetailedState().toString().equals("CONNECTED") && wifiManager.getConnectionInfo().getSSID().equals("\"Cessabit\"") && connectionCounter == 1){
                updateTitle(true);
                Log.d(TAG, "CONNECTED TO CESSABIT");
                getCessabitSeenWiFi();
                connectionCounter = 0;
            }else if (info.getDetailedState().toString().equals("CONNECTED") && wifiManager.getConnectionInfo().getSSID().equals("\"Cessabit\"")){
                connectionCounter++;
            }
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
        TV_chooseWIFI= findViewById(R.id.TV_chooseWIFI);
        Title = findViewById(R.id.Title);
        mProgressBar = findViewById(R.id.progressBar);
        listView = findViewById(R.id.deviceList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Wifi_SSID = listView.getItemAtPosition(position).toString();
                openDialog();

            }
        });

        mProgressBar.setVisibility(View.VISIBLE);

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) getApplicationContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        if (!wifiManager.isWifiEnabled()) {
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, R.layout.layout_list_item_device, R.id.DeviceTxtView, wifiList);
        listView.setAdapter(adapter);
        Title.setText("Searching");

        ConnectivityManager connectivityManager = (ConnectivityManager) getSystemService(getApplicationContext().CONNECTIVITY_SERVICE);
        NetworkRequest.Builder builder = new NetworkRequest.Builder();
        connectivityManager.registerNetworkCallback(
                builder.build(),
                new ConnectivityManager.NetworkCallback() {
                    /**
                     * @param network
                     */
                    @Override
                    public void onAvailable(Network network) {

                        if (isConnectedToCessabit()){
                            getCessabitSeenWiFi();
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateTitle( true);
                                }
                            });
                        }

                    }

                    /**
                     * @param network
                     */
                    @Override
                    public void onLost(Network network) {

                        //idk

                    }
                }

        );

    }

    private void openDialog(){
        EnterWifiDialog enterWifiDialog = new EnterWifiDialog();
        enterWifiDialog.show(getSupportFragmentManager(),"Enter wifi");
    }

    @Override
    public void applyTexts(String WIFI_password) {

        String url = "http://192.168.4.1/postWiFiPassword";
        final String Wifi_password = WIFI_password;

        StringRequest postRequest = new StringRequest(Request.Method.POST, url,
                new Response.Listener<String>()
                {
                    @Override
                    public void onResponse(String response) {
                        // response
                        Log.d(TAG, response);
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d(TAG, error.toString());
                    }
                })
        {
            @Override
            protected Map<String,String> getParams()
            {
                Map<String,String> params = new HashMap<String,String>();
                params.put("email", email);
                params.put("password", password);
                params.put("UID", UID);
                params.put("Wifi_SSID",Wifi_SSID);
                params.put("Wifi_password",Wifi_password);
                return params;
            }
        };
        //Log.d(TAG, "Sending " + postRequest.);
        Volley.newRequestQueue(this).add(postRequest);
        Intent intent = new Intent(ChooseDevice.this, HomePage.class);
        startActivity(intent);
    }


    private void connectToDevice(String SSID) {
        Log.d(TAG, "CONNECTING TO CESSABIT");
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
            Title.setText("Connected To Cessabit Device");
            TV_chooseWIFI.setVisibility(View.VISIBLE);
        }else{
            Title.setText("Not Devices found");
            TV_chooseWIFI.setVisibility(View.INVISIBLE);
        }
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private boolean isConnectedToCessabit(){
        WifiInfo connection;
        connection = wifiManager.getConnectionInfo();
        String ConnectedSSID = connection.getSSID();
        if (ConnectedSSID.equals("\"Cessabit\"")){
            connectedToCessabit = true;
            Log.d(TAG, " CONNECTED TO CESSABIT");
        }else{
            connectedToCessabit = false;
            Log.d(TAG, " NOT CONNECTED TO CESSABIT");
        }
        Log.d(TAG, ConnectedSSID);
        return connectedToCessabit;
    }


    public void getCessabitSeenWiFi() {

        Log.d(TAG, "getting Cessabit Seen WiFis");

        String url = "http://192.168.4.1/getKnownWiFi";

        JsonObjectRequest getRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                new Response.Listener<JSONObject>()
                {
                    @Override
                    public void onResponse(JSONObject response) {
                        // response
                        Log.d(TAG, "RESPONSE RECIEVED");

                        try{
                            JSONArray WiFis = response.getJSONArray("WiFis");
                            for(int i = 0, count = WiFis.length(); i< count; i++)
                            {
                                try {
                                    String WifiSSID = WiFis.getString(i);
                                    wifiList.add(WifiSSID);
                                }
                                catch (JSONException e) {
                                    e.printStackTrace();
                                }
                            }
                            adapter.notifyDataSetChanged();
                            Log.d(TAG, wifiList.toString());

                        } catch (JSONException e){
                            e.printStackTrace();
                        };
                    }
                },
                new Response.ErrorListener()
                {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        // error
                        Log.d(TAG, "HTTP RESPONSE ERROR");
                        Log.d(TAG, error.toString());
                    }
                }
        );
        Log.d(TAG, "SENDING HTTP REQUEST");
        Volley.newRequestQueue(this).add(getRequest);
    }

    @Override
    protected void onStop(){
        super.onStop();
        try{
            unregisterReceiver(wifiReceiver);
            unregisterReceiver(connectionChange);

        }catch(final Exception exception){
            Log.d(TAG,"cannot unregister receiver");
        }

    }
    @Override
    protected void onStart(){
        Log.d(ChooseDevice.class.getSimpleName(), "&&&&&&&&&&&&&&&&&&&&&&&&");
        super.onStart();
        wifiList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        //registerReceiver(connectionChange, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning for Devices ..", Toast.LENGTH_SHORT).show();

    }


}

