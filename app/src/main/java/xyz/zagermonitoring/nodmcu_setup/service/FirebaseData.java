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
import com.google.firebase.firestore.DocumentChange;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.ListenerRegistration;
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
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTempUpdate;

import static com.google.firebase.firestore.DocumentChange.Type.ADDED;
import static com.google.firebase.firestore.DocumentChange.Type.MODIFIED;
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
    private Boolean _online = false;
    private Boolean _Estopped = false;
    private Context _context;
    private String _deviceID;
    private ListenerRegistration tempListener;
    private ListenerRegistration settingsListener;
    private int lastSize;
    private int modifiedSize;

    public FirebaseData(Context context, String deviceID){
        _context = context;
        _deviceID = deviceID;
        activateDocListener();
    }

    public void activateDocListener(){
        DocumentReference docRef = db.collection("users").document(UID).collection("devices").document(_deviceID);
        settingsListener = docRef.addSnapshotListener(new EventListener<DocumentSnapshot>() {
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
                Boolean online;
                FieldPath onlinePath = FieldPath.of("online");
                FieldPath setAlmPath = FieldPath.of("setAlarms", "setTemperatures");
                FieldPath lowAlmPath = FieldPath.of("setAlarms", "lowAlarms");
                FieldPath highAlmPath = FieldPath.of("setAlarms", "highAlarms");
                FieldPath EstopPath = FieldPath.of("Estop");
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
                        online = (Boolean) documentSnapshot.get(onlinePath);
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_ONLINE_STATUS", online);
                        _online = online;
                    }catch(Exception ex){
                        Log.e(TAG, Objects.requireNonNull(ex.getMessage()));
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
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_SET_TEMPS", (ArrayList<Number>) setTemps);
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_LOW_ALARM", (ArrayList<Number>) lowAlarms);
                        intent.putExtra("com.zagermonitoring.FIREBASE_NEW_HIGH_ALARM", (ArrayList<Number>) highAlarms);

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
        DocumentReference docRef1 = db.collection("heartbeat").document(UID).collection("heartbeats").document(_deviceID);
    }

    public void downloadTemps(String deviceID) {
        Log.d(TAG, "deviceID" + deviceID);
        tempListener = db.collection("users/" + UID + "/devices/" + deviceID + "/Temperatures").addSnapshotListener(new EventListener<QuerySnapshot>() {
            @Override
            public void onEvent(@NonNull @Nullable QuerySnapshot value, @Nullable FirebaseFirestoreException e) {

                if (e != null) {
                    System.err.println("Listen failed: " + e);
                    return;
                }
                ArrayList<String> docTimesS = new ArrayList<>();
                Map<String, Object> data = new HashMap<>();
                Boolean added = false;
                Boolean modified = false;
                //get doc date values
                for (DocumentChange dc: value.getDocumentChanges()) {

                    switch(dc.getType()){
                        case ADDED:
                            lastSize = ((HashMap)dc.getDocument().getData().get("temps")).size();
                            Log.d(TAG, "case ADDED size: " + ((HashMap)dc.getDocument().getData().get("temps")).size());
                            try {
                                Map<String, Object> dayVals = (HashMap)dc.getDocument().getData().get("temps");
                                for (Map.Entry<String, Object> secondValsEntry : dayVals.entrySet()) {
                                    ArrayList<Float> docTempsPowers = new ArrayList<>();
                                    Map<String, ArrayList<Number>> tempArray = (HashMap) secondValsEntry.getValue();
                                    //Log.d(TAG, "tempArray" + tempArray.toString());
                                    String timeKey = secondValsEntry.getKey();
                                    String timeS = timeKey.replace("T", "");
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
                                added = true;
                            } catch (Exception ex) {
                                Log.e(TAG, ex.getMessage());
                            }
                            break;
                        case MODIFIED:
                            modifiedSize = ((HashMap)dc.getDocument().getData().get("temps")).size();
                            Log.d(TAG, "case MODIFIED size: " + ((HashMap)dc.getDocument().getData().get("temps")).size());
                            Map<String, Object> dataPointsMap = (HashMap)dc.getDocument().getData().get("temps");
                            List<String> timesS = new ArrayList<>();
                            for (Map.Entry<String, Object> secondValsEntry : dataPointsMap.entrySet()) {
                                String timeS = secondValsEntry.getKey();
                                timesS.add(timeS);
                            }
                            Collections.sort(timesS);
                            for (int i = modifiedSize-lastSize; i > 0; i--){
                                String latestTime = timesS.get(timesS.size() - (i));
                                Map<String, ArrayList<Number>> docTempsPowers = (HashMap) dataPointsMap.get(latestTime);
                                ArrayList<Number> temps = docTempsPowers.get("Temps");
                                ArrayList<Number> powers = docTempsPowers.get("Powers");
                                latestTime = latestTime.replace("T", "");
                                Log.d(TAG, "latestTime: " + latestTime);

                                Long time = Long.parseLong(latestTime);
                                times.add(0, time);
                                tempsP1.add(0, temps.get(0).floatValue());
                                tempsP2.add(0 ,temps.get(1).floatValue());
                                tempsP3.add(0, temps.get(2).floatValue());
                                tempsP4.add(0, temps.get(3).floatValue());
                                powersP1.add(0, temps.get(0).floatValue());
                                powersP2.add(0, temps.get(1).floatValue());
                                powersP3.add(0, temps.get(2).floatValue());
                                powersP4.add(0, temps.get(3).floatValue());
                                Intent intent = new Intent("com.zagermonitoring.FIREBASE_NEWDATA_POINT");
                                FirebaseTimeTempUpdate fttu1 = new FirebaseTimeTempUpdate(time, temps.get(0).floatValue(), powers.get(0).floatValue());
                                FirebaseTimeTempUpdate fttu2 = new FirebaseTimeTempUpdate(time, temps.get(1).floatValue(), powers.get(1).floatValue());
                                FirebaseTimeTempUpdate fttu3 = new FirebaseTimeTempUpdate(time, temps.get(2).floatValue(), powers.get(2).floatValue());
                                FirebaseTimeTempUpdate fttu4 = new FirebaseTimeTempUpdate(time, temps.get(3).floatValue(), powers.get(3).floatValue());
                                intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P1", fttu1);
                                intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P2", fttu2);
                                intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P3", fttu3);
                                intent.putExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P4", fttu4);
                                LocalBroadcastManager.getInstance(_context).sendBroadcast(intent);
                                modified = true;
                            }
                            lastSize = modifiedSize;
                            break;
                        default:
                            break;

                    }

                }

                if(added){
                    // sort and reverse so descending
                    Log.d(TAG, "added");
                    Collections.sort(docTimesS);
                    Collections.reverse(docTimesS);
                    Log.d(TAG, "LatestTime: " + docTimesS.get(0));
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

                if(modified){

                }
            }
        });
    }

    public void releaseListeners(){
        Log.d(TAG, "Killing Firebase Listeners");
        tempListener.remove();
        settingsListener.remove();
    }


    public Boolean getOnline() {return _online;};

    public Boolean getEstopped() { return  _Estopped; };

    public ArrayList<Boolean> getPortActive(){ return _alarmsOn; }

    public List<Number> getSetTemps() { return _setTemps; }

    public List<Number> getLowAlarms() { return _lowAlarms; }

    public List<Number> getHighAlarms() { return _highAlarms; }

    public ArrayList<Float> getTemps(int portIndex){
        switch (portIndex){
            case 0:
                return tempsP1;
            case 1:
                return tempsP2;
            case 2:
                return tempsP3;
            case 3:
                return tempsP4;
            default:
                ArrayList<Float> zeros = new ArrayList<Float>();
                zeros.add(0f);
                Log.e(TAG, "DID NOT GIVE PORT INDEX BETWEEN 0-3");
                return zeros;
        }
    }

    public ArrayList<Float> getPowers(int portIndex){
        switch (portIndex){
            case 0:
                return powersP1;
            case 1:
                return powersP2;
            case 2:
                return powersP3;
            case 3:
                return powersP4;
            default:
                ArrayList<Float> zeros = new ArrayList<Float>();
                zeros.add(0f);
                Log.e(TAG, "DID NOT GIVE PORT INDEX BETWEEN 0-3");
                return zeros;
        }
    }

    public ArrayList<Long> getTimes() { return times; }

    public Long getLatestTime() {if (times.size() > 0){return times.get(0);}else {return 0L;}}

    public Float getLatestTempP1() {if (tempsP1.size() > 0){return tempsP1.get(0);}else{return 0f;}}
    public Float getLatestTempP2() {if (tempsP2.size() > 0){return tempsP2.get(0);}else{return 0f;}}
    public Float getLatestTempP3() {if (tempsP3.size() > 0){return tempsP3.get(0);}else{return 0f;}}
    public Float getLatestTempP4() {if (tempsP4.size() > 0){return tempsP4.get(0);}else{return 0f;}}
    public Float getLatestPowerP1() {if (powersP1.size() > 0){return powersP1.get(0);}else{return 0f;}}
    public Float getLatestPowerP2() {if (powersP2.size() > 0){return powersP2.get(0);}else{return 0f;}}
    public Float getLatestPowerP3() {if (powersP3.size() > 0){return powersP3.get(0);}else{return 0f;}}
    public Float getLatestPowerP4() {if (powersP4.size() > 0){return powersP4.get(0);}else{return 0f;}}
}
