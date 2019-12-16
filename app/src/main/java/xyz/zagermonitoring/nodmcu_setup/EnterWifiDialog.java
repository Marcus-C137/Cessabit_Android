package xyz.zagermonitoring.nodmcu_setup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatDialogFragment;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;


public class EnterWifiDialog extends AppCompatDialogFragment {
    private EditText editTextPassword;
    private EnterWifiListener listener;

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState){
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_enter_wifi, null);
        editTextPassword = view.findViewById(R.id.Wifi_Password);

        builder.setView(view).setTitle("Wifi Password")
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Ok", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String WIFI_password = editTextPassword.getText().toString();
                        listener.applyTexts(WIFI_password);
                    }
                });


        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try {
            listener = (EnterWifiListener) context;
        } catch(ClassCastException e){
            throw new ClassCastException(context.toString()+
                    "must implement EnterWifiListener");
        }
    }


    public interface EnterWifiListener{
        void applyTexts(String WIFI_password);
    }
}
