package xyz.zagermonitoring.nodmcu_setup;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.fragment.NavHostFragment;

import android.security.ConfirmationAlreadyPresentingException;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.navigation.NavigationView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.google.firebase.firestore.QuerySnapshot;
import com.google.firebase.firestore.SetOptions;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.annotation.Nullable;


public class HomePageFragment extends Fragment implements View.OnClickListener {

    private static final String TAG = "HomePage";
    private int LOCATION_PERMISSION_CODE=99;
    private NavController navController;
    private String UID;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private TextView txtView_noDevices;
    private ListView devicelistView;
    private ImageButton setupBtn;
    private ArrayAdapter adapter;
    private ArrayList<String> arrayList = new ArrayList<>();
    private View view;
    private Context context;
    private boolean activeSubscription;
    FirebaseFirestore db = FirebaseFirestore.getInstance();


    private OnFragmentInteractionListener mListener;

    public HomePageFragment() {
        // Required empty public constructor
    }

    @Override
    public void onClick(View v){
        switch(v.getId()){
            case R.id.setupBtn:{
                Log.d("!!!!!!!!!!!!!!!!!!", "Clicked !!!!!!!!!!!!!!11");
                goToListDevices();
                break;
            }
        }
    }



    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home_page, container, false);
        devicelistView = view.findViewById(R.id.deviceListView);
        txtView_noDevices = view.findViewById(R.id.txtView_noDevices);
        setupBtn = view.findViewById(R.id.setupBtn);
        txtView_noDevices.setVisibility(View.INVISIBLE);

        adapter = new ArrayAdapter<>(getActivity(), android.R.layout.simple_list_item_1, arrayList);
        devicelistView.setAdapter(adapter);

        devicelistView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                if(activeSubscription){
                    String Device = devicelistView.getItemAtPosition(position).toString();
                    goToDevicePage(Device);
                }else{
                    navController.navigate(R.id.subscriptionFragment);
                }
            }
        });

        setupBtn.setOnClickListener(this);
        populateDeviceList();



        // Inflate the layout for this fragment
        return view;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        context = getActivity();
        user = FirebaseAuth.getInstance().getCurrentUser();
        UID = user.getUid();
        Log.d("UID", UID);
        navController = Navigation.findNavController(getActivity(), R.id.nav_host_fragment);

        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>() {
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task) {
            if (!task.isSuccessful()) {
                Log.w(TAG, "getInstanceId failed", task.getException());
                return;
            }
            // Get new Instance ID token
            String token = task.getResult().getToken();
            db = FirebaseFirestore.getInstance();
            Map<String, Object> tokenMap = new HashMap<>();
            tokenMap.put("CM_token",token);
            db.collection("users").document(UID).set(tokenMap, SetOptions.merge());

            }
        });

        DocumentReference docRef = db.collection("payments").document(UID);
        docRef.get().addOnCompleteListener(new OnCompleteListener<DocumentSnapshot>() {
            @Override
            public void onComplete(@NonNull Task<DocumentSnapshot> task) {
                if (task.isSuccessful()){
                    DocumentSnapshot document = task.getResult();
                    if(!document.exists()){
                        Log.d(TAG, "document does not exist");
                    }
                    if(document.exists()) {
                        Log.d(TAG, document.getData().toString());
                        if (document.contains("Subscription_Active")) {
                            activeSubscription = document.getBoolean("Subscription_Active");
                            Log.d(TAG, "Subscription_Active found");
                        }
                    }

                }else{
                    Log.d(TAG, "get sub active failed with ", task.getException());
                }

            }

        });

    }

    private void populateDeviceList() {
        String UID = user.getUid();
        db.collection("users").document(UID).collection("devices")
            .addSnapshotListener(new EventListener<QuerySnapshot>() {
                 @Override
                 public void onEvent(@Nullable QuerySnapshot snapshots,
                                     @Nullable FirebaseFirestoreException e) {
                     if (e != null) {
                         Log.w(TAG, "device list collection listener error");
                         return;
                     }
                     arrayList.clear();
                     for (QueryDocumentSnapshot document : snapshots) {
                         arrayList.add(document.getId());
                         adapter.notifyDataSetChanged();
                     }
                     if (arrayList.size() == 0) {
                         Log.d("Setting to Visible","now");
                         txtView_noDevices.setVisibility(View.VISIBLE);

                     } else {
                         txtView_noDevices.setVisibility(View.INVISIBLE);
                     }
                     Log.d(TAG, arrayList.toString());
                 }
             });
    }

    private void goToDevicePage(String Device){
        Bundle bundle = new Bundle();
        bundle.putString("Device", Device);
        Intent intent = new Intent(getActivity(),DevicePage.class);
        intent.putExtras(bundle);
        startActivity(intent);
    }


    private void goToListDevices(){
        Log.d("button clicked","here");
        if (ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            Log.d("does", "not have permission");
            if (ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION)){
                Log.d(" ", "Alert Builder Coming up");

                new AlertDialog.Builder(getActivity())
                .setTitle("Permission needed")
                .setMessage("To set up your new device this app will need wifi configuration permission. Because WiFi " +
                        "can technically tell your rough location, android request that a user gives access to location services." +
                        "If you agree, press ok when location permission is requested")
                .setPositiveButton("ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
                    }
                })
                .setNegativeButton("cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                }).create().show();
            }else{
                requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_CODE);
            }
        }
        if(ContextCompat.checkSelfPermission(getActivity(), Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED){
            Intent intent = new Intent(getActivity(), ChooseDevice.class);
            startActivity(intent);
        }
    }

    private void LogOut(){
        mAuth.signOut();
        Intent intent = new Intent(getActivity(), LoginActivity.class);
        startActivity(intent);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        Log.d("grantResults", grantResults + "");
        if (requestCode == LOCATION_PERMISSION_CODE) {
            if (grantResults.length>0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("onRequestPermissions", "on");
                Intent intent = new Intent(getActivity(), ChooseDevice.class);
                startActivity(intent);
            }
        }
    }

    /**
     * This interface must be implemented by activities that contain this
     * fragment to allow an interaction in this fragment to be communicated
     * to the activity and potentially other fragments contained in that
     * activity.
     * <p>
     * See the Android Training lesson <a href=
     * "http://developer.android.com/training/basics/fragments/communicating.html"
     * >Communicating with Other Fragments</a> for more information.
     */
    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
