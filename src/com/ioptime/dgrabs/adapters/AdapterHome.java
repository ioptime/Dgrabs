package com.ioptime.dgrabs.adapters;

import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioptime.dgrabs.libraries.UserFunctions;
import com.ioptime.dgrabs.utils.Utils;
 
import com.squareup.picasso.Picasso;
import com.ioptime.dgrabs.R;

public class AdapterHome extends BaseAdapter {	
	
	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater=null;
	
	// For animation transition
	private int lastPosition = -1;
	
	// Declare object of userFunctions class
	private UserFunctions userFunction;
	public AdapterHome(Activity a, ArrayList<HashMap<String, String>> d) {
        activity = a;
        data=d;
	
        inflater = (LayoutInflater)activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
        
	}
	
	public int getCount() {
		// TODO Auto-generated method stub
		return data.size();
	}

	public Object getItem(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public long getItemId(int position) {
		// TODO Auto-generated method stub
		return position;
	}

	public View getView(int position, View convertView, ViewGroup parent) {
		// TODO Auto-generated method stub
		ViewHolder holder;
		
		// Declare object of UserFuntion class
		userFunction = new UserFunctions();
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.adapter_home, null);
			holder = new ViewHolder();
			
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Animation transition
		Animation animation = AnimationUtils.loadAnimation(activity, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;
		        
		// Connect views object and views id on xml
		holder.lblTitle 		= (TextView) convertView.findViewById(R.id.lblTitle);
		holder.lblStartValue	= (TextView) convertView.findViewById(R.id.lblStartValue);
		holder.lblAfterDiscValue= (TextView) convertView.findViewById(R.id.lblAfterDiscountValue);
		//holder.lblDateEnd 		= (TextView) convertView.findViewById(R.id.lblEndDate);
		
		holder.imgThumbnail = (ImageView) convertView.findViewById(R.id.imgThumbnail);
		holder.icMarker 	= (ImageView) convertView.findViewById(R.id.icMarker);
		
		HashMap<String, String> item = new HashMap<String, String>();
        item = data.get(position);
        
		// Set data to textview and imageview
		// Set image with picasso
        Picasso.with(activity)
        .load(userFunction.URLAdmin+item.get(userFunction.key_deals_image))
        .fit().centerCrop()
        .tag(activity)
        .into(holder.imgThumbnail);
        
		// Set marker with picasso
        Picasso.with(activity)
        .load(userFunction.URLAdmin+item.get(userFunction.key_category_marker))
        .fit().centerCrop()
        .tag(activity)
        .into(holder.icMarker);
        
		holder.lblTitle.setText(item.get(userFunction.key_deals_title));
		holder.lblStartValue.setText(item.get(userFunction.key_deals_start_value)+Utils.mCurrency);
		holder.lblAfterDiscValue.setText(item.get(userFunction.key_deals_after_disc_value)+Utils.mCurrency+" / ");
		//holder.lblDateEnd.setText("  "+item.get(userFunction.key_deals_date_end));
		holder.lblStartValue.setPaintFlags(holder.lblStartValue.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
		return convertView;
	}

	// Method to create instance of views
	static class ViewHolder {
		private ImageView imgThumbnail, icMarker;
		private TextView lblTitle, lblDateEnd, lblAfterDiscValue, lblStartValue;
	}
	
	
}