package xyz.zagermonitoring.nodmcu_setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;
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

public class ChooseWifi extends AppCompatActivity implements EnterWifiDialog.EnterWifiListener {
    private WifiManager wifiManager;
    private ListView listView;
    private FirebaseAuth mAuth;
    private ArrayList<String> arrayList = new ArrayList<>();
    private ArrayAdapter adapter;
    private String email;
    private String UID;
    private String Wifi_SSID;

    public static final String SHARED_PREFS = "sharedPrefs";
    public static final String PASSWORD = "password";

    private String password;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_choose_wifi);
        FirebaseUser currentUser = mAuth.getInstance().getCurrentUser();
        email=currentUser.getEmail();
        UID=currentUser.getUid();
        listView = findViewById(R.id.deviceList);
        listView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Wifi_SSID = listView.getItemAtPosition(position).toString();
                openDialog();

            }
        });
        SharedPreferences sharedPreferences = getSharedPreferences(SHARED_PREFS, MODE_PRIVATE);
        password = sharedPreferences.getString(PASSWORD, "");

        wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);

        if(!wifiManager.isWifiEnabled()){
            Toast.makeText(this,"Wifi is disabled ... You need to enable it", Toast.LENGTH_LONG).show();
            wifiManager.setWifiEnabled(true);
        }

        adapter = new ArrayAdapter<>(this, R.layout.layout_list_item_wifi,R.id.DeviceTxtView, arrayList);
        listView.setAdapter(adapter);
        scanWifi();

    }


    private void scanWifi(){
        arrayList.clear();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
        wifiManager.startScan();
        Toast.makeText(this, "Scanning Wifi ..", Toast.LENGTH_SHORT).show();
    }

    BroadcastReceiver wifiReceiver = new BroadcastReceiver() {
        List<ScanResult> results;
        @Override
        public void onReceive(Context context, Intent intent) {
            results = wifiManager.getScanResults();
            unregisterReceiver(this);

            for (ScanResult scanResult: results){
                Log.d("Available Wifi: ", scanResult.SSID);
                if (!scanResult.SSID.startsWith("Cessabit"));
                arrayList.add(scanResult.SSID);
                adapter.notifyDataSetChanged();
            }

        }
    };

    private void openDialog(){
        EnterWifiDialog enterWifiDialog = new EnterWifiDialog();
        enterWifiDialog.show(getSupportFragmentManager(),"Enter wifi");
    }

    @Override
    public void applyTexts(String WIFI_password) {

        RequestQueue queue;

        Map<String, String> params = new HashMap<String, String>();
        params.put("email", email);
        params.put("password", password);
        params.put("UID", UID);
        params.put("Wifi_SSID",Wifi_SSID);
        params.put("Wifi_password",WIFI_password);

        String url = "http://192.168.1.4";


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
        Intent intent = new Intent(ChooseWifi.this, HomePage.class);
        startActivity(intent);
    }
}




