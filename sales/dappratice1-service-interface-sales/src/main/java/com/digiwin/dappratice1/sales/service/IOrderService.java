package com.digiwin.dappratice1.sales.service;

import java.util.Map;

import com.digiwin.app.service.AllowAnonymous;
import com.digiwin.app.service.DWService;
import com.digiwin.app.service.DWServiceResult;

public interface IOrderService extends DWService {

	/**
	 * 新增订单数据
	 * 
	 * @param dataset
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.create")
	@AllowAnonymous
	DWServiceResult post(Map<String, Object> dataset) throws Exception;

	/**
	 * 更新订单数据
	 * 
	 * @param dataset
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.update")
	@AllowAnonymous
	DWServiceResult put(Map<String, Object> dataset) throws Exception;

	/**
	 * 删除订单数据
	 * 
	 * @param dataset
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.delete")
	@AllowAnonymous
	DWServiceResult delete(Map<String, Object> dataset) throws Exception;

	/**
	 * 查询订单列表
	 * 
	 * @param queryInfo
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.list")
	@AllowAnonymous
	DWServiceResult list(Map<String, Object> queryInfo) throws Exception;

	/**
	 * 查询订单列表
	 *
	 * @param orderId
	 * @return
	 * @throws Exception
	 */
	// @EAIService(id = "bm.sales.order.get")
	@AllowAnonymous
	DWServiceResult get(String orderId) throws Exception;
}