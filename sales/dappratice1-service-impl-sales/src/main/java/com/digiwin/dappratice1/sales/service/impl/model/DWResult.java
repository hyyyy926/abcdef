package com.digiwin.dappratice1.sales.service.impl.model;

import com.digiwin.app.service.DWServiceResult;

public class DWResult {

	private DWServiceResult response;

	private Integer status;

	public DWServiceResult getResponse() {
		return response;
	}

	public void setResponse(DWServiceResult response) {
		this.response = response;
	}

	public Integer getStatus() {
		return status;
	}

	public void setStatus(Integer status) {
		this.status = status;
	}
}
