/**
    * File        : ActivitySplash.java
    * App name    : Goceng
    * Version     : 1.2.0
    * Created     : 05/12/14

    * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
    * Copyright (c) 2014 pongodev. All rights reserved.
    */

package com.ioptime.dgrabs;

 

import android.app.Activity;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Bundle;
import android.widget.ProgressBar;

public class ActivitySplash extends Activity {

	// Create an instance of ProgressBar
	private ProgressBar prgLoading;
		
	private int progress = 0;
    /** Called when the activity is first created. */
	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.activity_splash);

		prgLoading = (ProgressBar) findViewById(R.id.prgLoading);
    	prgLoading.setProgress(progress);

    	// Run progressbar via asynctask
		new Loading().execute();
	}
	
	public class Loading extends AsyncTask<Void, Void, Void>{
    	@Override
		 protected void onPreExecute() {}
    	
		@Override
		protected Void doInBackground(Void... arg0) {
			// TODO Auto-generated method stub
			
			// Create progress bar loading
			while(progress < 100){
				try {
					Thread.sleep(1000);
					progress += 30;
					prgLoading.setProgress(progress);
				} catch (InterruptedException e) {
					// TODO Auto-generated catch block
					e.printStackTrace();
				}
				
			}
			return null;
		}
    	
		@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
			
			// When progressbar finish call HomeActivity class
			Intent i = new Intent(ActivitySplash.this, ActivityHome.class);
			startActivity(i);
			overridePendingTransition (R.anim.open_next, R.anim.close_main);
		}
    }

}
