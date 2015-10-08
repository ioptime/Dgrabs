/**
 * File        : ActivityDetail.java
 * App name    : Goceng
 * Version     : 1.2.0
 * Created     : 05/12/14

 * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
 * Copyright (c) 2014 pongodev. All rights reserved.
 */

package com.ioptime.dgrabs;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Bitmap;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.ioptime.dgrabs.libraries.UserFunctions;
import com.ioptime.dgrabs.utils.Utils;

import com.squareup.picasso.Picasso;

public class ActivityDetail extends ActionBarActivity implements
		OnClickListener {

	// Create an instance of ActionBar
	private ActionBar actionbar;

	// Declare object of AdView class

	// Declare object of userFunction, JSONObject, and Utils class
	private UserFunctions userFunction;
	private JSONObject json;
	private Utils utils;

	// Declare variables to store data
	private String mGetDealId;
	private String mCompany;
	private String mTitle;
	private String mAddress;
	private String mDesc;
	private String mAfterDiscount;
	private String mStartValue;
	private String mDiscount;
	private String mSave;
	private String mDateStart;
	private String mDateEnd;
	private String mUrl;
	private String mImgDeal;
	private String mIcMarker;
	private Double mDblLatitude;
	private Double mDblLongitude;

	final String mimeType = "text/html";
	final String encoding = "UTF-8";

	// Declare view objects
	private TextView lblCompany, lblTitle, lblAddress, lblAfterDiscount,
			lblStartValue, lblDiscount, lblSave, lblDateStart, lblDateEnd;
	private ImageView imgThumbnail;
	private LinearLayout lytMedia;
	private RelativeLayout lytDetail, lytDesc;
	private Button btnGet;
	private WebView webDesc;

	private ProgressBar prgStepLoading;

	// Declare view objects
	private TextView lblNoResult, lblAlert;
	private Button btnRetry;
	private LinearLayout lytRetry;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_detail, menu);
		return true;
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_detail);

		// Declare object of userFunction and utils class
		userFunction = new UserFunctions();
		utils = new Utils(this);

		// Get intent Data from ActivityHome, ActivityPlaceAroundYou,
		// ActivityPlaceList OR ActivitySearch
		Intent i = getIntent();
		mGetDealId = i.getStringExtra(utils.EXTRA_DEAL_ID);

		// Get ActionBar and set back private Button on actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);

		// Connect view objects and xml ids
		lblCompany = (TextView) findViewById(R.id.lblCompany);
		lblTitle = (TextView) findViewById(R.id.lblTitle);
		lblAddress = (TextView) findViewById(R.id.lblAddress);
		lblAfterDiscount = (TextView) findViewById(R.id.lblAfterDiscount);
		lblStartValue = (TextView) findViewById(R.id.lblStartValue);
		lblDiscount = (TextView) findViewById(R.id.lblDiscountValue);
		lblSave = (TextView) findViewById(R.id.lblSaveValue);
		//lblDateStart = (TextView) findViewById(R.id.lblStartDateValue);
		//lblDateEnd = (TextView) findViewById(R.id.lblEndDateValue);
		lytMedia = (LinearLayout) findViewById(R.id.lytMedia);
		lytDetail = (RelativeLayout) findViewById(R.id.lytDetail);
		btnGet = (Button) findViewById(R.id.btnGet);
		imgThumbnail = (ImageView) findViewById(R.id.imgThumbnail);
		webDesc = (WebView) findViewById(R.id.webDesc);
		prgStepLoading = (ProgressBar) findViewById(R.id.prgLoading);
		lblNoResult = (TextView) findViewById(R.id.lblNoResult);
		lblAlert = (TextView) findViewById(R.id.lblAlert);
		lytDesc = (RelativeLayout) findViewById(R.id.lytDesc);
		lytRetry = (LinearLayout) findViewById(R.id.lytRetry);
		btnRetry = (Button) findViewById(R.id.btnRetry);

		btnRetry.setOnClickListener(this);
		btnGet.setOnClickListener(this);

		// Change cover image width and height
		LinearLayout.LayoutParams lp = new LinearLayout.LayoutParams(
				utils.loadPreferences(utils.PARAM_WIDTH_PIX),
				(((utils.loadPreferences(utils.PARAM_WIDTH_PIX)) * 9) / 16));

		lytMedia.setLayoutParams(lp);

		/*
		 * CHECK_PLAY_SERV = 1 means Google Play services version on the device
		 * supports the version of the client library you are using
		 */
		if (utils.loadPreferences(utils.CHECK_PLAY_SERV) == 1) {
			// Check the connection
			if (utils.isNetworkAvailable()) {

				// Call asynctask class
				new getDataAsync().execute();

				// Condition for admob (0=gone, 1=visible)
			} else {
				lblNoResult.setVisibility(View.GONE);
				lytRetry.setVisibility(View.VISIBLE);
				lblAlert.setText(R.string.no_connection);
			}
		}

	}

	// AsyncTask to Get Data from Server and put it on View Object
	public class getDataAsync extends AsyncTask<Void, Void, Void> {

		ProgressDialog progress;

		@Override
		protected void onPreExecute() {
			// TODO Auto-generated method stub

			// Show progress dialog when fetching data from database
			progress = ProgressDialog.show(ActivityDetail.this, "",
					getString(R.string.loading_data), true);

		}

		@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub

			// Method to get Data from Server
			getDataFromServer();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub

			if (json != null) {
				// Display Data
				lytDetail.setVisibility(View.VISIBLE);
				lytRetry.setVisibility(View.GONE);
				lblCompany.setText(mCompany + ",");
				lblTitle.setText(mTitle);
				lblAddress.setText(mAddress);
				lblAfterDiscount.setText(mAfterDiscount + Utils.mCurrency);
				lblStartValue.setText(mStartValue + Utils.mCurrency);
				lblDiscount.setText(mDiscount + "%");
				lblSave.setText(mSave + Utils.mCurrency);
				//lblDateStart.setText(" " + mDateStart);
				//lblDateEnd.setText(" " + mDateEnd);

				// Load data from url
				webViewStep();

				// Set Image thumbnail from Server with picasso
				Picasso.with(getApplicationContext())
						.load(userFunction.URLAdmin + mImgDeal).fit()
						.centerCrop().tag(getApplicationContext())
						.into(imgThumbnail);

			} else {
				lytDetail.setVisibility(View.GONE);
				lytRetry.setVisibility(View.VISIBLE);
				Toast.makeText(ActivityDetail.this,
						getString(R.string.no_connection), Toast.LENGTH_SHORT)
						.show();

			}
			if (progress.isShowing()) {
				progress.dismiss();
			}

		}

	}

	// Method to get Data from Server
	public void getDataFromServer() {

		try {
			json = userFunction.dealDetail(mGetDealId);
			if (json != null) {
				JSONArray dataDealsArray = json
						.getJSONArray(userFunction.array_deal_detail);

				JSONObject dealsObject = dataDealsArray.getJSONObject(0);

				// Store Data to Variables
				mCompany = dealsObject
						.getString(userFunction.key_deals_company);
				mTitle = dealsObject.getString(userFunction.key_deals_title);
				mAddress = dealsObject
						.getString(userFunction.key_deals_address);
				mAfterDiscount = dealsObject
						.getString(userFunction.key_deals_after_disc_value);
				mStartValue = dealsObject
						.getString(userFunction.key_deals_start_value);
				mDiscount = dealsObject.getString(userFunction.key_deals_disc);
				mSave = dealsObject.getString(userFunction.key_deals_save);
				mDateStart = dealsObject
						.getString(userFunction.key_deals_date_start);
				mDateEnd = dealsObject
						.getString(userFunction.key_deals_date_end);
				mDesc = dealsObject.getString(userFunction.key_deals_desc);
				mUrl = dealsObject.getString(userFunction.key_deals_url);
				mImgDeal = dealsObject.getString(userFunction.key_deals_image);
				mIcMarker = dealsObject
						.getString(userFunction.key_category_marker);
				mDblLatitude = dealsObject
						.getDouble(userFunction.key_deals_lat);
				mDblLongitude = dealsObject
						.getDouble(userFunction.key_deals_lng);

			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i("ActivityDetailPlace", "getDataFromServer: " + e);
		}
	}

	// Listener for option menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		Intent i;
		switch (item.getItemId()) {
		case android.R.id.home:
			// Previous page or exit
			finish();
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;
		case R.id.abDirection:
			// Call ActivityPlaceAroundYou
			i = new Intent(this, ActivityDirection.class);
			i.putExtra(utils.EXTRA_DEST_LAT, mDblLatitude);
			i.putExtra(utils.EXTRA_DEST_LNG, mDblLongitude);
			i.putExtra(utils.EXTRA_CATEGORY_MARKER, mIcMarker);
			startActivity(i);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
			break;

		case R.id.abShare:
			i = new Intent(this, ActivityShare.class);
			i.putExtra(utils.EXTRA_DEAL_ID, mGetDealId);
			i.putExtra(utils.EXTRA_DEAL_TITLE, mTitle);
			i.putExtra(utils.EXTRA_DEAL_DESC, mDesc);
			i.putExtra(utils.EXTRA_DEAL_IMG, mImgDeal);
			startActivity(i);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
			break;

		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	// Method handle load webview
	private void webViewStep() {
		lytDesc.setVisibility(View.VISIBLE);
		webDesc.loadUrl(userFunction.varLoadURL + mGetDealId);
		final Activity act = ActivityDetail.this;
		webDesc.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView webview, int progress) {

				act.setProgress(progress * 100);
				prgStepLoading.setProgress(progress);

			}

		});

		webDesc.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				super.onPageStarted(webDesc, url, favicon);
				prgStepLoading.setVisibility(View.VISIBLE);

			}

			@Override
			public void onPageFinished(WebView view, String url) {

				super.onPageFinished(webDesc, url);

				prgStepLoading.setProgress(0);
				prgStepLoading.setVisibility(View.GONE);

			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {

				view.stopLoading(); // may not be needed
				view.loadData(utils.timeoutMessageHtml, "text/html", "utf-8");
			}

		});
	}

	// Listener for on click
	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.btnGet:
			Intent i;
			// Open ActivityBrowser
			i = new Intent(this, ActivityBrowser.class);
			i.putExtra(utils.EXTRA_DEAL_URL, mUrl);
			i.putExtra(utils.EXTRA_DEAL_TITLE, mTitle);
			startActivity(i);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);

		case R.id.btnRetry:
			// Retry to get Data
			if (utils.isNetworkAvailable()) {
				json = null;
				new getDataAsync().execute();
			} else {
				lblNoResult.setVisibility(View.GONE);
				lytRetry.setVisibility(View.VISIBLE);
				lblAlert.setText(R.string.no_connection);
			}
			break;

		default:
			break;
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);

	}

}