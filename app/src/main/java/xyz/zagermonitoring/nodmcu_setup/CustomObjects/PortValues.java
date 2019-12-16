package xyz.zagermonitoring.nodmcu_setup.CustomObjects;

public class PortValues {
    private Number setTemp;
    private Number lowAlarm;
    private Number highAlarm;
    private Boolean AlarmOn;

    public PortValues(Number setTemp, Number lowAlarm, Number highAlarm, Boolean AlarmOn){
        this.setTemp = setTemp;
        this.lowAlarm = lowAlarm;
        this.highAlarm = highAlarm;
        this.AlarmOn = AlarmOn;
    }

    public Number getSetTemp() {
        return setTemp;
    }
    public Number getLowAlarm(){
        return lowAlarm;
    }
    public Number getHighAlarm(){
        return highAlarm;
    }
    public Boolean getAlarmOn(){
        return AlarmOn;
    }

    public void setAlarmOn(Boolean alarmOn) {
        this.AlarmOn = alarmOn;
    }

    public void setHighAlarm(Number highAlarm) {
        this.highAlarm = highAlarm;
    }
    public void setSetTemp(Number setTemp){
        this.setTemp= setTemp;
    }
    public void setLowAlarm(Number lowAlarm){
        this.lowAlarm = lowAlarm;
    }
}
