package xyz.zagermonitoring.nodmcu_setup.Items;

public class AlertItem {
    private int mImageResource;
    private String mAlertInfo;

    public AlertItem(int ImageResource, String AlertInfo){
        mImageResource = ImageResource;
        mAlertInfo = AlertInfo;
    }

    public int getmImageResource(){
        return  mImageResource;
    }

    public String getmAlertInfo(){
        return mAlertInfo;
    }
}
