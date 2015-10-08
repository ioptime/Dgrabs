package com.ioptime.dgrabs.fragments;

import java.util.ArrayList;
import com.ioptime.dgrabs.R;

import java.util.HashMap;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.app.Activity;
import android.app.ProgressDialog;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.ioptime.dgrabs.adapters.AdapterCategory;
import com.ioptime.dgrabs.libraries.UserFunctions;
import com.ioptime.dgrabs.utils.Utils;
 

public class FragmentCategory extends Fragment implements OnClickListener {
	private OnCategoryListSelectedListener mCallback;
	
	private ArrayList<HashMap<String, String>> items;

	// Declare object of UserFunctions, AdapterCategory and JSONObject class
	private AdapterCategory la;
	private UserFunctions userFunction;
	private Utils utils;
	private JSONObject json;
	private ProgressDialog pDialog;
	
	// Declare view objects
	private TextView lblNoResult, lblAlert;
	private Button btnRetry; 
	private LinearLayout lytRetry;
	private ListView list;
	
	public static String[] Categories;
	public int mCurrentPosition;

	// Get length array from server
	private int intLengthData;
	
	// Create array variables to store data
	private String[] mCategoryId;
	private String[] mCategoryName;
	private String[] mIcMarker;
	
	public interface OnCategoryListSelectedListener{
		
		public void onCategoryListSelected(String mCategoryId, String mCategoryName);
	}
	
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {

		View v = inflater.inflate(R.layout.fragment_category, null);

		list 		= (ListView) v.findViewById(R.id.list);
		lblNoResult	= (TextView) v.findViewById(R.id.lblNoResult);
		lblAlert	= (TextView) v.findViewById(R.id.lblAlert);
		lytRetry 	= (LinearLayout) v.findViewById(R.id.lytRetry);
		btnRetry 	= (Button) v.findViewById(R.id.btnRetry);

		btnRetry.setOnClickListener(this);
		
		// Declare object of UserFuntions class
		userFunction= new UserFunctions();
		utils 		= new Utils(getActivity());
		items 		= new ArrayList<HashMap<String, String>>();
		
		if(utils.isNetworkAvailable()){	
			new getCategoryList().execute();
		} else {
			lblNoResult.setVisibility(View.GONE);
    		lytRetry.setVisibility(View.VISIBLE);
    		lblAlert.setText(R.string.no_connection);
		}
		

		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1, int position,
					long arg3) {
				// TODO Auto-generated method stub
				HashMap<String, String> item = new HashMap<String, String>();
		        item = items.get(position);
				
				// Pass id to onListSelected method on HomeActivity
				mCallback.onCategoryListSelected(item.get(userFunction.key_category_id), item.get(userFunction.key_category_name));
				
				// Set the item as checked to be highlighted when in two-pane layout
				list.setItemChecked(position, true);
				
			}
		});
		
        return v;
	}

	public void onActivityCreated(Bundle savedInstanceState) {
		super.onActivityCreated(savedInstanceState);

	}
	
	@Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);

        // This makes sure that the container activity has implemented
        // the callback interface. If not, it throws an exception.
        try {
            mCallback = (OnCategoryListSelectedListener) activity;
        } catch (ClassCastException e) {
            throw new ClassCastException(activity.toString()
                    + " must implement OnCategoryListSelectedListener");
        }
    }
	
	// AsynTask to get list Category data 
	public class getCategoryList extends AsyncTask<Void, Void, Void>{

    	@Override
		 protected void onPreExecute() {
            // Showing progress dialog before sending http request
            pDialog = new ProgressDialog(
                    getActivity());
            pDialog.setMessage("Please wait..");
            pDialog.setIndeterminate(true);
            pDialog.setCancelable(false);
            pDialog.show();
    		
    	}

    	@Override
		protected Void doInBackground(Void... params) {
			// TODO Auto-generated method stub
    		// Method to get data from server
			getDataFromServer();
			return null;
		}
    	
    	@Override
		protected void onPostExecute(Void result) {
			// TODO Auto-generated method stub
    		
    		if(isAdded()){
	            if(items.size() != 0){

	            	lytRetry.setVisibility(View.GONE);
	            	list.setVisibility(View.VISIBLE);
	            	
	            	lblNoResult.setVisibility(View.GONE);
	            	
	            	// Getting adapter
	            	la = new AdapterCategory(getActivity(), items);
	            	list.setAdapter(la);
	            	
	            } else {
	            	if(json != null){
						lblNoResult.setVisibility(View.VISIBLE);
						
	            		lytRetry.setVisibility(View.GONE);
	            		
		            } else {
						lblNoResult.setVisibility(View.GONE);
	            		lytRetry.setVisibility(View.VISIBLE);
	            		
		            	Toast.makeText(getActivity(), R.string.no_connection, Toast.LENGTH_SHORT).show();
	            	}
	            }
        	}
        	
        	// Closing progress dialog
        	if(pDialog.isShowing()) pDialog.dismiss();	
		}
	}
	
	// Method to get data from server
	public void getDataFromServer(){
	       
        try {
            json = userFunction.categoryList();
            if(json != null){
		        JSONArray dataDealsArray = json.getJSONArray(userFunction.array_category_list);
		        
		        intLengthData 	= dataDealsArray.length();
		        mCategoryId 	= new String[intLengthData];
		        mCategoryName 	= new String[intLengthData];
		        mIcMarker 		= new String[intLengthData];
		                   
		        // Store data to variable array
		        for (int i = 0; i < intLengthData; i++) {
		        	JSONObject dealObject = dataDealsArray.getJSONObject(i);
		        	HashMap<String, String> map = new HashMap<String, String>();
		        	
		            mCategoryId[i] 	= dealObject.getString(userFunction.key_category_id);
		            mCategoryName[i]= dealObject.getString(userFunction.key_category_name);
		            mIcMarker[i]	= dealObject.getString(userFunction.key_category_marker);
		            
	            	map.put(userFunction.key_category_id, mCategoryId[i]);
		            map.put(userFunction.key_category_name, mCategoryName[i]);
		            map.put(userFunction.key_category_marker, mIcMarker[i]);
		            
		            // Adding HashList to ArrayList
		            items.add(map);
		        }
            }                
        } catch (JSONException e) {
            // TODO Auto-generated catch block
        	Log.i("FragmentCategory", "getDataFromServer: "+e);
        }      
    }

	@Override
	public void onClick(View v) {
	// TODO Auto-generated method stub
			switch (v.getId()) {
			case R.id.btnRetry:
				if(utils.isNetworkAvailable()){	
					json = null;
					new getCategoryList().execute();
				} else {
					lblNoResult.setVisibility(View.GONE);
		    		lytRetry.setVisibility(View.VISIBLE);
		    		lblAlert.setText(R.string.no_connection);
				}

			default:
				break;
			}
		
	}
}

