package xyz.zagermonitoring.nodmcu_setup;

import android.graphics.Color;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import com.github.mikephil.charting.charts.LineChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.data.Entry;
import com.github.mikephil.charting.data.LineData;
import com.github.mikephil.charting.data.LineDataSet;
import com.github.mikephil.charting.interfaces.datasets.ILineDataSet;
import com.github.mikephil.charting.listener.ChartTouchListener;
import com.github.mikephil.charting.listener.OnChartGestureListener;
import com.github.mikephil.charting.utils.ViewPortHandler;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;


/**
 * A simple {@link Fragment} subclass.
 * Use the {@link Port1Fragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class Port1Fragment extends Fragment {

    private static final String TAG = "Port1Fragment";
    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private DevicePage devicePage;
    private String deviceID;

    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();
    //private ArrayList<Long> times = new ArrayList<>();
    //private ArrayList<Float> temps = new ArrayList<>();

    private LineDataSet set1;
    private LineData lineData;
    private LineChart chart;
    private ViewPortHandler handler;
    private MyXAxisFormatter myXAxisFormatter = new MyXAxisFormatter();
    private int numOfPoints = 360; // number of dataPoints on chart
    private int dataGranularity = 10; //seconds between data

    private SeekBar seekBar;
    private int chartSelected = 0; // 0 - hour, 1 - day, 2 - month, 3 - year

    public Port1Fragment() {
        // Required empty public constructor
    }

    /**
     * Use this factory method to create a new instance of
     * this fragment using the provided parameters.
     *
     * @param param1 Parameter 1.
     * @param param2 Parameter 2.
     * @return A new instance of fragment DeviceHomeFragment.
     */
    // TODO: Rename and change types and number of parameters
    public static Port1Fragment newInstance(String param1, String param2) {
        Port1Fragment fragment = new Port1Fragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart(){
        super.onStart();
        seekBar = getView().findViewById(R.id.seekBar_port1);
        chart = getView().findViewById(R.id.LineChart_Port1);
        setUpSeek();
        setUpChart();
        ArrayList<Float> temps = devicePage._firebaseData.getTempsP4();
        ArrayList<Long> times = devicePage._firebaseData.getTimes();
        fillChart(temps, times);
        //downloadTemps();
    }

    @Override
    public void onResume() {
        super.onResume();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mParam1 = getArguments().getString(ARG_PARAM1);
            mParam2 = getArguments().getString(ARG_PARAM2);
        }

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_port1, container, false);
    }

//    private void downloadTemps(){
//        Log.d(TAG, "deviceID" + deviceID);
//        db.collection("users/"+UID+"/devices/"+deviceID+"/Temperatures").get().addOnCompleteListener(new OnCompleteListener<QuerySnapshot>() {
//            @Override
//            public void onComplete(@NonNull Task<QuerySnapshot> task) {
//                if (task.isSuccessful()) {
//                    ArrayList<String> docTimesS = new ArrayList<>();
//                    Map<String, Object> data = new HashMap<>();
//                    temps.clear();
//                    times.clear();
//                    //get doc date values
//                    for (QueryDocumentSnapshot document : task.getResult()) {
//                        Map<String, Object> dayVals = document.getData();
//                        //get hour values
//                        for(Map.Entry<String, Object> hourValsEntry : dayVals.entrySet()){
//                            Map<String, Object>  hourVals = (HashMap) hourValsEntry.getValue();
//                            //get second values and temp and power
//                            for(Map.Entry<String, Object> secondValsEntry: hourVals.entrySet()){
//                                Map<String, ArrayList<Number>> tempArray = (HashMap) secondValsEntry.getValue();
//                                String timeS = secondValsEntry.getKey().replace("T", "");
//                                float temp = tempArray.get("Temps").get(3).floatValue();
//                                docTimesS.add(timeS);
//                                data.put(timeS, temp);
//                            }
//                        }
//                    }
//                    // sort and reverse so ascending
//                    Collections.sort(docTimesS);
//                    Collections.reverse(docTimesS);
//                    // get temp from map and convert time string to long
//                    for ( int i = 0; i < docTimesS.size(); i++){
//                        float temp = (float)data.get(docTimesS.get(i));
//                        long time = Long.parseLong(docTimesS.get(i));
//                        temps.add(temp);
//                        times.add(time);
//                    }
//                    Log.d(TAG, "times " + times.toString());
//                    Log.d(TAG, "temps " + temps.toString());
//                    fillChart();
//
//                } else {
//                    Log.d(TAG, "Error getting documents: ", task.getException());
//                }
//            }
//        });
//    }

    private void fillChart(ArrayList<Float> temps, ArrayList<Long> times){
        Log.d(TAG, "temps size: " + temps.size());
        Log.d(TAG, "times size: " + times.size());
        ArrayList<Long> timeSlice = new ArrayList<>();
        ArrayList<Float> displayTemps = new ArrayList<>();
        long startTime = (Calendar.getInstance().getTimeInMillis() / 1000) - 10; //10 sec delay offset
        Log.d(TAG, "start time in secs " + startTime);
        if (numOfPoints > times.size()) numOfPoints = times.size();
        for(int i=0; i<numOfPoints; i++){
            timeSlice.add(startTime - i*dataGranularity);
        }
        Log.d(TAG, "timeSlice " + timeSlice.toString());
        //Log.d(TAG, "timeSlice " + timeSlice.toString());
        //make displayTemps find non congruent temp snap shots (device off) fill with zero

        //TODO
        for(int i=0; i < numOfPoints; i++){
            long timeInterval = timeSlice.get(i);
            int indexI = (i *dataGranularity/10);
            long dataBaseTime;
            if (indexI >= times.size()){
                dataBaseTime = 0;
            }else{
                dataBaseTime = times.get(i * dataGranularity/10);
            }
            if((timeInterval - dataGranularity/2 <= dataBaseTime) && (dataBaseTime < timeInterval + dataGranularity/2)){
                displayTemps.add(temps.get(i*dataGranularity/10));
            } else {
                for (int j=0; j < numOfPoints; j++){
                    int indexJ = (j * dataGranularity/10) - 1 ;
                    if (indexJ >= times.size()){
                        dataBaseTime = 0;
                    }else{
                        dataBaseTime = times.get(j * dataGranularity/10);
                    }
                    if((timeInterval - dataGranularity/2 <= dataBaseTime) && (dataBaseTime < timeInterval + dataGranularity/2)){
                        if(indexI < temps.size()){
                            displayTemps.add(temps.get(i*dataGranularity/10));
                        }
                        break;
                    }
                    if(j == numOfPoints -1){
                        displayTemps.add(0f);
                    }
                }
            }
        }
        Log.d(TAG, "displayTemps size" + displayTemps.size());
        Log.d(TAG, "timeSlice size" + timeSlice.size());
        Log.d(TAG, "displayTemps " + displayTemps.toString());
        ArrayList<Entry> values = new ArrayList<>();
        Collections.reverse(timeSlice);
        Collections.reverse(displayTemps);
        myXAxisFormatter.setInterval(dataGranularity);
        myXAxisFormatter.setStartTime(timeSlice.get(0));
        for(int i=0; i< displayTemps.size(); i++){
            values.add(new Entry(i, displayTemps.get(i)));
        }
        setData(values);
    }

    private void setUpChart(){

        chart.getDescription().setEnabled(false);
        chart.setDrawGridBackground(false);
        chart.setHighlightPerDragEnabled(false);
        chart.setHighlightPerTapEnabled(false);
        chart.setDragEnabled(true);
        chart.setScaleEnabled(true);
        chart.setPinchZoom(true);
        chart.getLegend().setEnabled(false);
        chart.setMaxVisibleValueCount(20);

        // // X-Axis Style // //
        chart.getXAxis().setPosition(XAxis.XAxisPosition.BOTTOM);
        chart.getXAxis().setDrawGridLines(false);
        chart.getXAxis().setTextColor(Color.WHITE);
        chart.getXAxis().setLabelCount(4, true);
        chart.getXAxis().setValueFormatter( myXAxisFormatter );

        // // Y-Axis Style // //
        chart.getAxisLeft().setDrawGridLines(false);
        chart.getAxisLeft().setTextColor(Color.WHITE);
        chart.getAxisLeft().setAxisMinimum(-1F);
        chart.getAxisLeft().setAxisMaximum(120F);

        // disable dual axis (only use LEFT axis)
        chart.getAxisRight().setEnabled(false);
        handler = chart.getViewPortHandler();
        chart.setOnChartGestureListener(new OnChartGestureListener() {
            @Override
            public void onChartGestureStart(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartGestureEnd(MotionEvent me, ChartTouchListener.ChartGesture lastPerformedGesture) {}
            @Override
            public void onChartLongPressed(MotionEvent me) {}
            @Override
            public void onChartDoubleTapped(MotionEvent me) {}
            @Override
            public void onChartSingleTapped(MotionEvent me) {}
            @Override
            public void onChartFling(MotionEvent me1, MotionEvent me2, float velocityX, float velocityY) {}
            @Override
            public void onChartScale(MotionEvent me, float scaleX, float scaleY) {
                //Log.d(TAG, "scaleX: "+ scaleX + " scaleY: " + scaleY);
                float range = chart.getHighestVisibleX() - chart.getLowestVisibleX();
                if (range < 40) {
                    set1.setDrawCircles(true);
                    set1.setDrawCircleHole(true);
                }else{
                    set1.setDrawCircles(false);
                    set1.setDrawCircleHole(false);
                }
            }
            @Override
            public void onChartTranslate(MotionEvent me, float dX, float dY) {}
        });

    }

    private void appendData(Entry value){
        if (chart.getData() != null && chart.getData().getDataSetCount() > 0){
            set1 = (LineDataSet) chart.getData().getDataSetByIndex(0);
            set1.addEntry(value);
            set1.notifyDataSetChanged();
            chart.getData().notifyDataChanged();
            chart.notifyDataSetChanged();
        }
    }

    private void setData(ArrayList<Entry> values){

        set1 = new LineDataSet(values, "DataSet 1");
        set1.setValueTextColor(Color.WHITE);
        set1.setDrawCircles(false);
        set1.setDrawCircleHole(false);
        //set1.setMode(LineDataSet.Mode.CUBIC_BEZIER);
        ArrayList<ILineDataSet> dataSets = new ArrayList<>();
        dataSets.add(set1); // add the data sets
        lineData = new LineData(dataSets);
        chart.clear();
        chart.setData(lineData);
        Log.d(TAG, chart.getData().getDataSets().toString());
        chart.invalidate();

    }

    private void setUpSeek(){
        seekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                // Write code to perform some action when progress is changed.
                chartSelected = progress;
                int hourDataGranularity = 10; //seconds
                int dayDataGranularity = 240; //seconds
                int monthDataGranularity = 7200; //seconds
                int yearDataGranularity = 86400; //seconds
                Log.d(TAG, "progress " + progress);
                switch(progress){
                    case 0:
                        dataGranularity = hourDataGranularity;
                        break;
                    case 1:
                        dataGranularity = dayDataGranularity;
                        break;
                    case 2:
                        dataGranularity = monthDataGranularity;
                        break;
                    case 3:
                        dataGranularity = yearDataGranularity;
                        break;
                }
                Log.d(TAG, "dataGranularity " + dataGranularity);
                ArrayList<Float> temps = devicePage._firebaseData.getTempsP4();
                ArrayList<Long> times = devicePage._firebaseData.getTimes();
                fillChart(temps, times);
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is started.
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                // Write code to perform some action when touch is stopped.
            }

        });
    }
}
