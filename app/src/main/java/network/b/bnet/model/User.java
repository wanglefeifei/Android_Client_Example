package network.b.bnet.model;

import java.io.Serializable;

public class User implements Serializable {
    private static final long serialVersionUID = 1L;
    private Integer userId;
    private String Token;
    private String nickName;
    private String passWord;
    private Integer age;
    private Integer sex;
    private String mobilePhone;
    private String qqOpendid;
    private String wxOpendid;

    public String getHeadUrl() {
        return headUrl;
    }

    public void setHeadUrl(String headUrl) {
        this.headUrl = headUrl;
    }

    private String headUrl;

    public String getEeoId() {
        return eeoId;
    }

    public void setEeoId(String eeoId) {
        this.eeoId = eeoId;
    }

    private String eeoId;

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getAge() {
        return age;
    }

    public void setAge(Integer age) {
        this.age = age;
    }

    public String getNickName() {
        return nickName;
    }

    public void setNickName(String nickName) {
        this.nickName = nickName;
    }

    public String getMobilePhone() {
        return mobilePhone;
    }

    public void setMobilePhone(String mobilePhone) {
        this.mobilePhone = mobilePhone;
    }

    public String getQqOpendid() {
        return qqOpendid;
    }

    public void setQqOpendid(String qqOpendid) {
        this.qqOpendid = qqOpendid;
    }

    public String getWxOpendid() {
        return wxOpendid;
    }

    public void setWxOpendid(String wxOpendid) {
        this.wxOpendid = wxOpendid;
    }

    public String getPassWord() {
        return passWord;
    }

    public void setPassWord(String passWord) {
        this.passWord = passWord;
    }

    public Integer getSex() {
        return sex;
    }

    public void setSex(Integer sex) {
        this.sex = sex;
    }


    public String getToken() {
        return Token;
    }

    public void setToken(String token) {
        Token = token;
    }
}
