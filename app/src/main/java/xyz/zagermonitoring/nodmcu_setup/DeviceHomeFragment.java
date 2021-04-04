package xyz.zagermonitoring.nodmcu_setup;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.LinearGradient;
import android.graphics.Paint;
import android.graphics.Shader;
import android.os.Bundle;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.localbroadcastmanager.content.LocalBroadcastManager;
import androidx.navigation.fragment.NavHostFragment;

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
import xyz.zagermonitoring.nodmcu_setup.Items.FirebaseTimeTempUpdate;

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
    private String deviceNickName;
    private Boolean Estopped;
    private Boolean Online;
    private FirebaseFirestore db = FirebaseFirestore.getInstance();
    private FirebaseUser user = FirebaseAuth.getInstance().getCurrentUser();
    private String UID = user.getUid();
    private DevicePage devicePage;
    private BarChart barChart;
    private TextView tvStatus;
    private Button btnEstop;
    private Button btnHome;
    private TextView txtDevice;

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

        // Inflate the layout for this fragment
        return inflater.inflate(R.layout.fragment_device_home, container, false);
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        tvStatus = view.findViewById(R.id.tv_device_status);
        barChart = view.findViewById(R.id.bc_currentTemps_p1);
        btnEstop = view.findViewById(R.id.btn_estop);
        btnHome = view.findViewById(R.id.btn_home);
        txtDevice = view.findViewById(R.id.txtView_deviceHomeHeader);
        devicePage = (DevicePage) getActivity();
        deviceID = devicePage.getDeviceID();
        deviceNickName = devicePage.getDeviceNickName();
        txtDevice.setText(deviceNickName);
        Estopped = devicePage._firebaseData.getEstopped();
        Online = devicePage._firebaseData.getOnline();
        Long latestTime = devicePage._firebaseData.getLatestTime();
        Float latestTempP1 = devicePage._firebaseData.getLatestTempP1();
        Float latestTempP2 = devicePage._firebaseData.getLatestTempP2();
        Float latestTempP3 = devicePage._firebaseData.getLatestTempP3();
        Float latestTempP4 = devicePage._firebaseData.getLatestTempP4();
        setUpChart();
        fillChart(latestTime, latestTempP1, latestTempP2, latestTempP3, latestTempP4);
        String status;
        if (!Online){
            status = "Offline";
            btnEstop.setText("Shut Off Power");
        }else if (Estopped){
            status = "Power Shutdown";
            btnEstop.setText("Restore Power");
        }else{
            status = "Online";
        }
        tvStatus.setText(status);
        BroadcastReceiver br = new BroadcastReceiver(){
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWDATA_POINT".equals(intent.getAction())){
                    FirebaseTimeTempUpdate fttu1 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P1");
                    FirebaseTimeTempUpdate fttu2 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P2");
                    FirebaseTimeTempUpdate fttu3 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P3");
                    FirebaseTimeTempUpdate fttu4 = intent.getParcelableExtra("com.zagermonitoring.FIREBASE_NEWTEMP_POINT_P4");
                    Long time = fttu1.getTime();
                    Float temp1 = fttu1.getTemp();
                    Float temp2 = fttu2.getTemp();
                    Float temp3 = fttu3.getTemp();
                    Float temp4 = fttu4.getTemp();
                    fillChart(time, temp1, temp2, temp3, temp4);
                }
            }
        };
        BroadcastReceiver br1 = new BroadcastReceiver() {
            @Override
            public void onReceive(Context context, Intent intent) {
                if("com.zagermonitoring.FIREBASE_NEWSETTINGS".equals(intent.getAction())){
                    Estopped = intent.getBooleanExtra("com.zagermonitoring.FIREBASE_NEW_E-STOP", false);
                    Online = intent.getBooleanExtra("com.zagermonitoring.FIREBASE_NEW_ONLINE_STATUS", false);
                    String status;
                    if (!Online){
                        status = "Offline";
                        btnEstop.setText("Shut Off Power");
                    }else if (Estopped){
                        status = "Power Shutdown";
                        btnEstop.setText("Restore Power");
                    }else{
                        status = "Online";
                    }
                    tvStatus.setText(status);
                }
            }
        };
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br , new IntentFilter("com.zagermonitoring.FIREBASE_NEWDATA_POINT"));
        LocalBroadcastManager.getInstance(getContext()).registerReceiver(br1 , new IntentFilter("com.zagermonitoring.FIREBASE_NEWSETTINGS"));

        btnEstop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Estopped = !Estopped;
                Map<String, Boolean> EStopField = new HashMap<>();
                EStopField.put("Estop", Estopped);
                db.document("users/" + UID + "/devices/" + deviceID).set(EStopField, SetOptions.merge());
                if (Estopped){
                    Toast.makeText(devicePage, "Power ShutOff Activated", Toast.LENGTH_SHORT).show();
                }else{
                    Toast.makeText(devicePage, "Power ShutOff Deactivated", Toast.LENGTH_SHORT).show();
                }
            }
        });

        btnHome.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(getActivity(),HomePage.class);
                startActivity(intent);
            }
        });
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        super.onViewStateRestored(savedInstanceState);
    }

    @Override
    public void onStart() {
        super.onStart();

    }

    @Override
    public void onResume() {
        super.onResume();
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
        set.setGradientColor(Color.parseColor("#0000FF"), Color.parseColor("#00dadf"));
        //set.setColor(Color.parseColor("#FF00dadf"));
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