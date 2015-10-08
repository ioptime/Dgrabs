package com.ioptime.dgrabs.adapters;

import com.ioptime.dgrabs.R;

import android.app.Activity;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.ioptime.dgrabs.fragments.FragmentNavigationDrawer;

public class AdapterMenu extends BaseAdapter {

	private Activity activity;

	public AdapterMenu(Activity act) {
		this.activity = act;

	}

	public int getCount() {
		// TODO Auto-generated method stub
		return FragmentNavigationDrawer.listMenu.length;
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

		if (convertView == null) {
			LayoutInflater inflater = (LayoutInflater) activity
					.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
			convertView = inflater.inflate(R.layout.adapter_menu, null);
			holder = new ViewHolder();

			convertView.setTag(holder);
		} else {
			holder = (ViewHolder) convertView.getTag();
		}

		holder.txtCategory = (TextView) convertView
				.findViewById(R.id.txtCategory);
		holder.imgMenu = (ImageView) convertView.findViewById(R.id.ic_img);
		holder.txtCategory.setText(FragmentNavigationDrawer.listMenu[position]);
		holder.imgMenu
				.setBackgroundResource(FragmentNavigationDrawer.imageMenu[position]);
		return convertView;
	}

	static class ViewHolder {
		TextView txtCategory;
		ImageView imgMenu;
	}

}