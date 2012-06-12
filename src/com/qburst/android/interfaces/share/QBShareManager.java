package com.qburst.android.interfaces.share;

import java.util.HashMap;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;

import com.qburst.share.R;
import com.qburst.share.activities.QBShareFacebook;
import com.qburst.share.activities.QBShareTwitter;
import com.qburst.share.activities.QMShareLinkedIn;

public class QBShareManager implements QBShareListener {
	private Context _context = null;
	private Activity _mActivity;
	private CharSequence[] items;
	private static final int FACEBOOK_INDEX = 0;
	private static final int TWITTER_INDEX = 1;
	private static final int LINKEDIN_INDEX = 2;
	private static final int CANCEL_INDEX = 3;
	int _type;
	private String _shortenedUrl;
	private String _name, _imageUrl, _description;

	public QBShareManager(Activity activity, HashMap<String, String> datas) {
		_context = activity;
		_mActivity = activity;
		items = _mActivity.getResources().getStringArray(R.array.share);
		_name = datas.get("n");
		_imageUrl = datas.get("u");
		_description = datas.get("d");

	}

	public void displayShareDialog() {
		AlertDialog.Builder builder = new AlertDialog.Builder(_context);
		builder.setTitle("Share via");
		builder.setItems(items, new DialogInterface.OnClickListener() {

			public void onClick(DialogInterface dialog, int item) {
				switch (item) {
				case CANCEL_INDEX: {
					break;
				}
				case FACEBOOK_INDEX: {
					QBShare share = QBShare.getGlobalShare(QBShareManager.this);
					if (share.getAccessToken(QBShare.QM_FACEBOOK_SESSION) == null)
						share
								.authorize(_mActivity,
										QBShare.QM_FACEBOOK_SESSION);
					if (share.getAccessToken(QBShare.QM_FACEBOOK_SESSION) != null) {
						Intent fbIntent = new Intent(_mActivity,
								QBShareFacebook.class);

						fbIntent.putExtra("name", _name);
						fbIntent.putExtra("descrp", _description);
						fbIntent.putExtra("link", _imageUrl);

						fbIntent.putExtra("type", 1);
						_mActivity.startActivity(fbIntent);
					}
				}
					break;
				case TWITTER_INDEX: {

					QBShare share = QBShare.getGlobalShare(QBShareManager.this);
					if (share.getAccessToken(QBShare.QM_TWITTER_SESSION) == null) {
						share.authorize(_mActivity, QBShare.QM_TWITTER_SESSION);
					} else {

						Intent twIntent = new Intent(_mActivity,
								QBShareTwitter.class);

						_mActivity.startActivity(twIntent);
					}
				}
					break;

				case LINKEDIN_INDEX: {
					QBShare share = QBShare.getGlobalShare(QBShareManager.this);
					if (share.getAccessToken(QBShare.QM_LINKEDIN_SESSION) == null) {
						share.authorize((Activity) _context,
								QBShare.QM_LINKEDIN_SESSION);

					} else {
						showCommentDialog(QBShare.QM_LINKEDIN_SESSION);

					}

					break;
				}

				}

			}
		});
		AlertDialog alert = builder.create();
		alert.show();
	}

	private void showCommentDialog(int sessionType) {

		QMShareLinkedIn dialog = new QMShareLinkedIn(_context, _name,
				_imageUrl, _description);
		dialog.setSessionType(sessionType);
		dialog.show();
	}

	@Override
	public void sessionAuthorized(int sessionType) {
		if (sessionType == QBShare.QM_TWITTER_SESSION) {
			Intent twIntent = new Intent(_mActivity,
					QBShareTwitter.class);

			if (_type == 1) {

				twIntent.putExtra("message", _shortenedUrl);
			} else {
				twIntent.putExtra("message", _mActivity
						.getString(R.string.Twitter_mshop_app_message)
						+ " " + _name);
			}
			_mActivity.startActivity(twIntent);
		} else if (sessionType == QBShare.QM_FACEBOOK_SESSION) {

			QBShare share = QBShare.getGlobalShare(QBShareManager.this);
			if (share.getAccessToken(QBShare.QM_FACEBOOK_SESSION) == null)
				share.authorize(_mActivity, QBShare.QM_FACEBOOK_SESSION);
			if (share.getAccessToken(QBShare.QM_FACEBOOK_SESSION) != null) {
				Intent fbIntent = new Intent(_mActivity,
						QBShareFacebook.class);

				fbIntent.putExtra("name", _name);
				fbIntent.putExtra("descrp", _description);
				fbIntent.putExtra("link", _imageUrl);

				fbIntent.putExtra("type", 1);
				_mActivity.startActivity(fbIntent);
			}
		} else if (sessionType == QBShare.QM_LINKEDIN_SESSION) {
			Activity activity = (Activity) _context;
			activity.runOnUiThread(new Runnable() {

				@Override
				public void run() {

					showCommentDialog(QBShare.QM_LINKEDIN_SESSION);

				}
			});
		}

	}

	@Override
	public void onSessionCancel(int sessionType) {

	}

	@Override
	public void onSessionError(Throwable e, int sessionType) {

	}

	@Override
	public void onError(Throwable e, int sessionType) {

	}
}
