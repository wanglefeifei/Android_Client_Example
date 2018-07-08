package network.b.bnet.model;

import java.io.Serializable;

public class BaseModel<T> implements Serializable {
    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private int code = -1000;

    public T getData() {
        return data;
    }

    public void setData(T data) {
        this.data = data;
    }

    public String getInfo() {
        return info;
    }

    public void setInfo(String info) {
        this.info = info;
    }

    public int getCode() {
        return code;
    }

    public void setCode(int code) {
        this.code = code;
    }

    private String info;
    private T data;


}
