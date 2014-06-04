/*
 * Copyright 2014 Luuk Willemsen (Twinone)
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *     http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * 
 */
package org.twinone.androidlib;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

public abstract class Dialogs {
	/**
	 * 
	 * Get the dialog to share the app
	 */
	public static AlertDialog getShareEditDialog(final Context c,
			final String promoText) {
		final AlertDialog.Builder ab = new AlertDialog.Builder(c);

		LayoutInflater inflater = (LayoutInflater) c
				.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View v = inflater.inflate(R.layout.share_dialog, null);
		ab.setView(v);
		final EditText et = (EditText) v
				.findViewById(R.id.share_dialog_et_content);
		et.setText(promoText);
		ab.setCancelable(false);
		ab.setTitle(R.string.lib_share_dlg_tit);
		ab.setMessage(R.string.lib_share_dlg_msg);
		ab.setNegativeButton(android.R.string.cancel, null);
		ab.setPositiveButton(android.R.string.ok, new OnClickListener() {

			@Override
			public void onClick(DialogInterface dialog, int which) {
				String text = et.getText().toString();

				Intent intent = new Intent();
				intent.setAction(Intent.ACTION_SEND);
				intent.setType("text/plain");
				intent.putExtra(Intent.EXTRA_TEXT, text);
				Intent sender = Intent.createChooser(intent,
						c.getString(R.string.lib_share_dlg_tit));
				c.startActivity(sender);
				// At this point, we can assume the user will share the app.
				// So never show the dialog again, he can manually open it from
				// the navigation
			}
		});
		return ab.create();
	}
}
