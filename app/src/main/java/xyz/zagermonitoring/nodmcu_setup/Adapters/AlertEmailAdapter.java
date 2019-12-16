package xyz.zagermonitoring.nodmcu_setup.Adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;

import xyz.zagermonitoring.nodmcu_setup.Items.AlertItem;
import xyz.zagermonitoring.nodmcu_setup.R;

public class AlertEmailAdapter extends RecyclerView.Adapter<AlertEmailAdapter.AlertEmailViewHolder>{
    private ArrayList<AlertItem> mAlertList;
    private OnAlertEmailClickListener mOnAlertEmailClickListener;

    public interface OnAlertEmailClickListener{
        void onAlertEmailClick(int position);
    }

    public void setOnItemClickListener(OnAlertEmailClickListener listener){
        mOnAlertEmailClickListener = listener;
    }


    public static class AlertEmailViewHolder extends RecyclerView.ViewHolder{
        public ImageView mImageView;
        public TextView alertInfo;

        public AlertEmailViewHolder(@NonNull View itemView, final OnAlertEmailClickListener onAlertEmailClickListener) {
            super(itemView);
            mImageView = itemView.findViewById(R.id.alertsInfoImg);
            alertInfo = itemView.findViewById(R.id.alertsInfoTxtView);
            itemView.setOnClickListener(new View.OnClickListener(){
                @Override
                public void onClick(View v){
                    if(onAlertEmailClickListener != null){
                        int position = getAdapterPosition();
                        onAlertEmailClickListener.onAlertEmailClick(position);
                    }
                }
            });
        }
    }

    public AlertEmailAdapter(ArrayList<AlertItem> alertList){
        mAlertList = alertList;
    }

    @NonNull
    @Override
    public AlertEmailViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_card_list, parent, false);
        AlertEmailViewHolder AVH = new AlertEmailViewHolder(v, mOnAlertEmailClickListener);
        return AVH;
    }

    @Override
    public void onBindViewHolder(@NonNull AlertEmailViewHolder holder, int position) {
        AlertItem currentItem = mAlertList.get(position);
        holder.mImageView.setImageResource(currentItem.getmImageResource());
        holder.alertInfo.setText(currentItem.getmAlertInfo());
    }

    @Override
    public int getItemCount() {
        return mAlertList.size();
    }
}
