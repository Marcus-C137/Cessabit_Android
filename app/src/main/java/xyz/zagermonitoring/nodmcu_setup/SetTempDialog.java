package xyz.zagermonitoring.nodmcu_setup;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.appcompat.app.AppCompatDialogFragment;


public class SetTempDialog extends AppCompatDialogFragment {
    private setTempsDialogListener listener;
    private EditText editTextSetTemp;
    private String title;
    private Integer group;
    private Integer child;

    public void setVals(String title, Integer group, Integer child){
        this.title = title;
        this.group = group;
        this.child = child;
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        LayoutInflater inflater = getActivity().getLayoutInflater();
        View view = inflater.inflate(R.layout.layout_set_temp,null);

        builder.setView(view)
                .setTitle("New " + title)
                .setNegativeButton("Cancel", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {

                    }
                })
                .setPositiveButton("Change", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        String setTemp = editTextSetTemp.getText().toString();
                        listener.applyTexts(setTemp, group, child);

                    }
                });
        editTextSetTemp = view.findViewById(R.id.editTextSetTemp);

        return builder.create();
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);

        try{
            listener = (setTempsDialogListener) context;
        }catch(ClassCastException e){
            throw new ClassCastException(context.toString() + "must implement ExampleDialog Listener");
        }
    }

    public interface setTempsDialogListener{
        void applyTexts(String setTemp, Integer group, Integer child);
    }
}
