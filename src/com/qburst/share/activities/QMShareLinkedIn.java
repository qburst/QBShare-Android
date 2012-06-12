package com.qburst.share.activities;

import java.util.HashMap;

import com.qburst.android.interfaces.share.QBShare;
import com.qburst.android.interfaces.share.QBShareListener;
import com.qburst.share.R;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

public class QMShareLinkedIn extends Dialog implements
		android.view.View.OnClickListener, QBShareListener {

	private Context _context;
	private EditText _commentET;
	private int _sessionType;
	private String _name, _imageUrl, _description;

	public int getSessionType() {
		return _sessionType;
	}

	public void setSessionType(int _sessionType) {
		this._sessionType = _sessionType;
	}

	public QMShareLinkedIn(Context context, String name, String imageUrl,
			String description) {
		super(context);
		_context = context;
		_name = name;
		_imageUrl = imageUrl;
		_description = description;
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		setContentView(R.layout.share_post_linkedin);
		Button cancelButton = (Button) findViewById(R.id.cancelButton);
		cancelButton.setOnClickListener(this);

		Button shareButton = (Button) findViewById(R.id.shareButton);
		shareButton.setOnClickListener(this);
		_commentET = (EditText) findViewById(R.id.commentEditText);
	}

	@Override
	public void onClick(View v) {
		switch (v.getId()) {
		case R.id.cancelButton:
			this.dismiss();
			break;
		case R.id.shareButton: {

			switch (_sessionType) {

			case QBShare.QM_LINKEDIN_SESSION:
				postToLinkedIn();
				break;
			}
		}
			break;
		default:
			break;
		}
		dismiss();
	}

	private void postToLinkedIn() {

		QBShare share = QBShare.getGlobalShare(this);

		HashMap<String, String> data = new HashMap<String, String>();
		data.put("name", _name);
		data.put("caption", _name);
		data.put("link", _imageUrl);
		data.put("picture", _imageUrl);

		data.put("description", _description);
		data.put("message", _commentET.getText().toString());

		share.postMessageToSession(QBShare.QM_LINKEDIN_SESSION, data,
				(Activity) _context);

	}

	@Override
	public void onSessionError(Throwable e, int sessionType) {

	}

	@Override
	public void onError(Throwable e, int sessionType) {

	}

	@Override
	public void sessionAuthorized(int sessionType) {

	}

	@Override
	public void onSessionCancel(int sessionType) {

	}
}
