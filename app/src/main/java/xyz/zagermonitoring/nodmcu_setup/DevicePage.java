package xyz.zagermonitoring.nodmcu_setup;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.navigation.NavController;
import androidx.navigation.fragment.NavHostFragment;
import androidx.navigation.ui.NavigationUI;

import android.os.Bundle;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.PortValues;
import xyz.zagermonitoring.nodmcu_setup.service.FirebaseData;

public class DevicePage extends AppCompatActivity {
    public String deviceID;
    public String deviceNickName;
    public BottomNavigationView bottomNavigationView;
    public NavController navController;
    public NavHostFragment navHostFragment;
    public FirebaseData _firebaseData;

    private static final String TAG = "DevicePages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceID = getIntent().getExtras().getString("Device");
        deviceNickName = getIntent().getExtras().getString("NickName");
        _firebaseData = new FirebaseData(getApplicationContext(), deviceID);
        _firebaseData.downloadTemps(deviceID);
        setContentView(R.layout.activity_device_page);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.deviceHomeBottomNav);
        navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_device_container);
        navController = navHostFragment.getNavController();

        bottomNavigationView.setOnNavigationItemSelectedListener( new BottomNavigationView.OnNavigationItemSelectedListener() {
            @Override
            public boolean onNavigationItemSelected(@NonNull MenuItem item) {
                Log.d(TAG, "BottomNavigationView Clicked");
                Log.d(TAG, "" + item.getItemId());
                switch(item.getItemId()){
                    case R.id.port1BottomNav:
                        Log.d(TAG, "port1BottomNavClicked");
                        navController.navigate(R.id.port1Fragment);
                        return true;
                    case R.id.port2BottomNav:
                        Log.d(TAG, "port2BottomNavClicked");
                        navController.navigate(R.id.port2Fragment);
                        return true;
                    case R.id.deviceHomeBottomNav:
                        Log.d(TAG, "deviceHomeBottomNavClicked");
                        navController.navigate(R.id.deviceHomeFragment);
                        return true;
                    case R.id.port3BottomNav:
                        Log.d(TAG, "port3BottomNavClicked");
                        navController.navigate(R.id.port3Fragment);
                        return true;
                    case R.id.port4BottomNav:
                        Log.d(TAG, "port4BottomNavClicked");
                        navController.navigate(R.id.port4Fragment);
                        return true;
                }

                return true;
            }
        });

    }

    @Override
    protected void onStop() {
        super.onStop();
        Log.d(TAG,"STOPPED");
        _firebaseData.releaseListeners();

    }


    public String getDeviceID(){return deviceID;}

    public String getDeviceNickName(){return deviceNickName;}

    public NavController getNavController(){ return navController; }

}
