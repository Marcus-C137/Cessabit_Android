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

public class DevicePage extends AppCompatActivity implements SetTempDialog.setTempsDialogListener, AdapterView.OnItemClickListener {
    private boolean setUP = false;
    public String deviceID;
    private String UID;
    private List<List<Number>> tempsDoc;
    private List<Number> currentTemps;
    private List<Boolean> alarmsDoc;
    private List<PortValues> portValues;
    private PortValues portBuff;
    private TempsExpandableListAdapter adapter;
    public BottomNavigationView bottomNavigationView;
    public NavigationView navigationView;
    public NavController navController;
    public NavHostFragment navHostFragment;
    public FirebaseData _firebaseData;
    FirebaseAuth mAuth;
    FirebaseFirestore database;
    DocumentReference docRef;

    private static final String TAG = "DevicePages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        deviceID = getIntent().getExtras().getString("Device");
        _firebaseData = new FirebaseData();
        _firebaseData.downloadTemps(deviceID);
        setContentView(R.layout.activity_device_page);
        bottomNavigationView = findViewById(R.id.bottomNavigationView);
        bottomNavigationView.setSelectedItemId(R.id.deviceHomeBottomNav);
        navHostFragment = (NavHostFragment) this.getSupportFragmentManager().findFragmentById(R.id.nav_device_container);
        navController = navHostFragment.getNavController();
        mAuth = FirebaseAuth.getInstance();
        UID = mAuth.getCurrentUser().getUid();
        database = FirebaseFirestore.getInstance();
        docRef = database.collection("users").document(UID).collection("devices").document(deviceID);
        // Read from the database
        tempsDoc = new ArrayList<>();
        currentTemps = new ArrayList<>();
        alarmsDoc = new ArrayList<>();
        portValues = new ArrayList<>();

        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<Number> Temps = new ArrayList<Number>() ;
                List<Number> SetTemps = new ArrayList<Number>();
                List<Number> LowAlarm = new ArrayList<Number>();
                List<Number> HighAlarm = new ArrayList<Number>();
                List<Boolean> AlarmsOn = new ArrayList<Boolean>(4);
                FieldPath setAlmPath = FieldPath.of("setAlarms", "setTemperatures");
                FieldPath lowAlmPath = FieldPath.of("setAlarms", "lowAlarms");
                FieldPath highAlmPath = FieldPath.of("setAlarms", "highAlarms");
                FieldPath p1Path = FieldPath.of("alarms", "port1Monitoring");
                FieldPath p2Path = FieldPath.of("alarms", "port2Monitoring");
                FieldPath p3Path = FieldPath.of("alarms", "port3Monitoring");
                FieldPath p4Path = FieldPath.of("alarms", "port4Monitoring");

                assert documentSnapshot != null;
                if(documentSnapshot.getData() != null){
                    SetTemps = (List<Number>) documentSnapshot.get(setAlmPath);
                    LowAlarm = (List<Number>) documentSnapshot.get(lowAlmPath);
                    HighAlarm = (List<Number>) documentSnapshot.get(highAlmPath);
                    AlarmsOn.add((Boolean) documentSnapshot.get(p1Path));
                    AlarmsOn.add((Boolean) documentSnapshot.get(p2Path));
                    AlarmsOn.add((Boolean) documentSnapshot.get(p3Path));
                    AlarmsOn.add((Boolean) documentSnapshot.get(p4Path));
                }
                currentTemps = new ArrayList<>();
                tempsDoc = new ArrayList<>();
                alarmsDoc = new ArrayList<>();
                currentTemps = Temps;
                tempsDoc.add(SetTemps);
                tempsDoc.add(LowAlarm);
                tempsDoc.add(HighAlarm);
                alarmsDoc = AlarmsOn;
                portValues.clear();
                for(int i=0; i< tempsDoc.size()+1; i++){
                    Log.d("adding tempsDoc", tempsDoc.toString());
                    Log.d("adding Alarms", alarmsDoc.toString());
                    portBuff = new PortValues(tempsDoc.get(0).get(i), tempsDoc.get(1).get(i), tempsDoc.get(2).get(i), alarmsDoc.get(i));
                    portValues.add(portBuff);
                }

                Log.d(TAG,"current Temps: "+ currentTemps.toString());
                Log.d(TAG,"set Temps: "+ tempsDoc.toString());

//                adapter.setVals(currentTemps,portValues);
//                adapter.notifyDataSetChanged();
            }


        });

//        ExpandableListView expandableListView = findViewById(R.id.ELV_ports);
//        expandableListView.setDividerHeight(1);
//        adapter = new TempsExpandableListAdapter(currentTemps,portValues);
//        expandableListView.setAdapter(adapter);
//        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
//            @Override
//            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
//                Log.d("ChildPosition", childPosition + "");
//                if (childPosition <= 2){
//                    Log.d(TAG, "Open Dialog");
//                    List<String> titles = new ArrayList<>();
//                    titles.add("Set Temperature");
//                    titles.add("Low Alarm");
//                    titles.add("High Alarm");
//                    SetTempDialog setTempDialog = new SetTempDialog();
//                    setTempDialog.setVals(titles.get(childPosition), groupPosition, childPosition);
//                    setTempDialog.show(getSupportFragmentManager(), "setTempDialog");
//                }else{
//                    List<Boolean> newAlarms = new ArrayList<>();
//                    Map<String, Object> newVals = new HashMap<>();
//                    portValues.get(groupPosition).setAlarmOn(!portValues.get(groupPosition).getAlarmOn());
//                    Log.d("alarmsDoc size", alarmsDoc.size() + "");
//                    for(int i =0; i< alarmsDoc.size(); i++){
//                        newAlarms.add(portValues.get(i).getAlarmOn());
//                    }
//                    newVals.put("alarmsMonitored", newAlarms);
//                    Log.d("alarmsMonitored", newVals.toString());
//                    database.collection("users").document(UID).collection("devices").document(deviceID)
//                            .set(newVals, SetOptions.merge());
//                }
//                return true;
//            }
//        });

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

    public String getDeviceID(){return deviceID;}


    @Override
    public void applyTexts(String setTemp, Integer group, Integer child) {
        tempsDoc.get(child).set(group, (Number) Double.parseDouble(setTemp));
        List<Number> newVal = tempsDoc.get(child);
        List<String> titles = new ArrayList<>();
        titles.add("setTemps");
        titles.add("lowAlarms");
        titles.add("highAlarms");
        String setVal = titles.get(child);
        Map<String, Object> tempField = new HashMap<>();
        tempField.put(setVal, newVal);
        tempField.put("iChangedTemps",true);
        database.collection("users").document(UID).collection("devices").document(deviceID)
                .set(tempField, SetOptions.merge());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id){
        long viewId = view.getId();

        if(viewId == R.id.alarmSwitch){
         Log.d(TAG, "position" + position);

        }
    }
}
