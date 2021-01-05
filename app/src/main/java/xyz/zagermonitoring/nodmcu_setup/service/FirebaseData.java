package xyz.zagermonitoring.nodmcu_setup.service;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.os.Parcelable;
import android.util.Log;

import androidx.annotation.NonNull;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;

import javax.annotation.Nullable;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.PortValues;
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTemp;

import static java.security.AccessController.getContext;

public class FirebaseData implements Serializable {
    private static final String TAG = "FirebaseData";
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();
    private ArrayList<Long> times = new ArrayList<>();
    private ArrayList<Float> tempsP1 = new ArrayList<>();
    private ArrayList<Float> tempsP2 = new ArrayList<>();
    private ArrayList<Float> tempsP3 = new ArrayList<>();
    private ArrayList<Float> tempsP4 = new ArrayList<>();
    private ArrayList<Float> powersP1 = new ArrayList<>();
    private ArrayList<Float> powersP2 = new ArrayList<>();
    private ArrayList<Float> powersP3 = new ArrayList<>();
    private ArrayList<Float> powersP4 = new ArrayList<>();
    private ArrayList<Boolean> _alarmsOn = new ArrayList<>();
    private List<Number> _setTemps = new ArrayList<>();
    private List<Number> _lowAlarms = new ArrayList<>();
    private List<Number> _highAlarms = new ArrayList<>();
    private Boolean _Estopped = false;
    private Context _context;
    private String _deviceID;

    public FirebaseData(Context context, String deviceID){
        _context = context;
        _deviceID = deviceID;
        activateDocListener();
    }

    public void activateDocListener(){
        DocumentReference docRef = db.collection("users").document(UID).collection("devices").document(_deviceID);
        docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
            @Override
            public void onEvent(@Nullable DocumentSnapshot documentSnapshot, @Nullable FirebaseFirestoreException e) {
                List<Number> Temps = new ArrayList<Number>() ;
                List<Number> SetTemps = new ArrayList<Number>();
                List<Number> LowAlarm = new ArrayList<Number>();
                List<Number> HighAlarm = new ArrayList<Number>();
                ArrayList<Boolean> alarmsOn = new ArrayList<>();
                List<Number> setTemps = new ArrayList<>();
                List<Number> lowAlarms = new ArrayList<>();
                List<Number> highAlarms = new ArrayList<>();
                FieldPath setAlmPath = FieldPath.of("setAlarms", "setTemperatures");
                FieldPath lowAlmPath = FieldPath.of("setAlarms", "lowAlarms");
                FieldPath highAlmPath = FieldPath.of("setAlarms", "highAlarms");
                FieldPath EstopPath = FieldPath.of("E-Stop");
                FieldPath p1Path = FieldPath.of("port1On");
                FieldPath p2Path = FieldPath.of("port2On");
                FieldPath p3Path = FieldPath.of("port3On");
                FieldPath p4Path = FieldPath.of("port4On");

                assert documentSnapshot != null;
                if(documentSnapshot.getData() != null){
                    Intent intent = new Intent("com.zagermonitoring.FIREBASE_NEWSETTINGS");
                    try{
                        _Estopped = (Boolean) documentSnapshot.get(EstopPath);
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_E-STOP", _Estopped);
                    }catch (Exception ex){
                        Log.e(TAG, "Error getting Estop field " + ex.getMessage());
                    }
                    try{
                        Temps = (List<Number>) documentSnapshot.get("currentTemps");
                    }catch (Exception ex){
                        Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                    }
                    try{
                        setTemps = (List<Number>) documentSnapshot.get(setAlmPath);
                        lowAlarms = (List<Number>) documentSnapshot.get(lowAlmPath);
                        highAlarms = (List<Number>) documentSnapshot.get(highAlmPath);
                        _setTemps = setTemps;
                        _lowAlarms = lowAlarms;
                        _highAlarms = highAlarms;
                        intent.putExtra("com.zagermonitoring.FIREABSE_NEW_SET_TEMPS", (ArrayList<Number>) setTemps);
                        intent.putExtra("com.zagermonitoring.FIREABSE_NEW_LOW_ALARM", (ArrayList<Number>) lowAlarms);
                        intent.putExtra("com.zagermonitoring.FIREABSE_NEW_HIGH_ALARM", (ArrayList<Number>) highAlarms);

                    }catch(Exception ex){
                        Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                    }
                    try{
                        alarmsOn.add((Boolean) documentSnapshot.get(p1Path));
                        alarmsOn.add((Boolean) documentSnapshot.get(p2Path));
                        alarmsOn.add((Boolean) documentSnapshot.get(p3Path));
                        alarmsOn.add((Boolean) documentSnapshot.get(p4Path));
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_ALARMS_ON", alarmsOn.toArray());
                        _alarmsOn = alarmsOn;
                    }catch(Exception ex){
                        Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
                    }
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                }
            }
        });
    }

    public void downloadTemps(String deviceID) {
        Log.d(TAG, "deviceID" + deviceID);
        db.collection("users/" + UID + "/devices/" + deviceID + "/Temperatures").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@NonNull @Nullable QuerySnapshot value,
                                @Nullable FirebaseFirestoreException e) {
                    ArrayList<String> docTimesS = new ArrayList<>();
                    Map<String, Object> data = new HashMap<>();
                //get doc date values
                for (QueryDocumentSnapshot document : value) {
                    Map<String, Object> dayValsWrapped = document.getData();
                    try {
                        Map<String, Object> dayVals = (HashMap) dayValsWrapped.get("temps");
                        for (Map.Entry<String, Object> secondValsEntry : dayVals.entrySet()) {
                            ArrayList<Float> docTempsPowers = new ArrayList<>();
                            Map<String, ArrayList<Number>> tempArray = (HashMap) secondValsEntry.getValue();
                                //Log.d(TAG, "tempArray" + tempArray.toString());
                                String timeS = secondValsEntry.getKey().replace("T", "");
                                float doctempP1 = tempArray.get("Temps").get(0).floatValue();
                                float doctempP2 = tempArray.get("Temps").get(1).floatValue();
                                float doctempP3 = tempArray.get("Temps").get(2).floatValue();
                                float doctempP4 = tempArray.get("Temps").get(3).floatValue();
                                float docpowerP1 = tempArray.get("Powers").get(0).floatValue();
                                float docpowerP2 = tempArray.get("Powers").get(1).floatValue();
                                float docpowerP3 = tempArray.get("Powers").get(2).floatValue();
                                float docpowerP4 = tempArray.get("Powers").get(3).floatValue();
                                docTempsPowers.add(doctempP1);
                                docTempsPowers.add(doctempP2);
                                docTempsPowers.add(doctempP3);
                                docTempsPowers.add(doctempP4);
                                docTempsPowers.add(docpowerP1);
                                docTempsPowers.add(docpowerP2);
                                docTempsPowers.add(docpowerP3);
                                docTempsPowers.add(docpowerP4);
                                docTimesS.add(timeS);
                                data.put(timeS, docTempsPowers);

                            }
                        } catch (Exception ex) {
                            Log.e(TAG, ex.getMessage());
                        }
                    }
                    // sort and reverse so descending
                    Collections.sort(docTimesS);
                    Collections.reverse(docTimesS);
                    times.clear();
                    tempsP1.clear();
                    tempsP2.clear();
                    tempsP3.clear();
                    tempsP4.clear();
                    powersP1.clear();
                    powersP2.clear();
                    powersP3.clear();
                    powersP4.clear();

                    // get temps and powers from map and convert time string to long
                    for (int i = 0; i < docTimesS.size(); i++) {
                        ArrayList<Float> tempsPowers = (ArrayList<Float>) data.get(docTimesS.get(i));
                        //Log.d(TAG, "tempsPowers "+ tempsPowers.toString());
                        long time = Long.parseLong(docTimesS.get(i));
                        times.add(time);
                        tempsP1.add(tempsPowers.get(0));
                        tempsP2.add(tempsPowers.get(1));
                        tempsP3.add(tempsPowers.get(2));
                        tempsP4.add(tempsPowers.get(3));
                        powersP1.add(tempsPowers.get(4));
                        powersP2.add(tempsPowers.get(5));
                        powersP3.add(tempsPowers.get(6));
                        powersP4.add(tempsPowers.get(7));

                    }
                    //Log.d(TAG, "temps: " + tempsP4);
                    //Log.d(TAG, "times " + times.toString());
                    Intent intent = new Intent("com.zagermonitoring.FIREBASE_NEWDATA");
                    FirebaseTimeTemp ftt1 = new FirebaseTimeTemp(times, tempsP1, powersP1);
                    FirebaseTimeTemp ftt2 = new FirebaseTimeTemp(times, tempsP2, powersP2);
                    FirebaseTimeTemp ftt3 = new FirebaseTimeTemp(times, tempsP3, powersP3);
                    FirebaseTimeTemp ftt4 = new FirebaseTimeTemp(times, tempsP4, powersP4);
                    intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P1", ftt1);
                    intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P2", ftt2);
                    intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P3", ftt3);
                    intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P4", ftt4);
                    LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                }
        });
    }

    public Boolean getEstopped() { return  _Estopped; };

    public ArrayList<Boolean> getPortActive(){
        return _alarmsOn;
    }

    public List<Number> getSetTemps() { return _setTemps; }

    public List<Number> getLowAlarms() { return _lowAlarms; }

    public List<Number> getHighAlarms() { return _highAlarms; }

    public ArrayList<Float> getTempsP1() {
        return tempsP1;
    }

    public ArrayList<Float> getTempsP2() {
        return tempsP2;
    }

    public ArrayList<Float> getTempsP3() {
        return tempsP3;
    }

    public ArrayList<Float> getTempsP4() {
        return tempsP4;
    }

    public ArrayList<Float> getPowersP1() {
        return powersP1;
    }

    public ArrayList<Float> getPowersP2() {
        return powersP2;
    }

    public ArrayList<Float> getPowersP3() {
        return powersP3;
    }

    public ArrayList<Float> getPowersP4() {
        return powersP4;
    }

    public ArrayList<Long> getTimes() {
        return times;
    }
}
