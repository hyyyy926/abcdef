package com.digiwin.dappratice1.sales.service;

import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;
import com.digiwin.app.service.DWServiceResult;

public interface IOrderReportService extends DWService {

	/**
	 * 查询订单报表数据
	 * 
	 * @param salesmanId
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.report.get")
	@AllowAnonymous
	DWServiceResult getReport(String salesmanId) throws Exception;

}