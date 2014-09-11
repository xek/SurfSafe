package com.surfsafe.locker.categoryselect;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.Editor;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.os.Build;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.BaseAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import com.google.gson.Gson;
import com.surfsafe.locker.lock.AppLockService;
import com.surfsafe.locker.util.PrefUtils;
import com.surfsafe.locker.R;

public class CategoryAdapter extends BaseAdapter {

	private LayoutInflater mInflater;
	private PackageManager mPm;
	private Context mContext;
	private Set<CategoryListElement> mInitialItems;
	private List<CategoryListElement> mItems;
	private Editor mEditor;
	private SharedPreferences mPreferences;

	public CategoryAdapter(Context context) {
		mContext = context;
		mInflater = (LayoutInflater) context
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		mPm = context.getPackageManager();
		// Empty
		mInitialItems = new HashSet<CategoryListElement>();
		mItems = new ArrayList<CategoryListElement>();
		mPreferences = PrefUtils.appsPrefs(context);
		mEditor = mPreferences.edit();

		new LoaderClass().execute((Void[]) null);
		// Collections.sort(mItems);
	}

	private class LoaderClass extends AsyncTask<Void, Void, Void> {

		@Override
		protected Void doInBackground(Void... params) {
			loadAppsIntoList();
			return null;
		}

		@Override
		protected void onPostExecute(Void result) {
			sort();
			if (mListener != null) {
				mLoadComplete = true;
				mListener.onLoadComplete();
			}
		}

	}

	private boolean mLoadComplete;

	public boolean isLoadComplete() {
		return mLoadComplete;
	}

	private OnEventListener mListener;

	public void setOnEventListener(OnEventListener listener) {
		mListener = listener;
	}

	public interface OnEventListener {
		public void onLoadComplete();

		public void onDirtyStateChanged(boolean dirty);
	}

	public boolean areAllAppsLocked() {
		for (CategoryListElement app : mItems)
			if (app.isApp() && !app.locked)
				return false;
		return true;
	}

	/**
	 * Creates a completely new list with the apps. Should only be called once.
	 * Does not sort
	 * 
	 * @param mContext
	 */
	public void loadAppsIntoList() {

		// other apps
		final Intent i = new Intent(Intent.ACTION_MAIN);
		i.addCategory(Intent.CATEGORY_LAUNCHER);
		final List<ResolveInfo> ris = mPm.queryIntentActivities(i, 0);
/*
		for (ResolveInfo ri : ris) {
				final CategoryListElement ah = new CategoryListElement(ri.loadLabel(mPm)
						.toString(), ri.activityInfo,
						CategoryListElement.PRIORITY_CHILD_CATS);
				mInitialItems.add(ah);
		} */
		final Set<String> lockedApps = PrefUtils.getLockedApps(mContext);
		for (CategoryListElement ah : mInitialItems) {
			ah.locked = lockedApps.contains(ah.packageName);
		}
		mItems = new ArrayList<CategoryListElement>(mInitialItems);
	}


	/**
	 * Sort the apps and notify the ListView that the items have changed. Should
	 * be called from the working thread
	 */
	public void sort() {
		Collections.sort(mItems);
		notifyDataSetChanged();
		notifyDirtyStateChanged(false);
	}

	@Override
	public int getCount() {
		return mItems.size();
	}

	@Override
	public Object getItem(int position) {
		return mItems.get(position);
	}

	public List<CategoryListElement> getAllItems() {
		return mItems;
	}

	@Override
	public long getItemId(int position) {
		return 0;
	}

	@Override
	public int getViewTypeCount() {
		// Number of different views we have
		return 2;
	}

	@Override
	public int getItemViewType(int position) {
		return mItems.get(position).isApp() ? 0 : 1;
	}

	@Override
	public View getView(int position, View convertView, ViewGroup parent) {
		if (mItems.get(position).isApp()) {
			return createAppViewFromResource(position, convertView, parent);
		} else {
			return createSeparatorViewFromResource(position, convertView,
					parent);
		}
	}

	private View createSeparatorViewFromResource(int position,
			View convertView, ViewGroup parent) {
		CategoryListElement ah = mItems.get(position);

		View view = convertView;
		if (view == null)
			view = mInflater.inflate(R.layout.applist_item_category, parent,
					false);
		TextView tv = (TextView) view.findViewById(R.id.listName);
		tv.setText(ah.title);

		return view;
	}

	private View createAppViewFromResource(int position, View convertView,
			ViewGroup parent) {

		CategoryListElement ah = mItems.get(position);
		View view = convertView;
		if (view == null)
			view = mInflater.inflate(R.layout.applist_item_app, parent, false);
		// changes with every click

		final ImageView lock = (ImageView) view
				.findViewById(R.id.applist_item_image);
		lock.setVisibility(ah.locked ? View.VISIBLE : View.GONE);

		final TextView name = (TextView) view.findViewById(R.id.listName);
		name.setText(ah.getLabel(mPm));

		final ImageView icon = (ImageView) view.findViewById(R.id.listIcon);
		final Drawable bg = ah.getIcon(mPm);
		if (bg == null)
			icon.setVisibility(View.GONE);
		else
			setBackgroundCompat(icon, bg);

		return view;
	}

	// TODO
	// TODO
	// TODO
	// TODO
	// TODO Important: Undo action.
	private ArrayList<CategoryListElement> mUndoItems;

	public void prepareUndo() {
		mUndoItems = new ArrayList<CategoryListElement>(mItems);
	}

	public void undo() {
		mItems = new ArrayList<CategoryListElement>(mUndoItems);
		notifyDataSetChanged();
	}

	public void setAllLocked(boolean lock) {
		ArrayList<String> apps = new ArrayList<String>();
		for (CategoryListElement app : mItems) {
			if (app.isApp()) {
				app.locked = lock;
				apps.add(app.packageName);
			}
		}
		setLocked(lock, apps.toArray(new String[apps.size()]));
		sort();
		save();
	}

	private boolean mDirtyState;

	private void notifyDirtyStateChanged(boolean dirty) {
		if (mDirtyState != dirty) {
			mDirtyState = dirty;
			if (mListener != null) {
				mListener.onDirtyStateChanged(dirty);
			}
		}
	}

	public void toggle(CategoryListElement item) {
		if (item.isApp()) {
			item.locked = !item.locked;
			setLocked(item.locked, item.packageName);
			save();
		}
		List<CategoryListElement> list = new ArrayList<CategoryListElement>(mItems);
		Collections.sort(list);
		boolean dirty = !list.equals(mItems);
		Log.d("", "dirty=" + dirty + ", mDirtyState = " + mDirtyState);

		notifyDirtyStateChanged(dirty);
	}

	public void save() {
		//TODO
		PrefUtils.apply(mEditor);
		AppLockService.restart(mContext);
		Gson gson = new Gson();
		Log.d("PREFERENCES", gson.toJson(mPreferences.getAll()));
		//TODO: send preferences to surfsafe
	}

	public void setLocked(boolean lock, String... packageNames) {
		Log.d("", "setLocked");
		for (String packageName : packageNames) {
			if (lock) {
				mEditor.putBoolean(packageName, true);
			} else {
				mEditor.remove(packageName);
			}
		}
	}

	@SuppressWarnings("deprecation")
	@SuppressLint("NewApi")
	private void setBackgroundCompat(View v, Drawable bg) {
		if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN)
			v.setBackgroundDrawable(bg);
		else
			v.setBackground(bg);
	}

}
