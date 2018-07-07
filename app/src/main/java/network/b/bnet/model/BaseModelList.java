package network.b.bnet.model;

import java.util.List;

public class BaseModelList<T> {
	private static final long serialVersionUID = 1L;
	private int code;

	public int getCode() {
		return code;
	}

	public void setCode(int code) {
		this.code = code;
	}

	public String getInfo() {
		return info;
	}

	public void setInfo(String info) {
		this.info = info;
	}



	private String info;

	public List<T> getData() {
		return data;
	}

	public void setData(List<T> data) {
		this.data = data;
	}

	private List<T> data;

}
