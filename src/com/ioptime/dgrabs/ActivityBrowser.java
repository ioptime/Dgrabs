/**
 * File        : ActivityBrowser.java
 * App name    : Goceng
 * Version     : 1.2.0
 * Created     : 05/12/14

 * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
 * Copyright (c) 2014 pongodev. All rights reserved.
 */

package com.ioptime.dgrabs;

import android.annotation.SuppressLint;

import android.app.Activity;
import android.content.Intent;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.text.Html;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.Window;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import com.ioptime.dgrabs.utils.Utils;

public class ActivityBrowser extends ActionBarActivity implements
		OnClickListener {

	// Create an instance of ActionBar
	private ActionBar actionbar;

	private WebView web;
	private ProgressBar prgPageLoading;
	private String mGetDealTitle;
	private String url;

	// Declare object of Utils and UserFunctions class
	private Utils utils;

	// Declare view objects
	private Button btnRetry;
	private LinearLayout lytRetry;
	private TextView lblAlert;

	private MenuItem miPrev, miNext;

	@Override
	public boolean onCreateOptionsMenu(Menu menu) {
		getMenuInflater().inflate(R.menu.actionbar_browser, menu);
		miPrev = (MenuItem) menu.findItem(R.id.abPrevious);
		miNext = (MenuItem) menu.findItem(R.id.abNext);

		return true;
	}

	/** Called when the activity is first created. */
	@SuppressLint("SetJavaScriptEnabled")
	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_PROGRESS);
		setContentView(R.layout.activity_browser);

		// Declare object of Utils and userFunction class
		utils = new Utils(this);

		// Get intent Data from ActivityDetail
		Intent i = getIntent();

		url = i.getStringExtra(utils.EXTRA_DEAL_URL);
		mGetDealTitle = i.getStringExtra(utils.EXTRA_DEAL_TITLE);

		// Get ActionBar and set back private Button on actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);
		actionbar.setTitle(Html.fromHtml(mGetDealTitle));

		// Connecct view object and xml ids
		web = (WebView) findViewById(R.id.web);
		lytRetry = (LinearLayout) findViewById(R.id.lytRetry);
		btnRetry = (Button) findViewById(R.id.btnRetry);
		lblAlert = (TextView) findViewById(R.id.lblAlert);
		btnRetry.setOnClickListener(this);

		prgPageLoading = (ProgressBar) findViewById(R.id.prgPageLoading);

		web.setHorizontalScrollBarEnabled(true);
		web.getSettings().setAllowFileAccess(true);
		web.getSettings().setJavaScriptEnabled(true);
		setProgressBarVisibility(true);

		web.getSettings().setBuiltInZoomControls(true);
		web.getSettings().setUseWideViewPort(true);
		web.setInitialScale(1);

		// Check the connection
		if (utils.isNetworkAvailable()) {
			webViewSocial();
		} else {
			lytRetry.setVisibility(View.VISIBLE);
			lblAlert.setText(R.string.no_connection);
		}

	}

	private void webViewSocial() {
		web.loadUrl(url);

		final Activity act = this;

		// Setting loading when data request to server
		web.setWebChromeClient(new WebChromeClient() {
			public void onProgressChanged(WebView webview, int progress) {

				act.setProgress(progress * 100);
				prgPageLoading.setProgress(progress);

			}

		});

		web.setWebViewClient(new WebViewClient() {
			@Override
			public void onPageStarted(WebView view, String url, Bitmap favicon) {

				super.onPageStarted(web, url, favicon);
				prgPageLoading.setVisibility(View.VISIBLE);

			}

			@Override
			public void onPageFinished(WebView view, String url) {

				super.onPageFinished(web, url);

				prgPageLoading.setProgress(0);
				prgPageLoading.setVisibility(View.GONE);

				if (web.canGoBack()) {
					miPrev.setEnabled(true);
					miPrev.setIcon(R.drawable.ic_action_previous_item);
				} else {
					miPrev.setEnabled(false);
					miPrev.setIcon(R.drawable.ic_action_previous_item_disabled);
				}

				if (web.canGoForward()) {
					miNext.setEnabled(true);
					miNext.setIcon(R.drawable.ic_action_next_item);
				} else {
					miNext.setEnabled(false);
					miNext.setIcon(R.drawable.ic_action_next_item_disabled);
				}
			}

			public void onReceivedError(WebView view, int errorCode,
					String description, String failingUrl) {
				view.stopLoading(); // may not be needed
				view.loadData(utils.timeoutMessageHtml, "text/html", "utf-8");
			}

		});
	}

	// Listener when item selected Menu in action bar
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		// Handle presses on the action bar items
		switch (item.getItemId()) {
		case android.R.id.home:
			// Previous page or exit
			finish();
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;

		case R.id.abPrevious:
			if (web.canGoBack()) {
				web.goBack();
			}
			break;
		case R.id.abNext:
			if (web.canGoForward()) {
				web.goForward();
			}
			break;
		case R.id.abRefresh:
			web.reload();
			break;
		case R.id.abStop:
			web.stopLoading();
			break;
		case R.id.abBrowser:
			Intent iBrowser = new Intent(Intent.ACTION_VIEW);
			iBrowser.setData(Uri.parse(url));
			startActivity(iBrowser);
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
			break;
		default:
			return super.onOptionsItemSelected(item);
		}
		return true;
	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnRetry:
			if (utils.isNetworkAvailable()) {
				// Load data from url
				lytRetry.setVisibility(View.GONE);
				webViewSocial();
			} else {
				lytRetry.setVisibility(View.VISIBLE);
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