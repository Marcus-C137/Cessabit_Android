package xyz.zagermonitoring.nodmcu_setup.Items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class FirebaseTimeTemp implements Parcelable {
    private ArrayList<Long> _times;
    private ArrayList<Float> _temps;
    private ArrayList<Float> _powers;

    public FirebaseTimeTemp(ArrayList<Long> times, ArrayList<Float> temps, ArrayList<Float> powers){
        _times = times;
        _temps = temps;
        _powers = powers;
    }

    protected FirebaseTimeTemp(Parcel in) {
        in.readList(_times, Long.class.getClassLoader());
        in.readList(_temps, Float.class.getClassLoader());
        in.readList(_powers, Float.class.getClassLoader());
    }

    public static final Creator<FirebaseTimeTemp> CREATOR = new Creator<FirebaseTimeTemp>() {
        @Override
        public FirebaseTimeTemp createFromParcel(Parcel in) {
            return new FirebaseTimeTemp(in);
        }

        @Override
        public FirebaseTimeTemp[] newArray(int size) {
            return new FirebaseTimeTemp[size];
        }
    };

    public ArrayList<Long> getTimes(){
        return _times;
    }

    public ArrayList<Float> getTemps(){
        return _temps;
    }

    public ArrayList<Float> getPowers(){
        return _powers;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeList(_times);
        parcel.writeList(_temps);
        parcel.writeList(_powers);
    }
}
