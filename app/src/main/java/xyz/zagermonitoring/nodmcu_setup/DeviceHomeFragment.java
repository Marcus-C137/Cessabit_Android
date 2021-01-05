package xyz.zagermonitoring.nodmcu_setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.github.mikephil.charting.animation.Easing;
import com.github.mikephil.charting.charts.BarChart;
import com.github.mikephil.charting.components.XAxis;
import com.github.mikephil.charting.components.YAxis;
import com.github.mikephil.charting.data.BarData;
import com.github.mikephil.charting.data.BarDataSet;
import com.github.mikephil.charting.data.BarEntry;
import com.github.mikephil.charting.interfaces.datasets.IBarDataSet;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.SetOptions;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTemp;

/**
 * A simple {@link Fragment} subclass.
 * Use the {@link DeviceHomeFragment#newInstance} factory method to
 * create an instance of this fragment.
 */
public class DeviceHomeFragment extends Fragment {

    private static final String TAG = "DeviceHomeFragment";

    // TODO: Rename parameter arguments, choose names that match
    // the fragment initialization parameters, e.g. ARG_ITEM_NUMBER
    private static final String ARG_PARAM1 = "param1";
    private static final String ARG_PARAM2 = "param2";

    // TODO: Rename and change types of parameters
    private String mParam1;
    private String mParam2;

    private String deviceID;
    private Boolean Estopped;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();
    private DevicePage devicePage;
    private BarChart barChart;
    private TextView tvStatus;
    private Button btnEstop;

    public DeviceHomeFragment() {
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
    public static DeviceHomeFragment newInstance(String param1, String param2) {
        DeviceHomeFragment fragment = new DeviceHomeFragment();
        Bundle args = new Bundle();
        args.putString(ARG_PARAM1, param1);
        args.putString(ARG_PARAM2, param2);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onStart() {
        super.onStart();
        tvStatus = getView().findViewById(R.id.tv_device_status);
        barChart = getView().findViewById(R.id.bc_currentTemps_p1);
        btnEstop = getView().findViewById(R.id.btn_estop);
        Estopped = devicePage._firebaseData.getEstopped();
        ArrayList<Long> times = devicePage._firebaseData.getTimes();
        ArrayList<Float> tempsP1 = devicePage._firebaseData.getTempsP1();
        ArrayList<Float> tempsP2 = devicePage._firebaseData.getTempsP2();
        ArrayList<Float> tempsP3 = devicePage._firebaseData.getTempsP3();
        ArrayList<Float> tempsP4 = devicePage._firebaseData.getTempsP4();
        Long latestTime;
        Float latestTempP1, latestTempP2, latestTempP3, latestTempP4;
        if (times.size() > 0){
            latestTime = times.get(0);
            latestTempP1 = tempsP1.get(0);
            latestTempP2 = tempsP2.get(0);
            latestTempP3 = tempsP3.get(0);
            latestTempP4 = tempsP4.get(0);
        }else{
            latestTime = 0L;
            latestTempP1 = 0f;
            latestTempP2 = 0f;
            latestTempP3 = 0f;
            latestTempP4 = 0f;
        }
        setUpChart();
        fillChart(latestTime, latestTempP1, latestTempP2, latestTempP3, latestTempP4);
        String status;
        if (Estopped){
            status = "Stopped";
        }else{
            status = "Online";
        }
        tvStatus.setText(status);
        BroadcastReceiver br = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWDATA".equals(intent.getAction())){
                    FirebaseTimeTemp ftt1 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P1");
                    FirebaseTimeTemp ftt2 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P2");
                    FirebaseTimeTemp ftt3 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P3");
                    FirebaseTimeTemp ftt4 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_P4");
                    Long time = ftt1.getTimes().get(0);
                    Float temp1 = ftt1.getTemps().get(0);
                    Float temp2 = ftt2.getTemps().get(0);
                    Float temp3 = ftt3.getTemps().get(0);
                    Float temp4 = ftt4.getTemps().get(0);
                    fillChart(time, temp1, temp2, temp3, temp4);
                }
            }
        };
        BroadcastReceiver br1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWSETTINGS".equals(intent.getAction())){
                    Estopped = intent.getBooleanExtra("com.zagermonitoring.FIREBASE_NEW_E-STOP", false);
                    String status;
                    if (Estopped){
                        status = "Stopped";
                    }else{
                        status = "Online";
                    }
                    tvStatus.setText(status);
                }
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br , new IntentFilter("com.zagermonitoring.FIREBASE_NEWDATA"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br1 , new IntentFilter("com.zagermonitoring.FIREBASE_NEWSETTINGS"));
        btnEstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estopped = !Estopped;
                Map<String, Boolean> EStopField = new HashMap<>();
                EStopField.put("E-Stop", Estopped);
                db.document("users/" + UID + "/devices/" + deviceID).set(EStopField, SetOptions.merge());
                if (Estopped){
                    Toast.makeText(devicePage, "Power ShutOff Activated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(devicePage, "Power ShutOff Deactivated", Toast.LENGTH_SHORT).show();
                }
            }
        });
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
        OnBackPressedCallback callback = new OnBackPressedCallback(true) {
            @Override
            public void handleOnBackPressed() {
                Intent intent = new Intent(getActivity(),HomePage.class);
                startActivity(intent);
            }
        };
        requireActivity().getOnBackPressedDispatcher().addCallback(this, callback);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_home, container, false);
    }

    private void fillChart(Long time, Float p1Temp, Float p2Temp, Float p3Temp, Float p4Temp){
        if(p1Temp < 0) p1Temp = 0f;
        if(p2Temp < 0) p2Temp = 0f;
        if(p3Temp < 0) p3Temp = 0f;
        if(p4Temp < 0) p4Temp = 0f;

        Calendar cal = Calendar.getInstance();
        long curTime = cal.getTimeInMillis() / 1000;
        Log.d(TAG, "Current Time "  + curTime);
        if (curTime - 60 > time){
            p1Temp = 0f; p2Temp = 0f; p3Temp = 0f; p4Temp = 0f;
        }
        ArrayList<BarEntry> values = new ArrayList<>();
        values.add(new BarEntry(1f, p1Temp));
        values.add(new BarEntry(2f, p2Temp));
        values.add(new BarEntry(3f, p3Temp));
        values.add(new BarEntry(4f, p4Temp));

        BarDataSet set;
        set = new BarDataSet(values, "");
        set.setDrawIcons(false);
        set.setColor(Color.argb(255, 30, 120, 255));
        set.setValueTextColor(Color.WHITE);
        ArrayList<IBarDataSet> dataSets = new ArrayList<>();
        dataSets.add(set);
        BarData data = new BarData(dataSets);
        data.setValueTextSize(10f);
        data.setBarWidth(0.9f);
        barChart.setData(data);
        barChart.invalidate();
    }

    private void setUpChart(){
        barChart.setDrawValueAboveBar(true);
        barChart.setPinchZoom(false);
        barChart.setTouchEnabled(false);
        barChart.setHighlightFullBarEnabled(false);
        barChart.setDrawGridBackground(false);
        barChart.getDescription().setEnabled(false);
        barChart.getLegend().setEnabled(false);
        barChart.animateY(3000, Easing.EaseInBack);
        //X axis
        XAxis xAxis = barChart.getXAxis();
        xAxis.setPosition(XAxis.XAxisPosition.BOTTOM);
        xAxis.setDrawGridLines(false);
        xAxis.setTextColor(Color.WHITE);
        xAxis.setGranularity(1f); // only intervals of 1 day
        xAxis.setLabelCount(4);

        //Y axis
        YAxis leftAxis = barChart.getAxisLeft();
        leftAxis.setDrawGridLines(false);
        leftAxis.setTextColor(Color.WHITE);
        leftAxis.setLabelCount(8, false);
        leftAxis.setSpaceTop(15f);
        leftAxis.setAxisMinimum(0f);
        leftAxis.setAxisMaximum(120f);

        barChart.getAxisRight().setEnabled(false);
    }

}