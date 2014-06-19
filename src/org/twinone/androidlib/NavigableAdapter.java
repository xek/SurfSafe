package org.twinone.androidlib;

import java.util.List;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

public class NavigableAdapter extends BaseAdapter {

	private Context mContext;
	private List<Navigable> mItems;

	private boolean mShowImages;

	public void setItems(List<Navigable> items) {
		mItems = items;
	}

	public void setShowImages(boolean showImages) {
		mShowImages = showImages;
	}

	public NavigableAdapter(Context c) {
		mContext = c;
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	@Override
	public long getItemId(int position) {
		return position;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		View root = convertView;

		Navigable item = mItems.get(position);
		if (root == null) {
			root = LayoutInflater.from(mContext).inflate(
					R.layout.navigable_item, parent, false);
		}

		TextView tv = (TextView) root.findViewById(R.id.lib_navigable_text);
		tv.setText(item.text);
		ImageView iv = (ImageView) root.findViewById(R.id.lib_navigable_image);
		if (mShowImages) {
			int res = item.imageResId;
			if (res != 0) {
				iv.setVisibility(View.VISIBLE);
				iv.setImageResource(res);
			} else {
				iv.setVisibility(View.INVISIBLE);
			}
		} else {
			iv.setVisibility(View.GONE);
		}

		return root;
	}
}
