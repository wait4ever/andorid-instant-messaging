package hk.edu.cuhk.ie.iems5722.group20.entity;

import android.app.Application;

public class UserInfo extends Application{
    public int id;
    public String nickName;



    public void setId(int id) {
        this.id = id;
    }



    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

//    @Override
//    public void onCreate() {
//
//        super.onCreate();
//
//        setNickName("jack");
//        setId(1);
//
//    }
}
