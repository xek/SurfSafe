package org.twinone.androidlib;

import java.util.List;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

public class SimpleNavigationFragment extends NavigationFragment {

	private ListView mListView;
	private NavigableAdapter mAdapter;

	private int mListViewResId;
	private int mLayoutResId;

	public void setupCustom(int layoutResId, int listViewResId) {
		mListViewResId = listViewResId;
		mLayoutResId = layoutResId;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container,
			Bundle savedInstanceState) {
		View v = null;
		if (mLayoutResId != 0 && mListViewResId != 0) {
			v = inflater.inflate(mLayoutResId, container, false);
			mListView = (ListView) v.findViewById(mListViewResId);
		} else {
			mListView = new ListView(getActivity());
			v = mListView;
		}

		if (mAdapter != null) {
			mListView.setAdapter(mAdapter);
		}
		return v;
	}

	public void setItems(List<Navigable> items) {
		mAdapter = new NavigableAdapter(getActivity());
		mAdapter.setItems(items);
		if (mListView != null) {
			mListView.setAdapter(mAdapter);
		}
	}

	public NavigableAdapter getAdapter() {
		return mAdapter;
	}
}
