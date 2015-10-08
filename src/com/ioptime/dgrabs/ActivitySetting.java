/**
    * File        : ActivitySetting.java
    * App name    : Goceng
    * Version     : 1.2.0
    * Created     : 05/12/14

    * Created by Taufan Erfiyanto & Cahaya Pangripta Alam on 21/01/14.
    * Copyright (c) 2014 pongodev. All rights reserved.
    */

package com.ioptime.dgrabs;

import android.app.ActionBar;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.CheckBoxPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.Preference.OnPreferenceChangeListener;
import android.preference.Preference.OnPreferenceClickListener;
import android.preference.PreferenceActivity;
import android.util.Log;
import android.view.MenuItem;
import android.widget.Toast;

import com.facebook.AppEventsLogger;
import com.facebook.FacebookAuthorizationException;
import com.facebook.FacebookOperationCanceledException;
import com.facebook.Request;
import com.facebook.Request.GraphUserCallback;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.FacebookDialog;
import com.ioptime.dgrabs.twitter.TwitterApp;
import com.ioptime.dgrabs.twitter.TwitterApp.TwDialogListener;
import com.ioptime.dgrabs.utils.Utils;
 

public class ActivitySetting extends PreferenceActivity implements OnSharedPreferenceChangeListener, OnPreferenceClickListener{
	
	// Create an instance of ActionBar, Utils, ListPreference, TwitterApp, PendingAction and UiLifecycleHelper
	private ActionBar actionbar;
	private Utils utils;	
	private ListPreference listPreferenceViewType;
	private TwitterApp mTwitter;
	private PendingAction pendingAction = PendingAction.NONE;	
	private UiLifecycleHelper uiHelper;
	
    // To handle social media it using or not
    private int paramSocialMedia; 
    
	// Declare view objects
	private CheckBoxPreference chkFacebook, chkTwitter, chkOverlay;

	private String facebookName = "";
	private String twitterName = "";
	
	private final String PENDING_ACTION_BUNDLE_KEY = "com.facebook.samples.hellofacebook:PendingAction";

    
    
    private enum PendingAction {
        NONE,
        POST_PHOTO,
        POST_STATUS_UPDATE
    }

    private Session.StatusCallback callback = new Session.StatusCallback() {
        @Override
        public void call(Session session, SessionState state, Exception exception) {
            onSessionStateChange(session, state, exception);
        }
    };

    private FacebookDialog.Callback dialogCallback = new FacebookDialog.Callback() {
        @Override
        public void onError(FacebookDialog.PendingCall pendingCall, Exception error, Bundle data) {
            Log.d("HelloFacebook", String.format("Error: %s", error.toString()));
        }

        @Override
        public void onComplete(FacebookDialog.PendingCall pendingCall, Bundle data) {
            Log.d("HelloFacebook", "Success!");
        }
    };

    
    @SuppressWarnings("deprecation")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        
        uiHelper = new UiLifecycleHelper(this, callback);
        uiHelper.onCreate(savedInstanceState);
        
        if (savedInstanceState != null) {
            String name = savedInstanceState.getString(PENDING_ACTION_BUNDLE_KEY);
            pendingAction = PendingAction.valueOf(name);
        }
        
        // Declare object to Utils Class
 		utils = new Utils(this);
 		
		// If paramSocialMedia == 1 it means using social media
 		paramSocialMedia=Utils.paramSocialMedia;     		
        if(paramSocialMedia==1){
        	addPreferencesFromResource(R.xml.setting_preference);
        } else {
        	addPreferencesFromResource(R.xml.setting_preference_no_socialmedia);
        }
        
        // Register for changes
    	getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
        				
     	// Get ActionBar and set back private Button on actionbar
    	actionbar = getActionBar();
     	actionbar.setDisplayHomeAsUpEnabled(true);
		
		// Declare object to Utils Class
		utils = new Utils(this);
		listPreferenceViewType 	= (ListPreference) findPreference(getString(R.string.preferences_type));   
		
        if(listPreferenceViewType.getValue()==null) {
            // to ensure we don't get a null value
            // set first value by default
            listPreferenceViewType.setValueIndex(0);
        }
		
		listPreferenceViewType 	= (ListPreference) findPreference(getString(R.string.preferences_type));   
		
        if(listPreferenceViewType.getValue()==null) {
            // to ensure we don't get a null value
            // set first value by default
            listPreferenceViewType.setValueIndex(0);
        }
		
        listPreferenceViewType.setSummary(listPreferenceViewType.getValue().toString());
        
        // Listener when PreferenceViewType change
        listPreferenceViewType.setOnPreferenceChangeListener(new OnPreferenceChangeListener() {
        	
            @Override
            public boolean onPreferenceChange(Preference preference, Object newValue) {
            	String textValue = newValue.toString();
            	ListPreference listPreference = (ListPreference) preference;
                int index = listPreference.findIndexOfValue(textValue);
                listPreferenceViewType.setDefaultValue(index);
                listPreferenceViewType.setValueIndex(index);
                utils.savePreferences(getString(R.string.preferences_type), index);               
                preference.setSummary(textValue);
                return false;
            }
        });
		   
        if(paramSocialMedia==1){
        	// Connect view objects and xml id
    		chkFacebook = (CheckBoxPreference) findPreference(getString(R.string.preferences_facebook));
    		chkTwitter  = (CheckBoxPreference) findPreference(getString(R.string.preferences_twitter));
    		
    		chkFacebook.setOnPreferenceClickListener(this);

    	    // Set facebook id, and twitter key
    		facebookName = utils.loadString(utils.FACEBOOK_NAME);
    		twitterName = utils.loadString(utils.TWITTER_NAME);
    		
            mTwitter  = new TwitterApp(this, 
            		getString(R.string.twitter_consumer_key),
            		getString(R.string.twitter_secret_key));

            
            // Event listener for twitter
            mTwitter.setListener(mTwLoginDialogListener);
    		
    		if (mTwitter.hasAccessToken()) {
    			chkTwitter.setChecked(true);
    			chkTwitter.setSummary(getString(R.string.connected_social)+" "+twitterName);
    		} else {
    			chkTwitter.setSummary(getString(R.string.connect));
    		}
    		
    		// Event listener to handle checkbox private Button when pressed
    		chkFacebook.setOnPreferenceClickListener(new OnPreferenceClickListener() {			
    			public boolean onPreferenceClick(Preference preference) {
    				// TODO Auto-generated method stub
    				onFacebookClick();
    				return false;
    			}
    		});
    		
    		// Event listener to handle checkbox private Button when pressed
    		chkTwitter.setOnPreferenceClickListener(new OnPreferenceClickListener() {
    			
    			public boolean onPreferenceClick(Preference preference) {
    				// TODO Auto-generated method stub
    				onTwitterClick();
    				return false;
    			}
    		});
        } 
        
		// Check paramter overlays
		int paramOverlay = utils.loadPreferences(utils.UTILS_OVERLAY);
		
        chkOverlay	= (CheckBoxPreference) findPreference(getString(R.string.preferences_overlay));
		if(paramOverlay!=1) {
			chkOverlay.setChecked(true);
		} else {
			chkOverlay.setChecked(false);
		}
			
		chkOverlay.setOnPreferenceClickListener(new OnPreferenceClickListener() {
			
			@Override
			public boolean onPreferenceClick(Preference preference) {
				// TODO Auto-generated method stub
				if(chkOverlay.isChecked()==true){
					utils.savePreferences(utils.UTILS_OVERLAY, 0);
					Toast.makeText(getApplicationContext(), "Overlay Activated", Toast.LENGTH_SHORT).show();
					chkOverlay.setChecked(true);
				} else {
					utils.savePreferences(utils.UTILS_OVERLAY, 1);
					chkOverlay.setChecked(false);
				}
				return false;
			}
		});
	}
    
/** Start: twitter*/    
    // Event listener to read twitter username from twitter dialog
 	private final TwDialogListener mTwLoginDialogListener = new TwDialogListener() {
 		
 		public void onComplete(String value) {
 			String username = mTwitter.getUsername();
 			username		= (username.equals("")) ? getString(R.string.no_name) : username;
 		
 			utils.saveString(utils.TWITTER_NAME, username);
 			
 			chkTwitter.setChecked(true);
 			chkTwitter.setSummary(getString(R.string.connected_social)+" "+username);
 			
 		}
 		
 		public void onError(String value) {
 			chkTwitter.setChecked(false);
 			chkTwitter.setSummary(getString(R.string.connect));
 			
 		}
 	};
 	
 	// Method to check whether twitter token is available or not
 	private void onTwitterClick() {
 		if (mTwitter.hasAccessToken()) {
 			
 			// If available, show alert dialog and confirm to delete twitter account
 			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
 			builder.setTitle(getString(R.string.confirm));
 			builder.setMessage(getString(R.string.delete_twitter_connection))
 			       .setCancelable(false)
 			       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			        	   mTwitter.resetAccessToken();
 			        	   
 			        	   chkTwitter.setChecked(false);
 			        	  chkTwitter.setSummary(getString(R.string.connect));
 			           }
 			       })
 			       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
 			           public void onClick(DialogInterface dialog, int id) {
 			                dialog.cancel();
 			                
 			                chkTwitter.setChecked(true);
 			               chkTwitter.setSummary(getString(R.string.connected_social)+" "+twitterName);
 			           }
 			       });
 			final AlertDialog alert = builder.create();
 			
 			alert.show();
 			
 		} else {
 			// Otherwise, authorize user
 			chkTwitter.setChecked(false);
 			chkTwitter.setSummary(getString(R.string.connect));
 			mTwitter.authorize();
 		}
 	}
 	
/** End: twitter*/	

/**Start: Facebook*/    
	    private void openFacebookSession(){
	        Session.openActiveSession(this, true, new Session.StatusCallback() {
	    	@Override
	    	public void call(Session session, SessionState state, Exception exception) {
	    		final Session s = session;
	    		if (session.isOpened()) {
	               Request request = Request.newMeRequest(session, new Request.GraphUserCallback() {
					
					@Override
					public void onCompleted(GraphUser user, Response response) {
						// TODO Auto-generated method stub
						if (s == Session.getActiveSession()) {
				            if (user != null) {
				                facebookName = user.getName();
								chkFacebook.setChecked(true);
								chkFacebook.setSummary(getString(R.string.connected_social)+" "+facebookName);
				                // you can make request to the /me API or do other stuff like post, etc. here
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
	    	    	Log.i("ActivitySetting: Facebook", exception.getMessage());
					utils.saveString(utils.FACEBOOK_NAME, facebookName);
	    	    }

	    	}
	        });
	    }
	    
	   // Method to check whether twitter token is available or not
	  	private void onFacebookClick() {
	  		if(hasFacebookSession()){
	  			
	  		// If available, show alert dialog and confirm to delete twitter account
	  			final AlertDialog.Builder builder = new AlertDialog.Builder(this);
	  			builder.setTitle(getString(R.string.confirm));
	  			builder.setMessage(getString(R.string.delete_facebook_connection))
	  			       .setCancelable(false)
	  			       .setPositiveButton(getString(R.string.yes), new DialogInterface.OnClickListener() {
	  			           public void onClick(DialogInterface dialog, int id) {

	  			 			logoutFacebook();
	  			           }
	  			       })
	  			       .setNegativeButton(getString(R.string.no), new DialogInterface.OnClickListener() {
	  			           public void onClick(DialogInterface dialog, int id) {
	  			                dialog.cancel();
	  			              chkFacebook.setChecked(true);
	  			                
	  			           }
	  			       });
	  			final AlertDialog alert = builder.create();
	  			
	  			alert.show();
	  			
			}else {

	  			chkFacebook.setChecked(false);
				openFacebookSession();
				handlePendingAction();
	  			
			}
		
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

	   private void onSessionStateChange(Session session, SessionState state, Exception exception) {
	        if (pendingAction != PendingAction.NONE &&
	                (exception instanceof FacebookOperationCanceledException ||
	                exception instanceof FacebookAuthorizationException)) {
	                new AlertDialog.Builder(ActivitySetting.this)
	                    .setTitle(getString(R.string.msgbox_title_cancel))
	                    .setMessage(getString(R.string.msgbox_message_not_granted))
	                    .setPositiveButton((getString(R.string.msgbox_button_ok)), null)
	                    .show();
	            pendingAction = PendingAction.NONE;
	        } else if (state == SessionState.OPENED_TOKEN_UPDATED) {
	            handlePendingAction();
	        }
	        updateUI();
	    }

	    private void updateUI() {
	    	if(paramSocialMedia==1){
	    		Session session = Session.getActiveSession();
	 	        boolean enableButtons = (session != null && session.isOpened());

	 	        if (enableButtons) {
	 	        	Request.newMeRequest(session, new GraphUserCallback() {
	 					
	 					@Override
	 					public void onCompleted(GraphUser user, Response response) {
	 						// TODO Auto-generated method stub
	 						if(response != null){
	 							try{
	 								facebookName = user.getName();
	 								utils.saveString(utils.FACEBOOK_NAME, facebookName);
	 							}catch(Exception e){
	 								e.printStackTrace();
	 							}
	 						}
	 					}
	 				}).executeAsync();
	 	        	
	 	            chkFacebook.setChecked(true);
	 	            String connectedAsUser = getString(R.string.connected_social)+" "+facebookName;
	 	            chkFacebook.setSummary(connectedAsUser);
	 	        }else {
	 	        	chkFacebook.setChecked(false);
	 	        	chkFacebook.setSummary(getString(R.string.connect));
	 	        }
	    	}
	        
	    }

	   @SuppressWarnings("incomplete-switch")
	    private void handlePendingAction() {
	        PendingAction previouslyPendingAction = pendingAction;
	        // These actions may re-set pendingAction if they are still pending, but we assume they
	        // will succeed.
	        pendingAction = PendingAction.NONE;

	        switch (previouslyPendingAction) {
	            case POST_STATUS_UPDATE:
	                //postStatusUpdate();
	                break;
	        }
	    }
	    
		// Facebook logout
		public void logoutFacebook() {
		    Session session = Session.getActiveSession();
		    if (session != null) {
		        session.closeAndClearTokenInformation();
		        chkFacebook.setChecked(false);
		        chkFacebook.setSummary(getString(R.string.connected_social)+" "+facebookName);
			    
		    }
		   
		    Session.setActiveSession(null);

		}
		
		public boolean hasFacebookSession(){
			Session session = Session.getActiveSession();
			boolean enableButtons = (session != null && session.isOpened());
		    if (enableButtons) 
			    return true;
		    else
		    	return false;
		}
/**End: Facebook*/
 
	/**Start: Activity lifecycle*/ 		
		@Override
		protected void onResume() {
		    super.onResume();
		    
	       uiHelper.onResume();

        // Call the 'activateApp' method to log an app event for use in analytics and advertising reporting.  Do so in
        // the onResume methods of the primary Activities that an app may be launched into.
        AppEventsLogger.activateApp(this);

        updateUI();
		}
		
	    @Override
	    public void onPause() {
	        super.onPause();
	        uiHelper.onPause();

	        // Call the 'deactivateApp' method to log an app event for use in analytics and advertising
	        // reporting.  Do so in the onPause methods of the primary Activities that an app may be launched into.
	        //AppEventsLogger.deactivateApp(this);
	    }
	    
	 	@SuppressWarnings("deprecation")
	@Override
	    protected void onDestroy() {
 		// Unregister from changes
 		getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
 		super.onDestroy();
 		uiHelper.onDestroy();
	    }
	 	
	 	@Override
	public boolean onPreferenceClick(Preference preference) {
		// TODO Auto-generated method stub
		
		return false;
	}
/**End: Activity lifecycle*/ 	 	
	 	
 		// Listener when Preference zoom Change
 		@Override
 		 public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
 			if (key.equals(getString(R.string.preferences_zoom))) {
 			    // Notify that value was really changed
 			    int value = sharedPreferences.getInt(getString(R.string.preferences_zoom), 0);
 			    utils.savePreferences(getString(R.string.preferences_zoom),value);			    
 			}
 		 }

 		
 		// Listener for option menu
 		@Override
 		public boolean onOptionsItemSelected(MenuItem item) {
 		    switch (item.getItemId()) {
 		    	case android.R.id.home:
 		    		// previous page or exit
 		    		finish();
 		    		
 		    		// Show transition when push back private Button in actionbar
 		    		overridePendingTransition (R.anim.open_main, R.anim.close_next);
 		    		return true;
 				default:
 					return super.onOptionsItemSelected(item);
 			}
 		}
 		
 	// Listener when back private Button pressed
 		@Override
 		public void onBackPressed() {
 			// TODO Auto-generated method stub
 			super.onBackPressed();
 			
 			// Show transition when back private Button pressed
 			overridePendingTransition (R.anim.open_main, R.anim.close_next);
 			
 		}

 		
 }
