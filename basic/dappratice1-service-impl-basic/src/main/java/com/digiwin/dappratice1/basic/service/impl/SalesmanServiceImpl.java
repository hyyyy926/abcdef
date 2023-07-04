package com.digiwin.dappratice1.basic.service.impl;

import java.util.HashMap;
import java.util.Map;

import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.service.DWServiceResult;
import com.digiwin.dappratice1.basic.service.ISalesmanService;

public class SalesmanServiceImpl implements ISalesmanService {

	private static final Map<String, String> SALESMAN = new HashMap<>();

	static {
		SALESMAN.put("s001", "銷售員1");
		SALESMAN.put("s002", "銷售員2");
		SALESMAN.put("s003", "銷售員3");
	}

	@Override
	public DWServiceResult post(String salesmanId) {

		return DWServiceResultBuilder.build(SALESMAN.getOrDefault(salesmanId, "銷售員1"));
	}
}
