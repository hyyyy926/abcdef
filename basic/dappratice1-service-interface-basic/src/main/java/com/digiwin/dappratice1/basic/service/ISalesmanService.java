package com.digiwin.dappratice1.basic.service;

import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;
import com.digiwin.app.service.DWServiceResult;

public interface ISalesmanService extends DWService {

	/**
	 * 查询销售员姓名
	 * 
	 * @param salesmanId
	 * @return
	 */
	// @EAIService(id = "bm.basic.salesman.get")
	@AllowAnonymous
	DWServiceResult post(String salesmanId);
}
