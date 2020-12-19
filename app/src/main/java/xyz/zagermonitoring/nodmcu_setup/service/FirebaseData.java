package xyz.zagermonitoring.nodmcu_setup.service;

import android.util.Log;

import androidx.annotation.NonNull;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class FirebaseData {
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


    public void downloadTemps(String deviceID){

        Log.d(TAG, "deviceID" + deviceID);
        db.collection("users/"+UID+"/devices/"+deviceID+"/Temperatures").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
            @Override
            public void onComplete(@NonNull Task<QuerySnapshot> task) {
                if (task.isSuccessful()) {
                    ArrayList<String> docTimesS = new ArrayList<>();
                    ArrayList<Float> docTempsPowers = new ArrayList<>();
                    Map<String, Object> data = new HashMap<>();
                    //get doc date values
                    for (QueryDocumentSnapshot document : task.getResult()) {
                        Map<String, Object> dayValsWrapped = document.getData();
                        try{
                            Map<String, Object> dayVals = (HashMap) dayValsWrapped.get("temps");
                            for(Map.Entry<String, Object> secondValsEntry: dayVals.entrySet()){
                                Map<String, ArrayList<Number>> tempArray = (HashMap) secondValsEntry.getValue();
                                Log.d(TAG, "tempArray" + tempArray.toString());
                                String timeS = secondValsEntry.getKey().replace("T", "");
                                float tempP1 = tempArray.get("Temps").get(0).floatValue();
                                float tempP2 = tempArray.get("Temps").get(1).floatValue();
                                float tempP3 = tempArray.get("Temps").get(2).floatValue();
                                float tempP4 = tempArray.get("Temps").get(3).floatValue();
                                float powerP1 = tempArray.get("Powers").get(0).floatValue();
                                float powerP2 = tempArray.get("Powers").get(1).floatValue();
                                float powerP3 = tempArray.get("Powers").get(2).floatValue();
                                float powerP4 = tempArray.get("Powers").get(3).floatValue();
                                docTempsPowers.add(tempP1);docTempsPowers.add(tempP2);docTempsPowers.add(tempP3);docTempsPowers.add(tempP4);
                                docTempsPowers.add(powerP1);docTempsPowers.add(powerP2);docTempsPowers.add(powerP3);docTempsPowers.add(powerP4);
                                docTimesS.add(timeS);
                                data.put(timeS, docTempsPowers);
                            }
                        }catch (Exception e){
                            Log.e(TAG, e.getMessage());
                        }
                    }
                    // sort and reverse so descending
                    Collections.sort(docTimesS);
                    Collections.reverse(docTimesS);
                    tempsP1.clear(); tempsP2.clear(); tempsP3.clear(); tempsP4.clear();
                    powersP1.clear(); powersP2.clear(); powersP3.clear(); powersP4.clear();

                    // get temps and powers from map and convert time string to long
                    for ( int i = 0; i < docTimesS.size(); i++){
                        ArrayList<Float> tempsPowers = (ArrayList<Float>)data.get(docTimesS.get(i));
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
                    Log.d(TAG, "times " + times.toString());
                } else {
                    Log.d(TAG, "Error getting documents: ", task.getException());
                }
            }
        });
    }

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

    public ArrayList<Long> getTimes() {
        return times;
    }
}
