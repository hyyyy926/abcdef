package com.digiwin.dappratice1.sales.service.impl;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.springframework.beans.factory.annotation.Autowired;

import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWQueryInfo;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.data.DWDataRow;
import com.digiwin.app.data.DWDataSet;
import com.digiwin.app.service.DWServiceResult;
import com.digiwin.dappratice1.sales.service.IOrderReportService;

public class OrderReportServiceImpl implements IOrderReportService {

	protected static final String[] ORDER_REPORT_FIELDS = {"tenantsid", "salesman_id", "amount"};
	private static final String ORDER_REPORT_TABLE_NAME = "dappratice1_order_report";

	@Autowired
	private DWDao dao;

	@Override
	public DWServiceResult getReport(String salesmanId) throws Exception {
		DWQueryInfo queryInfo = new DWQueryInfo(ORDER_REPORT_TABLE_NAME);
		queryInfo.addSelectField(ORDER_REPORT_FIELDS);
		queryInfo.addEqualInfo("salesman_id", salesmanId);
		queryInfo.addEqualInfo("tenantsid", "426255514321472");
		DWDataSet select = dao.select(queryInfo);
		Iterator<DWDataRow> iterator = select.getTable(ORDER_REPORT_TABLE_NAME).getRows().iterator();
		List<Map<String, Object>> result = new ArrayList<>();
		while (iterator.hasNext()) {
			DWDataRow next = iterator.next();
			result.add(next.getData());
		}
		return DWServiceResultBuilder.build(result);
	}
}
