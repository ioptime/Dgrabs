/**
 * File        : ActivityCategory.java
 * App name    : Goceng
 * Version     : 1.2.0
 * Created     : 05/12/14

 * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
 * Copyright (c) 2014 pongodev. All rights reserved.
 */

package com.ioptime.dgrabs;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Toast;

import com.ioptime.dgrabs.fragments.FragmentCategory;
import com.ioptime.dgrabs.utils.Utils;

public class ActivityCategory extends ActionBarActivity implements
		FragmentCategory.OnCategoryListSelectedListener {

	// Create an instance of ActionBar
	private ActionBar actionbar;

	// Declare object of AdView class

	// Declare object of Utils class
	private Utils utils;

	protected Fragment mFrag;

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_list);

		// Declare object of Utils class
		utils = new Utils(this);

		// connect view objects and xml ids

		// Get ActionBar and set back private Button on actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);

		FragmentTransaction t = this.getSupportFragmentManager()
				.beginTransaction();
		mFrag = new FragmentCategory();
		t.replace(R.id.frame_content, mFrag);
		t.commit();

		/*
		 * CHECK_PLAY_SERV = 1 means Google Play services version on the device
		 * supports the version of the client library you are using
		 */
		if (utils.loadPreferences(utils.CHECK_PLAY_SERV) == 1) {

			// Check the connection
			if (utils.isNetworkAvailable()) {
				// Condition for admob (0=gone, 1=visible)
			} else {
				Toast.makeText(this, getString(R.string.internet_alert),
						Toast.LENGTH_SHORT).show();
			}
		}

	}

	@Override
	public void onCategoryListSelected(String mCategoryId, String mCategoryName) {
		// TODO Auto-generated method stub

		// Call ActivityCategoryList
		Intent i = new Intent(this, ActivityCategoryList.class);
		i.putExtra(utils.EXTRA_CATEGORY_ID, mCategoryId);
		i.putExtra(utils.EXTRA_CATEGORY_NAME, mCategoryName);

		startActivity(i);
		overridePendingTransition(R.anim.open_next, R.anim.close_main);
	}

	// Listener for option menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:

			// Previous page or exit
			finish();
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;

		default:
			return super.onOptionsItemSelected(item);
		}
	}

	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		overridePendingTransition(R.anim.open_main, R.anim.close_next);

	}

}
