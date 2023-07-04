package com.digiwin.dappratice1.sales.service.impl;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.transaction.annotation.Transactional;

import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.data.DWDataRow;
import com.digiwin.app.data.DWDataRowState;
import com.digiwin.app.data.DWDataSet;
import com.digiwin.app.data.DWDataSetOperationOption;
import com.digiwin.app.data.DWDataTable;
import com.digiwin.app.schedule.context.DWScheduleContext;
import com.digiwin.app.schedule.dataobject.ScheduleTimeDO;
import com.digiwin.app.schedule.entity.DWJobResult;
import com.digiwin.app.schedule.entity.DWScheduleQuartzInfo;
import com.digiwin.app.service.DWServiceContext;
import com.digiwin.app.service.DWServiceResult;
import com.digiwin.dappratice1.sales.service.IOrderDataSyncService;
import com.digiwin.utils.DWTenantUtils;

public class OrderDataSyncServiceImpl implements IOrderDataSyncService {
	protected static final String[] ORDER_REPORT_FIELDS = {"tenantsid", "salesman_id", "amount"};
	private static final String ORDER_REPORT_TABLE_NAME = "dappratice1_order_report";
	@Autowired
	private DWDao dao;

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DWJobResult executeJob(Map<String, Object> paramMap) throws Exception {
		DWJobResult result = new DWJobResult();
		try {
			StringBuilder selSql = new StringBuilder(DWTenantUtils.getTenantIgnoreTagByColumnName());
			selSql.append(
					"select sum(amount) as amount,salesman_id,tenantsid from dappratice1_order group by tenantsid ,salesman_id");
			List<Map<String, Object>> datas = dao.select(selSql.toString());
			if (CollectionUtils.isNotEmpty(datas)) {

				DWDataSet dataSet = new DWDataSet();
				DWDataSetOperationOption option = new DWDataSetOperationOption();
				option.getTableStatementOption().enableBatchMode();
				DWDataTable dwDataTable = dataSet.newTable(ORDER_REPORT_TABLE_NAME);
				datas.forEach(data -> {
					DWDataRow dwDataRow = dwDataTable.newRow(data);
					dwDataRow.setState(DWDataRowState.DELETE_OPERATION);
				});
				dao.execute(dataSet, option);
				datas.forEach(data -> {
					DWDataRow dwDataRow = dwDataTable.newRow(data);
					dwDataRow.setState(DWDataRowState.CREATE_OPERATION);
				});

				dao.execute(dataSet, option);
				result.setMessage("dappratice1_report aynsc");
				result.setMessageDetail("dappratice1_report aynsc success");
				// 標記工作執行成功(此行不一定要寫，平台會默認加上)
				result.setExecuteStatus(DWJobResult.OK);

			} else {
				// do nothing...
			}
		} catch (Exception e) {
			throw e;
		}
		return result;
	}

	@Override
	public DWServiceResult post(Map<String, Object> paramMap) throws Exception {
		DWScheduleQuartzInfo info = new DWScheduleQuartzInfo();
		info.setModuleName("sales");
		info.setScheduleId("Schedule002");
		info.setJobName("IOrderDataSyncService");
		info.setScheduleType("2");
		info.setScheduleName("訂單數據同步");
		info.setEnableStatus("Y");
		info.setContactList(new ArrayList<>());
		info.setTenantSid(426255514321472L);
		ScheduleTimeDO detail = new ScheduleTimeDO();
		detail.setDayOfMonth("1-30");
		detail.setIsAllowConcurrent("Y");
		detail.setTime1("08,00,00,18,00,00,2,1");
		detail.setFrequency(1);
		detail.setIsRetry("Y");
		detail.setTenantSid(426255514321472L);
		info.setDetail(detail);

		Map<String, Object> profile = new HashMap<>();
		profile.put("tenantSid", 426255514321472L);
		DWTenantUtils.setTenantEnabled(true);
		DWServiceContext.getContext().setProfile(profile);
		Object o = DWScheduleContext.getInstance().addSchedule(info);
		DWTenantUtils.setTenantEnabled(false);

		return DWServiceResultBuilder.build(o);
	}
}
