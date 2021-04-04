package xyz.zagermonitoring.nodmcu_setup;

import android.content.Context;
import android.os.Bundle;
import androidx.fragment.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;


public class SubscriptionFragment extends Fragment{

    private OnFragmentInteractionListener mListener;
    private Button buyButton;
    private int billingResponseCode;

    public SubscriptionFragment() {
        // Required empty public constructor
    }

    public interface OnFragmentInteractionListener{
        void buyButtonClicked();
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View view = inflater.inflate(R.layout.fragment_subscriptions, container, false);
        buyButton = view.findViewById(R.id.buyButton);
        buyButton.setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                if (billingResponseCode ==0){
                    onButtonPressed();
                }else if(billingResponseCode ==3){
                    Toast.makeText(getContext(), "Please Set up Google Pay", Toast.LENGTH_LONG).show();
                }
            }
        });
        return view;
    }

    private void onButtonPressed() {
        if (mListener != null) {
            mListener.buyButtonClicked();
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if(context instanceof OnFragmentInteractionListener){
            try{
                ((HomePage)this.getActivity()).setupBillingClient();
                billingResponseCode = ((HomePage)this.getActivity()).getResponseCode();
            }catch(Error e){
                Log.w("error in subscriptions", "it is ");
                e.printStackTrace();
            }
            mListener = (OnFragmentInteractionListener) context;
        }else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

}