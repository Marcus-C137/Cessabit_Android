package xyz.zagermonitoring.nodmcu_setup;

import android.content.Context;

import android.os.Bundle;


import androidx.fragment.app.Fragment;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;


public class SubscribedFragment extends Fragment{

    private OnFragmentInteractionListener mListener;
    Button SubscribedOKButton;


    public SubscribedFragment() {
        // Required empty public constructor
    }


    public interface OnFragmentInteractionListener{
        void SubscribedOKButtonClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscribed, container, false);
        SubscribedOKButton = view.findViewById(R.id.okSubscribedBtn);

        SubscribedOKButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Navigation.findNavController(view).navigate(R.id.homePageFragment);
            }
        });
        return view;
    }

    public void onButtonPressed() {
        if (mListener != null) {
            mListener.SubscribedOKButtonClicked();
        }
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

}