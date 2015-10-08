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
import android.widget.TextView;
import android.widget.Toast;

import com.ioptime.dgrabs.adapters.AdapterHome;
import com.ioptime.dgrabs.libraries.UserFunctions;
import com.ioptime.dgrabs.loadmore.PagingListView;
import com.ioptime.dgrabs.utils.Utils;

public class FragmentCategoryList extends Fragment implements OnClickListener {

	// Create interface for ListFragment
	private OnDataListSelectedListener mCallback;

	private ArrayList<HashMap<String, String>> mItems;
	private ProgressDialog pDialog;

	// Declare object of userFunctions and Utils class
	private UserFunctions userFunction;
	private Utils utils;

	// Create instance of list and ListAdapter

	private AdapterHome la;
	private PagingListView list;

	// Declare view objects
	private TextView lblNoResult, lblAlert;
	private Button btnRetry;
	private LinearLayout lytRetry;

	// flag for current page
	private JSONObject json;
	private int mCurrentPage = 0;
	private int mPreviousPage;

	// create array variables to store data
	private String[] mDealsId;
	private String[] mTitle;
	private String[] mDateEnd;
	private String[] mAfterDiscValue;
	private String[] mStartValue;
	private String[] mImg;
	private String[] mIcMarker;

	private int intLengthData;
	// Declare variable
	private String mCategoryId;

	// To handle parameter loadmore gone or visible(1 = visible ; 0 = gone)
	private int paramLoadmore = 0;

	// Declare OnListSelected interface
	public interface OnDataListSelectedListener {
		public void onListSelected(String idSelected);
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		// TODO Auto-generated method stub

		View v = inflater.inflate(R.layout.fragment_home, container, false);

		list = (PagingListView) v.findViewById(R.id.list);
		lblNoResult = (TextView) v.findViewById(R.id.lblNoResult);
		lblAlert = (TextView) v.findViewById(R.id.lblAlert);
		lytRetry = (LinearLayout) v.findViewById(R.id.lytRetry);
		btnRetry = (Button) v.findViewById(R.id.btnRetry);

		btnRetry.setOnClickListener(this);

		// Declare object of userFunctions class
		userFunction = new UserFunctions();
		utils = new Utils(getActivity());
		mItems = new ArrayList<HashMap<String, String>>();

		// Get Bundle data
		Bundle bundle = this.getArguments();
		mCategoryId = bundle.getString(utils.EXTRA_CATEGORY_ID);

		if (utils.isNetworkAvailable()) {
			new loadFirstListView().execute();
		} else {
			lblNoResult.setVisibility(View.GONE);
			lytRetry.setVisibility(View.VISIBLE);
			lblAlert.setText(R.string.no_connection);
		}

		// Listener to get selected id when list item clicked
		list.setOnItemClickListener(new OnItemClickListener() {

			@Override
			public void onItemClick(AdapterView<?> arg0, View arg1,
					int position, long arg3) {
				// TODO Auto-generated method stub
				HashMap<String, String> item = new HashMap<String, String>();
				item = mItems.get(position);

				// Pass id to onListSelected method on HomeActivity
				mCallback.onListSelected(item.get(userFunction.key_deals_id));

				// Set the item as checked to be highlighted when in two-pane
				// layout
				list.setItemChecked(position, true);
			}
		});

		// Set loadmore
		list.setHasMoreItems(true);
		list.setPagingableListener(new PagingListView.Pagingable() {
			@Override
			public void onLoadMoreItems() {
				if (utils.isNetworkAvailable()) {
					json = null;
					if (paramLoadmore == 1) {
						new loadMoreListView().execute();
					} else {
						list.onFinishLoading(false, null);
					}

				} else {
					Toast.makeText(getActivity(), R.string.no_connection,
							Toast.LENGTH_SHORT).show();
				}
			}
		});

		return v;
	}

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		// This makes sure that the container activity has implemented
		// The callback interface. If not, it throws an exception.
		try {
			mCallback = (OnDataListSelectedListener) activity;
		} catch (ClassCastException e) {
			throw new ClassCastException(activity.toString()
					+ " must implement OnHeadlineSelectedListener");
		}
	}

	// Load first 10 videos
	private class loadFirstListView extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {
			// Showing progress dialog before sending http request
			pDialog = new ProgressDialog(getActivity());
			pDialog.setMessage("Please wait..");
			pDialog.setIndeterminate(true);
			pDialog.setCancelable(false);
			pDialog.show();
		}

		protected Void doInBackground(Void... unused) {
			getDataFromServer();
			return (null);
		}

		protected void onPostExecute(Void unused) {

			if (isAdded()) {

				if (mItems.size() != 0) {
					lytRetry.setVisibility(View.GONE);
					lblNoResult.setVisibility(View.GONE);
					list.setVisibility(View.VISIBLE);

					// Getting adapter
					la = new AdapterHome(getActivity(), mItems);

					if (list.getAdapter() == null) {
						list.setAdapter(la);
					}

				} else {
					if (json != null) {
						lblNoResult.setVisibility(View.VISIBLE);
						lytRetry.setVisibility(View.GONE);

					} else {
						lblNoResult.setVisibility(View.GONE);
						lytRetry.setVisibility(View.VISIBLE);
						lblAlert.setText(R.string.error_server);
					}
				}
			}

			// Closing progress dialog
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
		}
	}

	// Load more videos
	private class loadMoreListView extends AsyncTask<Void, Void, Void> {

		@Override
		protected void onPreExecute() {

		}

		protected Void doInBackground(Void... unused) {

			// Store previous value of current page
			mPreviousPage = mCurrentPage;
			// Increment current page
			mCurrentPage += FragmentHome.paramValueItemPerPage;
			getDataFromServer();
			return (null);
		}

		protected void onPostExecute(Void unused) {

			if (json != null) {
				// Get listview current position - used to maintain scroll
				// position
				int currentPosition = list.getFirstVisiblePosition();

				lytRetry.setVisibility(View.GONE);
				// Appending new data to mItems ArrayList
				la = new AdapterHome(getActivity(), mItems);

				if (list.getAdapter() == null) {
					list.setAdapter(la);
				}

				// Setting new scroll position
				list.setSelectionFromTop(currentPosition + 1, 0);
				list.onFinishLoading(true, mItems);

			} else {
				mCurrentPage = mPreviousPage;
				Toast.makeText(getActivity(), R.string.error_server,
						Toast.LENGTH_SHORT).show();
			}
			// Closing progress dialog
			if (pDialog.isShowing()) {
				pDialog.dismiss();
			}
		}
	}

	public void getDataFromServer() {

		try {

			json = userFunction.dealByCategory(mCategoryId, mCurrentPage);

			if (json != null) {
				JSONArray dataDealsArray = json
						.getJSONArray(userFunction.array_deal_by_category);

				// Get Count_Total from server
				int mCountTotal = Integer.valueOf(json
						.getString(userFunction.key_total_data));

				// Because it array, it start from 0 not 1, so mCountTotal - 1
				mCountTotal -= 1;

				/*
				 * Conditional if mCountTotal equal or more than mCurrentPage it
				 * means all data from server is already load
				 */
				if (((mCountTotal -= FragmentHome.paramValueItemPerPage) < mCurrentPage)) {
					paramLoadmore = 0;
				} else {
					paramLoadmore = 1;
				}

				intLengthData = dataDealsArray.length();
				mDealsId = new String[intLengthData];
				mTitle = new String[intLengthData];
				mDateEnd = new String[intLengthData];
				mAfterDiscValue = new String[intLengthData];
				mStartValue = new String[intLengthData];
				mImg = new String[intLengthData];
				mIcMarker = new String[intLengthData];

				for (int i = 0; i < intLengthData; i++) {
					// Store data from server to variable
					JSONObject dealsObject = dataDealsArray.getJSONObject(i);
					HashMap<String, String> map = new HashMap<String, String>();
					mDealsId[i] = dealsObject
							.getString(userFunction.key_deals_id);
					mTitle[i] = dealsObject
							.getString(userFunction.key_deals_title);
					mDateEnd[i] = dealsObject
							.getString(userFunction.key_deals_date_end);
					mAfterDiscValue[i] = dealsObject
							.getString(userFunction.key_deals_after_disc_value);
					mStartValue[i] = dealsObject
							.getString(userFunction.key_deals_start_value);
					mImg[i] = dealsObject
							.getString(userFunction.key_deals_image);
					mIcMarker[i] = dealsObject
							.getString(userFunction.key_category_marker);

					map.put(userFunction.key_deals_id, mDealsId[i]); // id not
																		// using
																		// any
																		// where
					map.put(userFunction.key_deals_title, mTitle[i]);
					map.put(userFunction.key_deals_date_end, mDateEnd[i]);
					map.put(userFunction.key_deals_after_disc_value,
							mAfterDiscValue[i]);
					map.put(userFunction.key_deals_start_value, mStartValue[i]);
					map.put(userFunction.key_deals_image, mImg[i]);
					map.put(userFunction.key_category_marker, mIcMarker[i]);

					// Adding HashList to ArrayList
					mItems.add(map);
				}
			}

		} catch (JSONException e) {
			// TODO Auto-generated catch block
			Log.i("FragmentHome", "getDataFromServer: " + e);
		}
	}

	@Override
	public void onDestroy() {
		// TODO Auto-generated method stub
		list.setAdapter(null);
		super.onDestroy();

	}

	@Override
	public void onClick(View v) {
		// TODO Auto-generated method stub
		switch (v.getId()) {
		case R.id.btnRetry:
			if (utils.isNetworkAvailable()) {
				json = null;
				new loadFirstListView().execute();
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
}
