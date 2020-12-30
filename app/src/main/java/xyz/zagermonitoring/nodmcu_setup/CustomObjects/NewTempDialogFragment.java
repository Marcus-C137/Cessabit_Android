package xyz.zagermonitoring.nodmcu_setup.CustomObjects;

import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

import xyz.zagermonitoring.nodmcu_setup.R;

public class NewTempDialogFragment extends DialogFragment {
    private static final String TAG = "NewTempDialogFragment";
    private EditText newTempText;
    private TextView header;
    private TextView cancel;
    private TextView change;
    public OnNewTemp mOnNewTemp;

    public interface OnNewTemp{
        void sendNewTemp(int category, Number newTemp);
    }

    public static NewTempDialogFragment newInstance(int category) {
        NewTempDialogFragment frag = new NewTempDialogFragment();
        Bundle args = new Bundle();
        args.putInt("category", category);
        frag.setArguments(args);
        return frag;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.dialog_new_temp, container, false);
        header = view.findViewById(R.id.tv_newTempDialog_Header);
        newTempText = view.findViewById(R.id.et_newTempDialog_NewTemp);
        cancel = view.findViewById(R.id.tv_newTempDialog_Cancel);
        change = view.findViewById(R.id.tv_newTempDialog_Change);
        String headerText = "";
        final int category = getArguments().getInt("category");
        switch (category){
            case 1:
                headerText = "Enter New Set Temperature";
                break;
            case 2:
                headerText = "Enter New Low Alarm Temperature";
                break;
            case 3:
                headerText = "Enter New High Alarm Temperature";
                break;
            default:
                Log.e(TAG, "Not uno dos o tres");
        }
        header.setText(headerText);
        cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                getDialog().dismiss();
            }
        });
        change.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Number newTemp = Double.parseDouble(newTempText.getText().toString());
                mOnNewTemp.sendNewTemp(category, newTemp);
                getDialog().dismiss();
            }
        });
        return view;
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        try{
            mOnNewTemp = (OnNewTemp) getTargetFragment();
        }catch(ClassCastException e){
            Log.e(TAG, "onAttach: ClassCastException: " + e.getMessage());
        }
    }
}
