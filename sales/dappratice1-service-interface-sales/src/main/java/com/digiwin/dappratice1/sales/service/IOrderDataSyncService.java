package com.digiwin.dappratice1.sales.service;

import java.util.Map;

import com.digiwin.app.schedule.entity.DWJobResult;
import com.digiwin.app.schedule.quartz.job.DWJob;
import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;
import com.digiwin.app.service.DWServiceResult;

public interface IOrderDataSyncService extends DWService, DWJob {

	/**
	 * 订单同步
	 * 
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@AllowAnonymous
	DWJobResult executeJob(Map<String, Object> paramMap) throws Exception;

	/**
	 * 添加定时任务
	 * 
	 * @param paramMap
	 * @return
	 * @throws Exception
	 */
	@AllowAnonymous
	DWServiceResult post(Map<String, Object> paramMap) throws Exception;
}
