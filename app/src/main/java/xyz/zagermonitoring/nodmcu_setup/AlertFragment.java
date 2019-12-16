package xyz.zagermonitoring.nodmcu_setup;

import android.app.Dialog;
import android.content.Context;
import android.net.Uri;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.EventListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.FirebaseFirestoreException;
import com.google.firebase.functions.FirebaseFunctions;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.annotation.Nullable;

import xyz.zagermonitoring.nodmcu_setup.Adapters.AlertEmailAdapter;
import xyz.zagermonitoring.nodmcu_setup.Adapters.AlertPhoneNumAdapter;
import xyz.zagermonitoring.nodmcu_setup.Items.AlertItem;


public class AlertFragment extends Fragment {

    private OnFragmentInteractionListener mListener;
    private FirebaseAuth mAuth;
    private FirebaseUser user;
    private FirebaseFirestore db;
    private FirebaseFunctions mFunctions;
    private Dialog alertDialog;
    private Dialog alertAddEmail;
    private Dialog alertAddPhnNum;
    private Button alertAddPhoneNumberBtn;
    private Button alertAddEmailBtn;
    private ArrayList<String> emailList;
    private ArrayList<String> phnNumList;
    private ArrayList<AlertItem> emailItems;
    private ArrayList<AlertItem> phnNumItems;
    private RecyclerView emailRV;
    private RecyclerView phnNumRV;
    private AlertEmailAdapter emailAdapter;
    private AlertPhoneNumAdapter phnNumAdapter;
    private RecyclerView.LayoutManager emailLayoutManager;
    private RecyclerView.LayoutManager phnNumLayoutManager;
    private static final String TAG = "AlertFragment";

    public AlertFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        user = mAuth.getCurrentUser();
        db = FirebaseFirestore.getInstance();
        mFunctions = FirebaseFunctions.getInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_alert, container, false);

        alertDialog = new Dialog(getContext());
        alertAddEmail = new Dialog(getContext());
        alertAddPhnNum = new Dialog(getContext());

        alertAddPhoneNumberBtn = view.findViewById(R.id.alertAddPhoneNumberBtn);
        alertAddEmailBtn= view.findViewById(R.id.alertAddEmailBtn);
        emailRV = view.findViewById(R.id.alertEmailsRV);
        phnNumRV = view.findViewById(R.id.alertPhoneNumbersRV);

        emailList = new ArrayList<>();
        phnNumList = new ArrayList<>();
        emailItems = new ArrayList<>();
        phnNumItems = new ArrayList<>();

        emailLayoutManager = new LinearLayoutManager(getContext());
        emailAdapter = new AlertEmailAdapter(emailItems);
        emailRV.setLayoutManager(emailLayoutManager);
        emailRV.setAdapter(emailAdapter);
        phnNumLayoutManager = new LinearLayoutManager(getContext());
        phnNumAdapter = new AlertPhoneNumAdapter(phnNumItems);
        phnNumRV.setLayoutManager(phnNumLayoutManager);
        phnNumRV.setAdapter(phnNumAdapter);

        emailAdapter.setOnItemClickListener(new AlertEmailAdapter.OnAlertEmailClickListener(){
            @Override
            public void onAlertEmailClick(int position){
                ShowPopupEmail(emailList.get(position));
            }
        });

        phnNumAdapter.setOnClickListener(new AlertPhoneNumAdapter.OnAlertPhoneNumClickListener() {
            @Override
            public void onAlertPhoneNumClick(int position) {
                ShowPopupText(phnNumList.get(position));
            }
        });

        alertAddEmailBtn.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view){
                ShowAddEmail(view);
            }
        });
        alertAddPhoneNumberBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) { ShowAddPhoneNumber(view);
            }
        });

        String UID = user.getUid();
        db.collection("alerts").document(UID)
                .addSnapshotListener(new EventListener<DocumentSnapshot>() {
                    @Override
                    public void onEvent(@Nullable DocumentSnapshot document, @Nullable FirebaseFirestoreException e) {
                        if(document.contains("Emails")) {
                            ArrayList<String> arrayListbuffer = (ArrayList<String>) document.get("Emails");
                            emailList.clear();
                            emailItems.clear();
                            for (String email : arrayListbuffer) {
                                emailList.add(email);
                                emailItems.add(new AlertItem(R.drawable.ic_email_black_24dp, email));
                                Log.d(TAG, "emailList " + email);
                                emailAdapter.notifyDataSetChanged();
                            }
                        }

                        if(document.contains("TextNums")) {
                            ArrayList<String> arrayListbuffer = (ArrayList<String>) document.get("TextNums");
                            phnNumList.clear();
                            phnNumItems.clear();
                            for (String phoneNum : arrayListbuffer) {
                                phnNumList.add(phoneNum);
                                phnNumItems.add(new AlertItem(R.drawable.ic_sms_black_24dp, phoneNum));
                                Log.d(TAG, "TextNums " + phoneNum);
                                phnNumAdapter.notifyDataSetChanged();
                            }
                        }
                    }
                });
        return view;
    }

    private void ShowPopupEmail(final String Email){
        TextView alertInfo;
        Button testAlert;
        Button deleteInfo;
        Button cancel;

        alertDialog.setContentView(R.layout.layout_alert);
        alertInfo = alertDialog.findViewById(R.id.alertInfo);
        testAlert = alertDialog.findViewById(R.id.alertDialogTestBtn);
        deleteInfo = alertDialog.findViewById(R.id.alertDialogDeleteBtn);
        cancel = alertDialog.findViewById(R.id.alertDialogCancelBtn);

        alertInfo.setText(Email);

        testAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> data = new HashMap<>();
                data.put("type", "Email");
                data.put("email", Email);
                Log.d("Calling", "Function");
                mFunctions.getHttpsCallable("testAlarms").call(data);
            }
        });

        deleteInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = emailList.indexOf(Email);
                emailList.remove(Email);
                emailItems.remove(pos);
                db.collection("alerts").document(user.getUid()).update("Emails", emailList);
                emailAdapter.notifyDataSetChanged();
                alertDialog.dismiss();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }


    private void ShowPopupText(final String phoneNumber){
        TextView alertInfo;
        Button testAlert;
        Button deleteInfo;
        Button cancel;

        alertDialog.setContentView(R.layout.layout_alert);
        alertInfo = alertDialog.findViewById(R.id.alertInfo);
        testAlert = alertDialog.findViewById(R.id.alertDialogTestBtn);
        deleteInfo = alertDialog.findViewById(R.id.alertDialogDeleteBtn);
        cancel = alertDialog.findViewById(R.id.alertDialogCancelBtn);

        alertInfo.setText(phoneNumber);

        testAlert.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Map<String, Object> data = new HashMap<>();
                data.put("type", "Text");
                data.put("number", phoneNumber);
                mFunctions.getHttpsCallable("testAlarms").call(data);
            }
        });

        deleteInfo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int pos = phnNumList.indexOf(phoneNumber);
                phnNumList.remove(phoneNumber);
                phnNumItems.remove(pos);
                db.collection("alerts").document(user.getUid()).update("TextNums", phnNumList);
                alertDialog.dismiss();
                phnNumAdapter.notifyDataSetChanged();

            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertDialog.dismiss();
            }
        });

        alertDialog.show();

    }

    private void ShowAddEmail(View v){
        Button add;
        Button cancel;
        final EditText emailET;

        alertAddEmail.setContentView(R.layout.layout_alert_add_email);
        add = alertAddEmail.findViewById(R.id.alertEmailDialogOKBtn);
        cancel = alertAddEmail.findViewById(R.id.alertEmailDialogCancelBtn);
        emailET = alertAddEmail.findViewById(R.id.alertEmailET);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String email = emailET.getText().toString();
                emailList.add(email);
                if (email.length() != 0){
                    db.collection("alerts").document(user.getUid()).update("Emails", emailList)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                Log.d(TAG, "update successfull");
                            }
                        }
                    });
                }
                alertAddEmail.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertAddEmail.dismiss();
            }
        });

        alertAddEmail.show();
    }

    private void ShowAddPhoneNumber(View v){
        Button add;
        Button cancel;
        final EditText phnNumET;

        alertAddPhnNum.setContentView(R.layout.layout_alert_add_phone_number);
        add = alertAddPhnNum.findViewById(R.id.alertPhnNumDialogOKBtn);
        cancel = alertAddPhnNum.findViewById(R.id.alertPhnNumDialogCancelBtn);
        phnNumET = alertAddPhnNum.findViewById(R.id.alertPhoneNumberET);

        add.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String phnNumber = phnNumET.getText().toString();
                phnNumList.add(phnNumber);
                if (phnNumber.length() != 0){
                    db.collection("alerts").document(user.getUid()).update("TextNums", phnNumList);
                }
                alertAddPhnNum.dismiss();
            }
        });

        cancel.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View view) {
                alertAddPhnNum.dismiss();
            }
        });

        alertAddPhnNum.show();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }



    public interface OnFragmentInteractionListener {
        // TODO: Update argument type and name
        void onFragmentInteraction(Uri uri);
    }
}
