package com.qburst.share.activities;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;

import com.qburst.android.interfaces.share.QBShare;
import com.qburst.android.interfaces.share.QBShareListener;
import com.qburst.share.R;
import com.qmcommerce.android.utils.QBImageLoader;

public class QBShareFacebook extends Activity implements
		QBShareListener {

	QBShare share;

	TextView Pname;
	TextView descrptn;

	private int _type;

	private String _name;
	private String _descrptn;
	private String _imageUrl;
	private ImageView separatorIV, pictureIV;
	private QBImageLoader imageLoader;
	private EditText commentET;

	

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.share_post_facebook);
		share = QBShare.getGlobalShare(this);
		imageLoader = new QBImageLoader(this.getApplicationContext());
		initViews();
		getBundleExtras();
		setValues();
	}

	/**
	 * initialize display
	 */
	private void initViews() {
		Pname = (TextView) findViewById(R.id.heading);
		descrptn = (TextView) findViewById(R.id.link);
		separatorIV = (ImageView) findViewById(R.id.seperatorImage);

		pictureIV = (ImageView) findViewById(R.id.image);

		commentET = (EditText) findViewById(R.id.comment);

		((EditText) findViewById(R.id.comment))
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {

					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(commentET
									.getWindowToken(), 0);
						}
						return false;
					}
				});

	}

	/**
	 * Get the bundled data
	 */

	public void getBundleExtras() {
		Bundle extras = getIntent().getExtras();
		_type = extras.getInt("type");
		_name = extras.getString("name");
		_descrptn = extras.getString("descrp");

		if (_type == 1 && extras.getString("link") != null) {
			_imageUrl = extras.getString("link");
			// _picture = extras.getString("picture");
		}
	}

	public void setValues() {

		Pname.setText(_name);
		descrptn.setText(_descrptn);
		separatorIV.setVisibility(View.VISIBLE);

		if (_imageUrl != null) {
			pictureIV.setVisibility(View.VISIBLE);
			separatorIV.setVisibility(View.VISIBLE);
			pictureIV.setTag(encodeImageUrl(_imageUrl));
			imageLoader
					.displayImage(encodeImageUrl(_imageUrl), this, pictureIV);
		}
	}

	public void cancelClicked(View v) {
		finish();
	}

	/**
	 * on share button clicked
	 * 
	 * @param v
	 */

	public void shareClicked(View v) {
		HashMap<String, String> data = new HashMap<String, String>();

		data.put("message", commentET.getText().toString());

		data.put("name", _name);

		data.put("caption", _descrptn);

		data.put("link", _imageUrl);

		if (_imageUrl != null)
			data.put("picture", _imageUrl);

		share.postMessageToSession(QBShare.QM_FACEBOOK_SESSION, data, this);
		Log.d("FB post", _name);
		data.clear();

	}

	@Override
	public void onError(Throwable e, int sessionType) {

	}

	@Override
	public void onSessionCancel(int sessionType) {

	}

	@Override
	public void onSessionError(Throwable e, int sessionType) {

	}

	@Override
	public void sessionAuthorized(int sessionType) {

	}

	public static String encodeImageUrl(String url) {
		int pos = url.lastIndexOf('/') + 1;
		String temp = url.substring(pos);
		int pos1 = 0;
		pos1 = temp.lastIndexOf('?') + 1;
		try {
			if (pos1 != 0)
				url = url.substring(0, pos)
						+ URLEncoder.encode(url.substring(pos, pos + pos1 - 1),
								"UTF-8");
			else
				url = url.substring(0, pos)
						+ URLEncoder.encode(url.substring(pos), "UTF-8");
		} catch (UnsupportedEncodingException e) {
			Log.e(e.getClass().getName() + ": encodeImageUrl", e.getMessage());
		}

		return url;
	}
}
