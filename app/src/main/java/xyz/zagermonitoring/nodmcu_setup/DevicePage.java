package xyz.zagermonitoring.nodmcu_setup;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ExpandableListView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.PortValues;

public class DevicePage extends AppCompatActivity implements SetTempDialog.setTempsDialogListener, AdapterView.OnItemClickListener {
    private boolean setUP = false;
    private String deviceID;
    private String UID;
    private List<List<Number>> tempsDoc;
    private List<Number> currentTemps;
    private List<Boolean> alarmsDoc;
    private List<PortValues> portValues;
    private PortValues portBuff;
    private TempsExpandableListAdapter adapter;
    FirebaseAuth mAuth;
    FirebaseFirestore database;
    DocumentReference docRef;

    private static final String TAG = "DevicePages";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_device_page);

        deviceID = getIntent().getExtras().getString("Device");
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
                List<Number> Temps = (List<Number>) documentSnapshot.get("currentTemps");
                List<Number> SetTemps = (List<Number>) documentSnapshot.get("setTemps");
                List<Number> LowAlarm = (List<Number>) documentSnapshot.get("lowAlarms");
                List<Number> HighAlarm = (List<Number>) documentSnapshot.get("highAlarms");
                List<Boolean> AlarmsOn = (List<Boolean>) documentSnapshot.get("alarmsMonitored");

                currentTemps = new ArrayList<>();
                currentTemps = Temps;
                tempsDoc = new ArrayList<>();
                tempsDoc.add(SetTemps);
                tempsDoc.add(LowAlarm);
                tempsDoc.add(HighAlarm);
                alarmsDoc = new ArrayList<>();
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

                adapter.setVals(currentTemps,portValues);
                adapter.notifyDataSetChanged();
            }
        });

        ExpandableListView expandableListView = findViewById(R.id.ELV_ports);
        expandableListView.setDividerHeight(1);
        adapter = new TempsExpandableListAdapter(currentTemps,portValues);
        expandableListView.setAdapter(adapter);
        expandableListView.setOnChildClickListener(new ExpandableListView.OnChildClickListener() {
            @Override
            public boolean onChildClick(ExpandableListView parent, View v, int groupPosition, int childPosition, long id) {
                Log.d("ChildPosition", childPosition + "");
                if (childPosition <= 2){
                    Log.d(TAG, "Open Dialog");
                    List<String> titles = new ArrayList<>();
                    titles.add("Set Temperature");
                    titles.add("Low Alarm");
                    titles.add("High Alarm");
                    SetTempDialog setTempDialog = new SetTempDialog();
                    setTempDialog.setVals(titles.get(childPosition), groupPosition, childPosition);
                    setTempDialog.show(getSupportFragmentManager(), "setTempDialog");
                }else{
                    List<Boolean> newAlarms = new ArrayList<>();
                    Map<String, Object> newVals = new HashMap<>();
                    portValues.get(groupPosition).setAlarmOn(!portValues.get(groupPosition).getAlarmOn());
                    Log.d("alarmsDoc size", alarmsDoc.size() + "");
                    for(int i =0; i< alarmsDoc.size(); i++){
                        newAlarms.add(portValues.get(i).getAlarmOn());
                    }
                    newVals.put("alarmsMonitored", newAlarms);
                    Log.d("alarmsMonitored", newVals.toString());
                    database.collection("users").document(UID).collection("devices").document(deviceID)
                            .set(newVals, SetOptions.merge());
                }
                return true;
            }
        });

    }

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
