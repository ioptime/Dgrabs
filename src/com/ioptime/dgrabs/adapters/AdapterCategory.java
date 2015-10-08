package com.ioptime.dgrabs.adapters;


import java.util.ArrayList;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioptime.dgrabs.R;
import com.ioptime.dgrabs.libraries.UserFunctions;
 
import com.squareup.picasso.Picasso;
public class AdapterCategory extends BaseAdapter {

	private Activity activity;
	private ArrayList<HashMap<String, String>> data;
	private static LayoutInflater inflater=null;
		
	//declare object of userFunctions class
	private UserFunctions userFunction;

	// For animation transition
	private int lastPosition = -1;
	
	public AdapterCategory(Activity a, ArrayList<HashMap<String, String>> d) {
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
		
		//declare object of userFunctions class
		userFunction = new UserFunctions();
		
		if(convertView == null){
			convertView = inflater.inflate(R.layout.adapter_category, null);
			holder = new ViewHolder();	
			convertView.setTag(holder);
		}else{
			holder = (ViewHolder) convertView.getTag();
		}
		
		// Animation transition
		Animation animation = AnimationUtils.loadAnimation(activity, (position > lastPosition) ? R.anim.up_from_bottom : R.anim.down_from_top);
        convertView.startAnimation(animation);
        lastPosition = position;
        
		holder.lblNameCategory 	= (TextView) convertView.findViewById(R.id.lblNameCategory);
		holder.icMarker 		= (ImageView) convertView.findViewById(R.id.icmarker);
		
		HashMap<String, String> item = new HashMap<String, String>();
        item = data.get(position);
        
		holder.lblNameCategory.setText(item.get(userFunction.key_category_name));
		
		// Set marker with picasso
        Picasso.with(activity)
        .load(userFunction.URLAdmin+item.get(userFunction.key_category_marker))
        .fit().centerCrop()
        .tag(activity)
        .into(holder.icMarker);
		return convertView;
	}
	
	static class ViewHolder {
		private TextView lblNameCategory;
		private ImageView icMarker;
	}
		
		
}