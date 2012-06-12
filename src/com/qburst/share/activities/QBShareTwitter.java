package com.qburst.share.activities;

import java.util.HashMap;

import android.app.Activity;
import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.TextView;

import com.qburst.android.interfaces.share.QBShare;
import com.qburst.android.interfaces.share.QBShareListener;
import com.qburst.share.R;

public class QBShareTwitter extends Activity implements
		QBShareListener {

	QBShare share;
	EditText messageET;
	private String _message;

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		setContentView(R.layout.share_post_twitter);

		share = QBShare.getGlobalShare(this);

		messageET = (EditText) findViewById(R.id.comment);
		((EditText) findViewById(R.id.comment))
				.setOnEditorActionListener(new EditText.OnEditorActionListener() {
					@Override
					public boolean onEditorAction(TextView v, int actionId,
							KeyEvent event) {
						if (actionId == EditorInfo.IME_ACTION_DONE) {
							InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
							imm.hideSoftInputFromWindow(messageET
									.getWindowToken(), 0);
						}
						return false;
					}
				});

		getBundleExtras();
	}

	public void getBundleExtras() {

		messageET.setText(_message);

	}

	public void shareClicked(View v) {

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("message", messageET.getText().toString());
		share.postMessageToSession(QBShare.QM_TWITTER_SESSION, data, this);

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
}
