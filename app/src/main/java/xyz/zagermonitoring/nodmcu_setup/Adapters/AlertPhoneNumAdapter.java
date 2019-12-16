package xyz.zagermonitoring.nodmcu_setup.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import xyz.zagermonitoring.nodmcu_setup.Items.AlertItem;
import xyz.zagermonitoring.nodmcu_setup.R;

public class AlertPhoneNumAdapter extends RecyclerView.Adapter<AlertPhoneNumAdapter.AlertViewHolder>{

    private ArrayList<AlertItem> mAlertList;
    private OnAlertPhoneNumClickListener mOnAlertPhoneNumListener;

    public interface OnAlertPhoneNumClickListener{
        void onAlertPhoneNumClick(int position);
    }

    public void setOnClickListener(OnAlertPhoneNumClickListener listener){
        mOnAlertPhoneNumListener = listener;
    }


    public static class AlertViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView alertInfo;

        public AlertViewHolder(@NonNull View itemView, final OnAlertPhoneNumClickListener onAlertPhoneNumClickListener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.alertsInfoImg);
            alertInfo = itemView.findViewById(R.id.alertsInfoTxtView);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(onAlertPhoneNumClickListener != null){
                        int position = getAdapterPosition();
                        onAlertPhoneNumClickListener.onAlertPhoneNumClick(position);
                    }
                }

            });
        }
    }

    public AlertPhoneNumAdapter(ArrayList<AlertItem> alertList){
        mAlertList = alertList;
    }

    @NonNull
    @Override
    public AlertViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_card_list, parent, false);
        AlertViewHolder AVH = new AlertViewHolder(v, mOnAlertPhoneNumListener);
        return AVH;
    }

    @Override
    public void onBindViewHolder(@NonNull AlertViewHolder holder, int position) {
        AlertItem currentItem = mAlertList.get(position);

        holder.mImageView.setImageResource(currentItem.getmImageResource());
        holder.alertInfo.setText(currentItem.getmAlertInfo());
    }

    @Override
    public int getItemCount() {
        return mAlertList.size();
    }
}
