package com.digiwin.dappratice1.sales.service.impl;

import java.net.MalformedURLException;
import java.net.URISyntaxException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

import org.apache.commons.collections.CollectionUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.transaction.annotation.Transactional;

import com.digiwin.app.container.exceptions.DWInvocationException;
import com.digiwin.app.dao.DWDao;
import com.digiwin.app.dao.DWPagableQueryInfo;
import com.digiwin.app.dao.DWPaginationQueryResult;
import com.digiwin.app.dao.DWQueryCondition;
import com.digiwin.app.dao.DWQueryInfo;
import com.digiwin.app.dao.DWQueryValueOperator;
import com.digiwin.app.dao.DWServiceResultBuilder;
import com.digiwin.app.data.DWDataRow;
import com.digiwin.app.data.DWDataRowState;
import com.digiwin.app.data.DWDataSet;
import com.digiwin.app.data.DWDataSetOperationOption;
import com.digiwin.app.data.DWDataTable;
import com.digiwin.app.service.DWServiceResult;
import com.digiwin.app.serviceclient.ServiceClient;
import com.digiwin.app.serviceclient.rpc.DWDapInvTarget;
import com.digiwin.app.serviceclient.rpc.DWInvMessage;
import com.digiwin.dappratice1.sales.service.IOrderService;
import com.digiwin.dappratice1.sales.service.impl.model.DWResult;
import com.digiwin.utils.DWTenantUtils;

public class OrderServiceImpl implements IOrderService {

	@Autowired
	private DWDao dao;
	protected static final String[] ORDER_FIELDS = {"tenantsid", "order_id", "order_date", "salesman_id", "org_id",
			"amount"};
	protected static final String[] ORDER_DETAIL_FIELDS = {"tenantsid", "order_id", "seq", "quantity", "price",
			"amount"};

	private static final String ORDER_TABLE_NAME = "dappratice1_order";
	private static final String ORDER_DETAIL_TABLE_NAME = "dappratice1_order_detail";

	private void prepareData(Map<String, Object> dataset, DWDataSet ds, String tableName, String[] fields,
			Optional<Consumer<List<String>>> returnedCon) {
		List<Map<String, Object>> dataList = getList(dataset, tableName);
		DWDataTable table = ds.newTable(tableName);
		List<String> orderIds = dataList.stream().map(data -> {
			DWDataRow row = table.newRow();
			row.setState(String
					.valueOf(data.getOrDefault(DWDataRowState.COLUMN_NAME_ROW_STATE, DWDataRowState.UPDATE_OPERATION))
					.toUpperCase());
			for (String field : fields) {
				Object value = data.get(field);
				if (value != null) {
					row.set(field, value);
				}
			}
			return String.valueOf(data.get("order_id"));
		}).collect(Collectors.toList());
		returnedCon.ifPresent(con -> con.accept(orderIds));
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DWServiceResult post(Map<String, Object> dataset) throws Exception {
		// order
		DWDataSet dataSet = new DWDataSet();
		prepareData(dataset, dataSet, ORDER_TABLE_NAME, ORDER_FIELDS, Optional.empty());
		dao.execute(dataSet);

		// order detail
		prepareData(dataset, dataSet, ORDER_DETAIL_TABLE_NAME, ORDER_DETAIL_FIELDS, Optional.empty());
		dao.execute(dataSet);
		DWServiceResult result = DWServiceResultBuilder.build(null);
		result.setSuccess(true);
		result.setMessage("新增成功");
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DWServiceResult put(Map<String, Object> dataset) throws Exception {
		DWDataSet dataSet = new DWDataSet();
		DWDataSetOperationOption option = new DWDataSetOperationOption();
		option.getUpdateOption().enableBatchCondition();
		prepareData(dataset, dataSet, ORDER_TABLE_NAME, ORDER_FIELDS, Optional.empty());
		dao.execute(dataSet, option);

		prepareData(dataset, dataSet, ORDER_DETAIL_TABLE_NAME, ORDER_DETAIL_FIELDS, Optional.empty());
		dao.execute(dataSet, option);
		DWServiceResult result = DWServiceResultBuilder.build(null);
		result.setSuccess(true);
		result.setMessage("修改成功");
		return result;
	}

	@Override
	@Transactional(rollbackFor = Exception.class)
	public DWServiceResult delete(Map<String, Object> dataset) throws Exception {
		DWDataSet ds = new DWDataSet();
		DWDataSetOperationOption option = new DWDataSetOperationOption();
		option.getTableStatementOption().enableBatchMode();
		prepareData(dataset, ds, ORDER_TABLE_NAME, ORDER_FIELDS, Optional.of(list -> {
			DWDataTable table = ds.newTable(ORDER_DETAIL_TABLE_NAME);
			DWQueryInfo queryInfo = new DWQueryInfo(ORDER_DETAIL_TABLE_NAME);
			queryInfo.addFieldInfo("order_id", DWQueryValueOperator.In, list.toArray());
			queryInfo.addSelectField("order_id", "seq", "tenantsid");

			List<Map<String, Object>> datas = transResult(queryInfo, ORDER_DETAIL_TABLE_NAME);
			datas.forEach(map -> {
				DWDataRow row = table.newRow();
				row.setState(DWDataRowState.DELETE_OPERATION);
				row.set("order_id", map.get("order_id"));
				row.set("seq", map.get("seq"));
				row.set("tenantsid", map.get("tenantsid"));
			});
		}));
		dao.execute(ds, option);
		DWServiceResult result = DWServiceResultBuilder.build(null);
		result.setSuccess(true);
		result.setMessage("删除成功");
		return result;
	}

	@Override
	public DWServiceResult list(Map<String, Object> queryInfo) throws Exception {
		DWPagableQueryInfo pagableQueryInfo = prepareQueryData(queryInfo);
		DWPaginationQueryResult dwPaginationQueryResult = dao.selectWithPage(pagableQueryInfo);
		Map<String, Object> transform = transform(dwPaginationQueryResult);
		DWServiceResult result = DWServiceResultBuilder.build(transform);
		result.setSuccess(true);
		result.setMessage("查詢成功");

		return result;
	}

	@Override
	public DWServiceResult get(String orderId) throws Exception {
		StringBuilder sbSql = new StringBuilder(DWTenantUtils.getTenantIgnoreTagByColumnName());
		List<Object> params = new ArrayList<>();
		sbSql.append("select t1.tenantsid,t1.order_id,t1.order_date,t1.salesman_id,t1.org_id,t1.amount,");
		sbSql.append("t2.seq,t2.quantity,t2.price,t2.amount as detail_amount from dappratice1_order t1");
		sbSql.append(" inner join dappratice1_order_detail t2 on t1.order_id=t2.order_id where t1.order_id=?");
		// params.add(BmCommonUtils.tenantId());
		params.add(orderId);
		List<Map<String, Object>> datas = dao.select(sbSql.toString(), params.toArray());
		Map<String, Object> rs = buildDatas(datas);
		DWServiceResult result = DWServiceResultBuilder.build(rs);
		result.setSuccess(true);
		result.setMessage("查詢成功");
		return result;
	}

	private Map<String, Object> buildDatas(List<Map<String, Object>> datas) {
		if (CollectionUtils.isEmpty(datas)) {
			return new HashMap<>();
		}

		Map<String, Object> primary = datas.get(0);

		Map<String, Object> outer = new HashMap<>();
		Map<String, Object> order = new HashMap<>();
		List<Map<String, Object>> orderValue = new ArrayList<>();
		order.put("salesman_id", primary.get("salesman_id"));
		order.put("order_date", primary.get("order_date"));
		order.put("amount", primary.get("amount"));
		order.put("tenantsid", primary.get("tenantsid"));
		order.put("org_id", primary.get("org_id"));
		order.put("order_id", primary.get("order_id"));
		Map<String, Object> child = new HashMap<>();
		List<Map<String, Object>> children = datas.stream().map(data -> {
			Map<String, Object> inner = new HashMap<>();
			inner.put("amount", data.get("detail_amount"));
			inner.put("quantity", data.get("quantity"));
			inner.put("tenantsid", primary.get("tenantsid"));
			inner.put("price", data.get("price"));
			inner.put("seq", data.get("seq"));
			inner.put("order_id", primary.get("order_id"));
			return inner;
		}).collect(Collectors.toList());
		child.put("dappratice1_order_detail", children);
		order.put("child", child);
		orderValue.add(order);
		outer.put("dappratice1_order", orderValue);
		outer.put("dappratice1_order_detail", new ArrayList<>());
		return outer;
	}

	private List<Map<String, Object>> transResult(DWQueryInfo queryInfo, String tableName) {
		DWDataSet dataSet = dao.select(queryInfo);
		Iterator<DWDataRow> iterator = dataSet.getTable(tableName).getRows().iterator();
		List<Map<String, Object>> datas = new ArrayList<>();
		while (iterator.hasNext()) {
			DWDataRow next = iterator.next();
			datas.add(next.getData());
		}
		return datas;
	}

	private Map<String, Object> transform(DWPaginationQueryResult dwPaginationQueryResult)
			throws MalformedURLException, DWInvocationException, URISyntaxException {
		Map<String, Object> result = new HashMap<>();
		DWDataTable primaryTable = dwPaginationQueryResult.getDataSet().getTables().getPrimaryTable();
		Iterator<DWDataRow> iterator = primaryTable.getRows().iterator();
		List<Map<String, Object>> datas = new ArrayList<>();
		while (iterator.hasNext()) {
			DWDataRow next = iterator.next();
			Map<String, Object> data = next.getData();
			String salesmanName = getSalesmanNameById(data.get("salesman_id"));
			data.put("salesman_name", salesmanName);
			datas.add(data);
		}
		result.put("pageCount", dwPaginationQueryResult.getPageCount());
		result.put("rowCount", dwPaginationQueryResult.getRowCount());
		result.put("pageSize", dwPaginationQueryResult.getPageSize());
		result.put("currentPage", dwPaginationQueryResult.getCurrentPage());
		result.put("dappratice1_order", datas);
		return result;
	}

	private String getSalesmanNameById(Object salesmanId)
			throws MalformedURLException, URISyntaxException, DWInvocationException {
		ServiceClient sc = new ServiceClient();
		DWDapInvTarget invTarget = DWDapInvTarget.createServiceRouteDapAPI("basic", "salesman", "");
		DWInvMessage message = new DWInvMessage();
		Map<String, Object> data = new HashMap<>();
		data.put("salesmanId", salesmanId);
		message.setEntity(data);
		DWResult result = sc.invoke(invTarget, message, DWResult.class);
		if (HttpStatus.OK.value() == result.getStatus()) {
			return result.getResponse().getData().toString();
		} else {
			return "";
		}
	}

	private DWPagableQueryInfo prepareQueryData(Map<String, Object> queryInfo) {
		DWPagableQueryInfo pagableQueryInfo = new DWPagableQueryInfo(ORDER_TABLE_NAME);
		buildPageInfo(pagableQueryInfo, queryInfo);
		buildSelectFields(pagableQueryInfo, queryInfo);
		buildCondition(pagableQueryInfo, queryInfo);
		return pagableQueryInfo;
	}

	private void buildSelectFields(DWPagableQueryInfo pagableQueryInfo, Map<String, Object> queryInfo) {
		List<String> orderfileds = Optional.ofNullable(queryInfo.get("orderfileds")).map(of -> (List<String>) of)
				.orElseGet(() -> Arrays.asList(ORDER_FIELDS));
		pagableQueryInfo.addSelectField(orderfileds.toArray(new String[0]));
	}

	private void buildCondition(DWPagableQueryInfo pagableQueryInfo, Map<String, Object> queryInfo) {
		Map<String, Object> condition = getMap(queryInfo, "condition");
		DWQueryCondition queryCondition = new DWQueryCondition();
		setJoin(condition, queryCondition);
		List<Map<String, Object>> items = getList(condition, "items");
		items.forEach(item -> {
			setJoin(item, queryCondition);
			List<Map<String, Object>> itemInners = getList(item, "items");
			itemInners.forEach(itemInner -> addEqualInfo(queryCondition, itemInner));
		});
		pagableQueryInfo.setCondition(queryCondition);
	}

	private void buildPageInfo(DWPagableQueryInfo pagableQueryInfo, Map<String, Object> queryInfo) {
		Integer pageNumber = Optional.ofNullable(queryInfo.get("pageNumber")).map(pn -> ((Double) pn).intValue())
				.orElse(1);
		Integer pageSize = Optional.ofNullable(queryInfo.get("pageSize")).map(ps -> ((Double) ps).intValue())
				.orElse(10);
		pagableQueryInfo.setPageNumber(pageNumber);
		pagableQueryInfo.setPageSize(pageSize);
	}

	private void setJoin(Map<String, Object> item, DWQueryCondition queryCondition) {
		String joinInner = item.getOrDefault("joinOperator", "AND").toString();
		if ("AND".equalsIgnoreCase(joinInner)) {
			queryCondition.ANDJoin();
		} else {
			queryCondition.ORJoin();
		}
	}

	private void addEqualInfo(DWQueryCondition queryCondition, Map<String, Object> item) {
		Object name = item.get("name");
		Object value = item.get("value");
		if (value != null && name != null) {
			queryCondition.addEqualInfo(name.toString(), value);
		}
	}

	private Map<String, Object> getMap(Object map, String key) {
		return (Map<String, Object>) ((Map<String, Object>) map).get(key);
	}

	private List<Map<String, Object>> getList(Object map, String key) {
		return (List<Map<String, Object>>) ((Map<String, Object>) map).get(key);
	}
}
