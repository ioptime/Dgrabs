/**
 * File        : ActivityShare.java
 * App name    : Goceng
 * Version     : 1.2.0
 * Created     : 05/12/14

 * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 25/01/14.
 * Copyright (c) 2014 pongodev. All rights reserved.
 */

package com.ioptime.dgrabs;

import android.annotation.SuppressLint;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.support.v7.app.ActionBar;
import android.support.v7.app.ActionBarActivity;
import android.util.Log;
import android.view.MenuItem;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.FacebookRequestError;
import com.facebook.HttpMethod;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphObject;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.ioptime.dgrabs.libraries.UserFunctions;
import com.ioptime.dgrabs.twitter.TwitterApp;
import com.ioptime.dgrabs.twitter.TwitterApp.TwDialogListener;
import com.ioptime.dgrabs.utils.Utils;

@SuppressLint("HandlerLeak")
public class ActivityShare extends ActionBarActivity implements OnClickListener {

	// Create an instance of ActionBar, Adview, TwitterApp, ProgressDialog,
	// Utils, UserFuntions
	private ActionBar actionbar;
	private TwitterApp mTwitter;
	private ProgressDialog mProgress;
	private Utils utils;
	private UserFunctions userFunction;

	// Create object of views
	private EditText txtWhatDoYouThink;
	private CheckBox chkFacebook, chkTwitter;
	private TextView lblFacebook, lblTwitter, lblShareTo;
	private Button btnOtherApps, btnShare;

	// Declare variables to store data
	private String mDealId, mTitle, mImgDeal;

	private String mAppName, mReview, URLLocationPage;
	private String facebookName = "";
	private boolean mConnection;
	private boolean postToTwitter = false;
	private boolean canPresentShareDialog;
	private static final String PERMISSION = "publish_actions";
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

	private PendingAction pendingAction = PendingAction.NONE;

	private enum PendingAction {
		NONE, POST_PHOTO, POST_STATUS_UPDATE
	}

	private UiLifecycleHelper uiHelper;

	private Session.StatusCallback callback = new Session.StatusCallback() {
		@Override
		public void call(Session session, SessionState state,
				Exception exception) {
			onSessionStateChange(session, state, exception);
		}
	};

	private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
		@Override
		public void onError(FacebookDialog.PendingCall pendingCall,
				Exception error, Bundle data) {
			Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
		}

		@Override
		public void onComplete(FacebookDialog.PendingCall pendingCall,
				Bundle data) {
			Log.d("HelloFacebook", "Success!");
		}
	};

	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		uiHelper = new UiLifecycleHelper(this, callback);
		uiHelper.onCreate(savedInstanceState);

		if (savedInstanceState != null) {
			String name = savedInstanceState
					.getString(PENDING_ACTION_BUNDLE_KEY);
			pendingAction = PendingAction.valueOf(name);
		}

		setContentView(R.layout.activity_share);

		// Declare object of Utils and UserFunctions class
		utils = new Utils(this);
		userFunction = new UserFunctions();

		// Get ActionBar and set back private Button on actionbar
		actionbar = getSupportActionBar();
		actionbar.setDisplayHomeAsUpEnabled(true);

		// Connect view objects and xml ids
		txtWhatDoYouThink = (EditText) findViewById(R.id.txtWhatDoYouThink);
		chkFacebook = (CheckBox) findViewById(R.id.chkFacebook);
		chkTwitter = (CheckBox) findViewById(R.id.chkTwitter);
		lblFacebook = (TextView) findViewById(R.id.lblFacebook);
		lblTwitter = (TextView) findViewById(R.id.lblTwitter);
		lblShareTo = (TextView) findViewById(R.id.lblShareTo);
		btnOtherApps = (Button) findViewById(R.id.btnOtherApps);
		btnShare = (Button) findViewById(R.id.btnShare);

		chkFacebook.setOnClickListener(this);
		chkTwitter.setOnClickListener(this);
		btnOtherApps.setOnClickListener(this);
		btnShare.setOnClickListener(this);

		// If paramSocialMedia == 1 it means using social media
		int paramSocialMedia = Utils.paramSocialMedia;
		if (paramSocialMedia == 0) {
			lblFacebook.setVisibility(View.GONE);
			lblTwitter.setVisibility(View.GONE);
			lblShareTo.setVisibility(View.GONE);
			chkFacebook.setVisibility(View.GONE);
			chkTwitter.setVisibility(View.GONE);
		}

		// Get intent Data from ActivityDetailPlace
		Intent i = getIntent();
		mDealId = i.getStringExtra(utils.EXTRA_DEAL_ID);
		mTitle = i.getStringExtra(utils.EXTRA_DEAL_TITLE);
		mImgDeal = i.getStringExtra(utils.EXTRA_DEAL_IMG);

		URLLocationPage = userFunction.URLAdmin
				+ userFunction.service_view_deal + userFunction.key_deals_id
				+ "=" + mDealId;
		mAppName = getString(R.string.app_name);

		mProgress = new ProgressDialog(this);
		mTwitter = new TwitterApp(this,
				getString(R.string.twitter_consumer_key),
				getString(R.string.twitter_secret_key));

		// Can we present the share dialog for regular links?
		canPresentShareDialog = FacebookDialog.canPresentShareDialog(this,
				FacebookDialog.ShareDialogFeature.SHARE_DIALOG);

		// Checking twitter token
		mTwitter.setListener(mTwLoginDialogListener);

		if (mTwitter.hasAccessToken()) {
			chkTwitter.setChecked(true);

		}

		/*
		 * CHECK_PLAY_SERV = 1 means Google Play services version on the device
		 * supports the version of the client library you are using
		 */
		if (utils.loadPreferences(utils.CHECK_PLAY_SERV) == 1) {

			// Check the connection
			if (utils.isNetworkAvailable()) {
				// Condition for admob (0=gone, 1=visible)
				btnShare.setVisibility(View.VISIBLE);
				// load ads
			}
		}

	}

	/** Start: Twitter */
	// Check the value of review, if more than 110 show toast message
	private void postReview(String review) {
		Toast.makeText(this, getString(R.string.post_review),
				Toast.LENGTH_SHORT).show();

		if (review.length() > 110) {
			Toast.makeText(this, getString(R.string.post_limit),
					Toast.LENGTH_SHORT).show();
		} else {
			postToTwitter = true;
		}
	}

	// Method to check if twitter token not available, authorize user
	private void onTwitterClick() {
		if (!mTwitter.hasAccessToken()) {
			chkTwitter.setChecked(false);
			mTwitter.authorize();
		}
	}

	// Method to post status to twitter
	private void postToTwitter(final String review) {
		new Thread() {
			@Override
			public void run() {
				int what = 0;

				try {
					mTwitter.updateStatus(review + URLLocationPage);

				} catch (Exception e) {
					what = 1;
				}

				mHandlerTwitter
						.sendMessage(mHandlerTwitter.obtainMessage(what));
			}
		}.start();
	}

	// Event handler for reading twitter posting result
	private Handler mHandlerTwitter = new Handler() {
		@Override
		public void handleMessage(Message msg) {
			String text = (msg.what == 0) ? getString(R.string.post_to_twitter)
					: getString(R.string.post_to_twitter_failed);

			Toast.makeText(ActivityShare.this, text, Toast.LENGTH_SHORT).show();

			if (!(chkFacebook.isChecked() && chkTwitter.isChecked())) {
				finish();

				// Show transition when finish
				overridePendingTransition(R.anim.open_main, R.anim.close_next);
			}
		}
	};

	// Event listener to read twitter username from twitter dialog
	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {

		public void onComplete(String value) {
			String username = mTwitter.getUsername();
			username = (username.equals("")) ? getString(R.string.no_name)
					: username;
			// bro
			utils.saveString(utils.TWITTER_NAME, username);
			chkTwitter.setChecked(true);

			Toast.makeText(ActivityShare.this,
					getString(R.string.connect_twitter) + " " + username,
					Toast.LENGTH_LONG).show();
		}

		public void onError(String value) {
			chkTwitter.setChecked(false);

			Toast.makeText(ActivityShare.this,
					getString(R.string.connect_twitter_failed),
					Toast.LENGTH_LONG).show();
		}
	};

	/** End: Twitter */

	/** Start: facebook */
	private void openFacebookSession() {
		Session.openActiveSession(this, true, new Session.StatusCallback() {
			@Override
			public void call(Session session, SessionState state,
					Exception exception) {
				final Session s = session;
				if (session.isOpened()) {
					Request request = Request.newMeRequest(session,
							new Request.GraphUserCallback() {

								@Override
								public void onCompleted(GraphUser user,
										Response response) {
									// TODO Auto-generated method stub
									if (s == Session.getActiveSession()) {
										if (user != null) {
											facebookName = user.getName();
										}
									}
									if (response.getError() != null) {
										// Handle errors, will do so later.
									}
								}
							});
					request.executeAsync();
				}

				if (exception != null) {
					Log.d("ActivityShare: Facebook", exception.getMessage());
					utils.saveString(utils.FACEBOOK_NAME, facebookName);
				}

			}
		});
	}

	@Override
	protected void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		uiHelper.onSaveInstanceState(outState);

		outState.putString(PENDING_ACTION_BUNDLE_KEY, pendingAction.name());
	}

	@Override
	protected void onActivityResult(int requestCode, int resultCode, Intent data) {
		super.onActivityResult(requestCode, resultCode, data);
		uiHelper.onActivityResult(requestCode, resultCode, data, dialogCallback);
	}

	private void onSessionStateChange(Session session, SessionState state,
			Exception exception) {
		if (pendingAction != PendingAction.NONE
				&& (exception instanceof FacebookOperationCanceledException || exception instanceof FacebookAuthorizationException)) {
			new AlertDialog.Builder(ActivityShare.this)
					.setTitle(getString(R.string.msgbox_title_cancel))
					.setMessage(getString(R.string.msgbox_message_not_granted))
					.setPositiveButton(getString(R.string.msgbox_button_ok),
							null).show();
			pendingAction = PendingAction.NONE;
		} else if (state == SessionState.OPENED_TOKEN_UPDATED) {
			handlePendingAction();
		}
		updateUI();
	}

	private void updateUI() {
		Session session = Session.getActiveSession();
		boolean enableButtons = (session != null && session.isOpened());

		if (enableButtons) {
			Request.newMeRequest(session, new GraphUserCallback() {

				@Override
				public void onCompleted(GraphUser user, Response response) {
					// TODO Auto-generated method stub
					if (response != null) {
						try {
							facebookName = user.getName();
							utils.saveString(utils.FACEBOOK_NAME, facebookName);
						} catch (Exception e) {
							e.printStackTrace();
						}
					}
				}
			}).executeAsync();

			chkFacebook.setChecked(true);

		} else {
			chkFacebook.setChecked(false);

		}
	}

	@SuppressWarnings("incomplete-switch")
	private void handlePendingAction() {
		PendingAction previouslyPendingAction = pendingAction;
		// These actions may re-set pendingAction if they are still pending, but
		// we assume they
		// will succeed.
		pendingAction = PendingAction.NONE;

		switch (previouslyPendingAction) {
		case POST_STATUS_UPDATE:
			postStatusUpdate();
			break;
		}
	}

	private void onClickPostStatusUpdate() {
		performPublish(PendingAction.POST_STATUS_UPDATE, canPresentShareDialog);
	}

	private void postStatusUpdate() {

		if (canPresentShareDialog) {
			FacebookDialog shareDialog = createShareDialogBuilderForLink()
					.build();
			uiHelper.trackPendingDialogCall(shareDialog.present());
		} else if (hasPublishPermission()) {
			mProgress.setMessage("Posting ...");
			mProgress.show();

			Bundle params = new Bundle();
			params.putString("caption", mAppName);
			params.putString("message", mReview);
			params.putString("link", URLLocationPage);
			params.putString("picture", userFunction.URLAdmin + mImgDeal);

			Request request = new Request(Session.getActiveSession(),
					"me/feed", params, HttpMethod.POST);
			request.setCallback(new Request.Callback() {
				@Override
				public void onCompleted(Response response) {
					showPublishResult(mReview, response.getGraphObject(),
							response.getError());
				}
			});
			request.executeAsync();

		} else {
			pendingAction = PendingAction.POST_STATUS_UPDATE;
		}
	}

	private interface GraphObjectWithId extends GraphObject {
		String getId();
	}

	private void showPublishResult(String message, GraphObject result,
			FacebookRequestError error) {
		String alertMessage = null;
		if (error == null) {
			String id = result.cast(GraphObjectWithId.class).getId();
			alertMessage = getString(R.string.alert_success_post_facebook,
					message, id);
		} else {
			alertMessage = error.getErrorMessage();
		}
		mProgress.cancel();

		Toast.makeText(ActivityShare.this, "facebook: " + alertMessage,
				Toast.LENGTH_SHORT).show();
		finish();

		// Show transition when push back private Button in actionbar
		overridePendingTransition(R.anim.open_main, R.anim.close_next);
	}

	private FacebookDialog.ShareDialogBuilder createShareDialogBuilderForLink() {
		return new FacebookDialog.ShareDialogBuilder(this)
				.setName("Hello Facebook")
				.setDescription(
						"The 'Hello Facebook' sample application showcases simple Facebook integration")
				.setLink("http://developers.facebook.com/android");
	}

	private void performPublish(PendingAction action, boolean allowNoSession) {
		Session session = Session.getActiveSession();
		if (session != null) {
			pendingAction = action;
			if (hasPublishPermission()) {
				// We can do the action right away.
				handlePendingAction();
				return;
			} else if (session.isOpened()) {
				// We need to get new permissions, then complete the action when
				// we get called back.
				session.requestNewPublishPermissions(new Session.NewPermissionsRequest(
						this, PERMISSION));
				return;
			}
		}

		if (allowNoSession) {
			pendingAction = action;
			handlePendingAction();
		}
	}

	private boolean hasPublishPermission() {
		Session session = Session.getActiveSession();
		return session != null
				&& session.getPermissions().contains("publish_actions");
	}

	/** End: Facebook */

	// Listener for option menu
	@Override
	public boolean onOptionsItemSelected(MenuItem item) {
		switch (item.getItemId()) {
		case android.R.id.home:
			// previous page or exit
			finish();

			// Show transition when push back private Button in actionbar
			overridePendingTransition(R.anim.open_main, R.anim.close_next);
			return true;
		default:
			return super.onOptionsItemSelected(item);
		}
	}

	// Listener on Click
	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnShare:
			mConnection = utils.isNetworkAvailable();
			if (mConnection) {
				// Get text from edittext and store to string variable
				mReview = txtWhatDoYouThink.getText().toString();

				// Check to value of review
				if (mReview.equals("")) {
					mReview = mTitle + " at " + getString(R.string.app_name)
							+ " ";
				} else {
					mReview += " - " + mTitle + " at "
							+ getString(R.string.app_name) + " ";
				}

				// Check the checkbox and post to both facebook and twitter or
				// one of them
				if (chkFacebook.isChecked() && chkTwitter.isChecked()) {
					postReview(mReview);
					if (postToTwitter) {
						onClickPostStatusUpdate();
						postToTwitter(mReview);
					}
				} else if (chkFacebook.isChecked() && !chkTwitter.isChecked()) {
					onClickPostStatusUpdate();
				} else if (!chkFacebook.isChecked() && chkTwitter.isChecked()) {
					postReview(mReview);
					if (postToTwitter)
						postToTwitter(mReview);
				} else {
					Toast.makeText(ActivityShare.this,
							getString(R.string.select_share),
							Toast.LENGTH_SHORT).show();
				}

			} else {
				Toast.makeText(getApplicationContext(), R.string.no_connection,
						Toast.LENGTH_SHORT).show();
			}
			break;

		case R.id.btnOtherApps:
			// Share this app to other application
			Intent iShare = new Intent(Intent.ACTION_SEND);
			iShare.setType("text/plain");
			iShare.putExtra(Intent.EXTRA_SUBJECT, mTitle);
			iShare.putExtra(Intent.EXTRA_TEXT, "\nDetail: " + URLLocationPage);
			startActivity(Intent.createChooser(iShare,
					getString(R.string.share_via)));

			// Show transition when private Button pressed
			overridePendingTransition(R.anim.open_next, R.anim.close_main);
			break;

		case R.id.chkFacebook:
			// When checkbox facebook click
			if (chkFacebook.isChecked()) {
				openFacebookSession();
				handlePendingAction();
			}
			break;

		case R.id.chkTwitter:
			// When checkbox twitter click
			onTwitterClick();
			break;
		default:
			break;
		}

	}

	/** Start: Activity lifecycle */
	@Override
	protected void onResume() {
		super.onResume();
		uiHelper.onResume();

		// Call the 'activateApp' method to log an app event for use in
		// analytics and advertising reporting. Do so in
		// the onResume methods of the primary Activities that an app may be
		// launched into.
		AppEventsLogger.activateApp(this);

		updateUI();
	}

	@Override
	public void onPause() {
		super.onPause();
		uiHelper.onPause();

		// Call the 'deactivateApp' method to log an app event for use in
		// analytics and advertising
		// reporting. Do so in the onPause methods of the primary Activities
		// that an app may be launched into.
		//AppEventsLogger.deactivateApp(this);
	}

	@Override
	public void onDestroy() {
		super.onDestroy();
		uiHelper.onDestroy();
	}

	/** End: Activity lifecycle */

	// Listener when back private Button pressed
	@Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();

		// Show transition when back private Button pressed
		overridePendingTransition(R.anim.open_main, R.anim.close_next);

	}

}
