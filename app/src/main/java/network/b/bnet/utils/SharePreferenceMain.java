package network.b.bnet.utils;


import android.content.Context;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;

import com.google.gson.reflect.TypeToken;

import network.b.bnet.model.User;


public class SharePreferenceMain {
    private static SharePreferenceMain spm;
    public static SharedPreferences sp;
    private Editor edit;
    // public data
    private final static String BASE_MODE_DATA = "BASE_MODE_DATA";

    //number of starts
    private final String StartCount = "StartCount";

    //User login information
    private final String UserLoginInfo = "UserLoginInfo";



    private final String drawPostionInfo="drawPostionInfo";


    private SharePreferenceMain() {
    }

    @SuppressWarnings("static-access")
    public synchronized static SharePreferenceMain getSharedPreference(
            Context ct) {
        sp = ct.getSharedPreferences(BASE_MODE_DATA, ct.MODE_PRIVATE);
        if (spm == null) {

            return spm = new SharePreferenceMain();
        }

        return spm;
    }


    /*
         * number of starts
         */
    public int getStartCount() {
        int rtns = 0;
        if (sp.contains(StartCount)) {
            rtns = sp.getInt(StartCount, 0);
        }
        return rtns;
    }

    public boolean saveStartCount() {

        edit = sp.edit();
        int rtns = sp.getInt(StartCount, 0);
        edit.putInt(StartCount, rtns + 1);
        edit.commit();
        return true;
    }

    /*
            * save
            */
    public String getdrawPostionInfo(String exName) {
        String rtns = null;
        if (sp.contains(drawPostionInfo+exName)) {
            rtns = sp.getString(drawPostionInfo+exName,null);
        }
        return rtns;
    }

    public boolean savedrawPostionInfo(String exName, String info) {

        edit = sp.edit();
        edit.putString(drawPostionInfo+exName,info);
        edit.commit();
        return true;
    }
    /**
     * User login information
     *
     * @return
     */
    public User getLoginData() {
        String rtn = sp.getString(UserLoginInfo, null);
        User loginModel = null;
        if (rtn != null) {
            loginModel = Utils.base64codeToObject(rtn,
                    new TypeToken<User>() {
                    }.getType());

        } else {
            //is not login back
            return null;
        }
        return loginModel;
    }

    /**
     * save user login  information
     *
     * @param loginModel
     * @return
     */
    public boolean saveLoginModel(User loginModel) {
        try {
            String rul = "";
            if (loginModel != null) {
                rul = Utils.objectToBase64Code(loginModel);
            } else {
                rul = null;
            }
            edit = sp.edit();
            edit.putString(UserLoginInfo, rul);
            edit.commit();
            return true;
        } catch (Exception e) {
            // TODO: handle exception
            return false;
        }
    }


    /*
            * save
            */
    public String getdWalletAddr() {
        String rtns = null;
        if (sp.contains("getdWalletAddr")) {
            rtns = sp.getString("getdWalletAddr",null);
        }
        return rtns;
    }

    public boolean savedWalletAddr(String info) {

        edit = sp.edit();
        edit.putString("getdWalletAddr",info);
        edit.commit();
        return true;
    }

}
