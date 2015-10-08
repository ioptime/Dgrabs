/**
 * File        : UserFuntions.java
 * App name    : Goceng
 * Version     : 1.2.0
 * Created     : 05/12/14

 * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
 * Copyright (c) 2014 pongodev. All rights reserved.
 */

package com.ioptime.dgrabs.libraries;

import org.json.JSONObject;

public class UserFunctions {

	private JSONParser jsonParser;

	// Web Service
	private final String Server = "http://dgrabs.com/";

	// Folder
	private final String folderMain = "dgrabs_admin/";
	private final String folderApi = "api/";

	// Url
	private final String URLApi = Server + folderMain + folderApi;
	public final String URLAdmin = Server + folderMain;

	// Service
	private final String service_latest_deals = "latest_deals?";
	private final String service_gcm = "register_id_gcm?";
	private final String service_category_list = "category_list?";
	private final String service_deal_by_category = "deal_by_category?";
	private final String service_deal_detail = "deal_detail?";
	private final String service_deal_by_search = "deal_by_search_name?";
	private final String service_deal_around_you = "deal_around_you?";
	private final String service_currency = "currency";
	public final String service_view_deal = "view-deal.php?";
	private final String service_desc = "description.php?";

	// Param
	private final String param_start_index = "start_index=";
	private final String param_items_per_page = "items_per_page=";
	private final String param_category_id = "category_id=";
	private final String param_deal_id = "deal_id=";
	private final String param_user_lat = "user_lat=";
	private final String param_user_lng = "user_lng=";
	private final String param_keyword = "keyword=";
	private final String param_register_id = "register_id=";
	private final String param_unique_id = "unique_device_id=";

	// Key object name to get value
	public final String key_deals_id = "deal_id";
	public final String key_deals_title = "title";
	public final String key_deals_date_start = "start_date";
	public final String key_deals_date_end = "end_date";
	public final String key_deals_after_disc_value = "after_discount_value";
	public final String key_deals_start_value = "start_value";
	public final String key_deals_image = "image";
	public final String key_deals_company = "company";
	public final String key_deals_address = "address";
	public final String key_deals_lat = "latitude";
	public final String key_deals_lng = "longitude";
	public final String key_deals_url = "deal_url";
	public final String key_deals_disc = "discount";
	public final String key_deals_save = "save_value";
	public final String key_deals_desc = "description";
	public final String key_category_id = "category_id";
	public final String key_category_name = "category_name";
	public final String key_category_marker = "category_marker";
	public final String key_currency_code = "code";
	public final String key_status = "status";
	public final String key_total_data = "totalData";
	// Array
	public final String array_latest_deals = "latestDeals";
	public final String array_category_list = "categoryList";
	public final String array_deal_detail = "dealDetail";
	public final String array_place_by_search = "dealBySearchName";
	public final String array_around_you = "dealAroundYou";
	public final String array_deal_by_category = "dealByCategory";
	public final String array_currency = "currency";
	public final String array_register_id = "registerID";

	// LoadUrl
	public final String varLoadURL = Server + folderMain + service_desc
			+ param_deal_id;
	private String webService;
	public final int valueItemsPerPage = 10;

	// Google project id
	public static final String SENDER_ID = "76984860688";

	// constructor
	public UserFunctions() {
		jsonParser = new JSONParser();
	}

	/**
	 * function make Login Request
	 * 
	 * @param email
	 * @param password
	 * */

	// http://your-website/your-folder/api/register_id_gcm?register_id=1234567890&unique_device_id=123456789
	public JSONObject registerIdGcm(String register_id, String unique_id) {
		webService = URLApi + service_gcm + param_register_id + register_id
				+ "&" + param_unique_id + unique_id;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject latestDeals(int valueStartIndex) {
		webService = URLApi + service_latest_deals + param_start_index
				+ valueStartIndex + "&" + param_items_per_page
				+ valueItemsPerPage;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject aroundYou(double userLat, double userLng) {
		webService = URLApi + service_deal_around_you + param_user_lat
				+ userLat + "&" + param_user_lng + userLng;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject dealDetail(String dealId) {
		webService = URLApi + service_deal_detail + param_deal_id + dealId;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject categoryList() {
		webService = URLApi + service_category_list;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject dealByCategory(String valueCategoryId, int valueStartIndex) {

		webService = URLApi + service_deal_by_category + param_category_id
				+ valueCategoryId + "&" + param_start_index + valueStartIndex
				+ "&" + param_items_per_page + valueItemsPerPage;

		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject searchByName(String valueKeyName, int valueStartIndex,
			int valueItemsPerPage) {

		webService = URLApi + service_deal_by_search + param_keyword
				+ valueKeyName + "&" + param_start_index + valueStartIndex
				+ "&" + param_items_per_page + valueItemsPerPage;

		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject currency() {
		webService = URLApi + service_currency;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

	public JSONObject loadURL(String dealId) {
		webService = Server + folderMain + service_desc + param_deal_id
				+ dealId;
		JSONObject json = jsonParser.getJSONFromUrl(webService);
		return json;
	}

}