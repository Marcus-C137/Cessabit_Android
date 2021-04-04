package xyz.zagermonitoring.nodmcu_setup.Items;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;

public class FirebaseTimeTempUpdate implements Parcelable {
    private Long _time;
    private Float _temp;
    private Float _power;

    public FirebaseTimeTempUpdate(Long time, Float temp, Float power){
        _time = time;
        _temp = temp;
        _power = power;
    }

    protected FirebaseTimeTempUpdate(Parcel in) {
        in.readLong();
        in.readFloat();
        in.readFloat();
    }

    public static final Creator<FirebaseTimeTempUpdate> CREATOR = new Creator<FirebaseTimeTempUpdate>() {
        @Override
        public FirebaseTimeTempUpdate createFromParcel(Parcel in) {
            return new FirebaseTimeTempUpdate(in);
        }

        @Override
        public FirebaseTimeTempUpdate[] newArray(int size) {
            return new FirebaseTimeTempUpdate[size];
        }
    };

    public Long getTime(){
        return _time;
    }

    public Float getTemp(){
        return _temp;
    }

    public Float getPower(){
        return _power;
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeLong(_time);
        parcel.writeFloat(_temp);
        parcel.writeFloat(_power);
    }
}
