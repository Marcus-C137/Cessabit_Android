package xyz.zagermonitoring.nodmcu_setup;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseExpandableListAdapter;
import android.widget.CompoundButton;
import android.widget.ListView;
import android.widget.Switch;
import android.widget.TextView;


import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import xyz.zagermonitoring.nodmcu_setup.CustomObjects.PortValues;

public class TempsExpandableListAdapter extends BaseExpandableListAdapter {
    private List<Object> portAlarms;
    private List<PortValues> portValues;
    private List<Number> currentTemps;


    public TempsExpandableListAdapter(List<Number> currentTemps, List<PortValues> portValues){
        this.portValues = portValues;
        this.currentTemps=currentTemps;
    }

    public void setVals(List<Number> currentTemps, List<PortValues> portValues){
        this.portValues = portValues;
        this.currentTemps=currentTemps;
    }

    @Override
    public int getGroupCount() {
        return currentTemps.size();
    }

    @Override
    public int getChildrenCount(int groupPosition) {
        return 4;
    }

    @Override
    public Number getGroup(int groupPosition) {
        return currentTemps.get(groupPosition);
    }

    @Override
    public PortValues getChild(int groupPosition, int childPosition) {
        return portValues.get(groupPosition);
    }

    @Override
    public long getGroupId(int groupPosition) {
        return groupPosition;
    }

    @Override
    public long getChildId(int groupPosition, int childPosition) {
        return groupPosition*childPosition;
    }

    @Override
    public boolean hasStableIds() {
        return false;
    }

    @Override
    public View getGroupView(int groupPosition, boolean isExpanded, View convertView, ViewGroup parent) {
        if (convertView ==null)
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_temp_ports, parent, false);
        TextView textView = convertView.findViewById(R.id.TV_temperature);
        Number currentTemperature = getGroup(groupPosition);
        String currentTemperatureText = String.format(Locale.getDefault(), "%.1f", currentTemperature.floatValue()) +" F";
        textView.setText(currentTemperatureText);
        TextView textView1 = convertView.findViewById((R.id.tv_1));
        textView1.setText(String.valueOf(groupPosition+1));

        return convertView;
    }

    @Override
    public View getChildView(final int groupPosition, final int childPosition, boolean isLastChild, View convertView, ViewGroup parent) {
        if(!isLastChild){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_temp_temps, parent, false);
        }

        if(isLastChild){
            convertView = LayoutInflater.from(parent.getContext()).inflate(R.layout.layout_temp_alarms, parent, false);
        }

        if(!isLastChild){
            TextView textView = convertView.findViewById(R.id.TV_name);
            List<String> fields = new ArrayList<>();
            fields.add("Set Temperature");
            fields.add("Low Alarm");
            fields.add("High Alarm");
            textView.setText(fields.get(childPosition));
            TextView textView1 = convertView.findViewById(R.id.TV_setTemp);
            Number temperature = 6;
            if (childPosition == 0){
                temperature = getChild(groupPosition, childPosition).getSetTemp();
            }else if(childPosition == 1) {
                temperature = getChild(groupPosition, childPosition).getLowAlarm();
            }else if(childPosition == 2) {
                temperature = getChild(groupPosition, childPosition).getHighAlarm();
            }
            String tempDisplay = temperature.toString() + " F";
            textView1.setText(tempDisplay);
        }else{
            final Switch mSwitch = convertView.findViewById(R.id.alarmSwitch);
            mSwitch.setFocusable(false);
            mSwitch.setClickable(false);
            Boolean AlarmOn = getChild(groupPosition,childPosition).getAlarmOn();
            if(AlarmOn){
                mSwitch.setChecked(true);
            }else{
                mSwitch.setChecked(false);
            }

        }
        return convertView;
    }

    @Override
    public boolean isChildSelectable(int groupPosition, int childPosition) {
        return true;
    }


}
