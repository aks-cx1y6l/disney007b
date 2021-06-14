package com.mz.jarboot.dto;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.mz.jarboot.constant.ResultCodeConst;

public abstract class BaseResponse {
	/**默认成功*/
	protected int resultCode = ResultCodeConst.SUCCESS;
	protected String resultMsg;
	protected Long total;
	
	public BaseResponse() {
		
	}
	
	public int getResultCode() {
		return resultCode;
	}
	
	public void setResultCode(int resultCode) {
		this.resultCode = resultCode;
	}

	public String getResultMsg() {
		return resultMsg;
	}

	public void setResultMsg(String resultMsg) {
		this.resultMsg = resultMsg;
	}

	public Long getTotal() {
		return total;
	}

	public void setTotal(Long total) {
		this.total = total;
	}
	
	@JsonIgnore
	public boolean isOk(){
		return resultCode == ResultCodeConst.SUCCESS;
	}
	
}
