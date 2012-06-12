package com.qburst.android.interfaces.share;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Iterator;

import org.json.JSONException;
import org.json.JSONObject;

import com.qburst.android.facebook.DialogError;
import com.qburst.android.facebook.Facebook;
import com.qburst.android.facebook.FacebookError;
import com.qburst.android.linkedin.LinkedIn;
import com.qburst.android.linkedin.LinkedInError;
import com.qburst.android.twitter.Twitter;
import com.qburst.android.twitter.TwitterError;
import com.qburst.config.QBShareConstants;
import com.qburst.share.R;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.media.AudioManager;
import android.os.AsyncTask;
import android.os.Bundle;

public class QBShare {

	public static final int QM_FACEBOOK_SESSION = 1;
	public static final int QM_TWITTER_SESSION = 2;
	public static final int QM_LINKEDIN_SESSION = 3;

	public static Facebook _facebook;
	public static Twitter _twitter;
	public static LinkedIn _linkedIn;

	private QBShareListener _listener;
	private HashMap<String, String> _userInfo;

	private static QBShare _globalShare;
	JSONObject response = null;
	private ProgressDialog dialog;

	public static QBShare getGlobalShare(QBShareListener listener) {

		if (_globalShare == null) {
			_globalShare = new QBShare(listener);
		} else {
			_globalShare.setShareListener(listener);
		}
		return _globalShare;
	}

	public QBShare(QBShareListener listener) {
		_listener = listener;
	}

	public String getAccessToken(int sessionType) {

		String accessToken = null;

		switch (sessionType) {
		case QM_FACEBOOK_SESSION: {
			if (_facebook != null)
				accessToken = _facebook.getAccessToken();
		}
			break;

		case QM_TWITTER_SESSION: {
			if (_twitter != null)
				accessToken = _twitter.getAccessToken();
		}
			break;

		case QM_LINKEDIN_SESSION: {
			if (_linkedIn != null)
				accessToken = _linkedIn.getAccessToken();
		}

		default:
			break;
		}

		return accessToken;
	}

	/**
	 * authorize session
	 * 
	 * @param callingActivity
	 * @param sessionType
	 */
	public void authorize(Activity callingActivity, int sessionType) {

		switch (sessionType) {
		case QM_FACEBOOK_SESSION: {
			_facebook = new Facebook(QBShareConstants.FB_APP_ID);
			dialog = new ProgressDialog(callingActivity);
			_facebook.authorize(callingActivity, new String[] {
					"publish_stream", "offline_access", "email" },
					new Facebook.DialogListener() {

						@Override
						public void onFacebookError(FacebookError e) {
							if (_listener != null) {
								_listener
										.onSessionError(e, QM_FACEBOOK_SESSION);
							}
						}

						@Override
						public void onError(DialogError e) {
							if (_listener != null) {
								_listener.onError(e, QM_FACEBOOK_SESSION);
							}
						}

						@Override
						public void onComplete(Bundle values) {

							new fbLoginAsyncTask().execute();
						}

						@Override
						public void onCancel() {
							if (_listener != null) {
								_listener.onSessionCancel(QM_FACEBOOK_SESSION);
							}
						}
					});

		}
			break;

		case QM_TWITTER_SESSION: {
			_twitter = new Twitter(R.drawable.twitter_logo);
			_twitter.authorize(callingActivity, null,
					QBShareConstants.TW_APP_KEY,
					QBShareConstants.TW_APP_SECRET,
					new Twitter.TwitterDialogListener() {

						@Override
						public void onTwitterError(TwitterError e) {
							if (_listener != null) {
								_listener.onSessionError(e, QM_TWITTER_SESSION);
							}
						}

						@Override
						public void onError(
								com.qburst.android.twitter.DialogError e) {
							if (_listener != null) {
								_listener.onError(e, QM_TWITTER_SESSION);
							}
						}

						@Override
						public void onComplete(Bundle values) {
							if (_listener != null) {
								_listener.sessionAuthorized(QM_TWITTER_SESSION);
							}
						}

						@Override
						public void onCancel() {
							if (_listener != null) {
								_listener.onSessionCancel(QM_TWITTER_SESSION);
							}
						}
					});
		}
			break;

		case QM_LINKEDIN_SESSION: {
			_linkedIn = new LinkedIn(R.drawable.linkedin_logo);
			_linkedIn.authorize(callingActivity, null,
					QBShareConstants.LN_API_KEY,
					QBShareConstants.LN_API_SECRET,
					new LinkedIn.LinkedInDialogListener() {

						@Override
						public void onLinkedInError(LinkedInError e) {

							if (_listener != null) {
								_listener
										.onSessionError(e, QM_LINKEDIN_SESSION);
							}
						}

						@Override
						public void onError(
								com.qburst.android.linkedin.DialogError e) {
							if (_listener != null) {
								_listener.onError(e, QM_LINKEDIN_SESSION);
							}
						}

						@Override
						public void onComplete(Bundle values) {
							if (_listener != null) {
								_listener
										.sessionAuthorized(QM_LINKEDIN_SESSION);
							}
						}

						@Override
						public void onCancel() {
							if (_listener != null) {
								_listener.onSessionCancel(QM_LINKEDIN_SESSION);
							}
						}
					});
		}
			break;

		default:
			break;
		}

	}

	/**
	 * postMessageToSession
	 * 
	 * @param sessionType
	 * @param data
	 * @param activity
	 */
	public void postMessageToSession(int sessionType,
			HashMap<String, String> data, Activity activity) {

		switch (sessionType) {
		case QM_FACEBOOK_SESSION: {
			Bundle params = new Bundle();

			Iterator<String> keys = data.keySet().iterator();
			while (keys.hasNext()) {
				String key = keys.next();
				params.putString(key, data.get(key));
			}
			try {
				_facebook.request("me/feed", params, "POST");
				showDialogOkWithGoBack("", activity
						.getString(R.string.Facebook_post_success), activity);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
				showDialogOkWithGoBack("", activity
						.getString(R.string.Facebook_post_failed), activity);
			} catch (MalformedURLException e) {
				e.printStackTrace();
				showDialogOkWithGoBack("", activity
						.getString(R.string.Facebook_post_failed), activity);
			} catch (IOException e) {
				e.printStackTrace();
				showDialogOkWithGoBack("", activity
						.getString(R.string.Facebook_post_failed), activity);
			} catch (Exception e) {
				e.printStackTrace();
				showDialogOkWithGoBack("", activity
						.getString(R.string.Facebook_post_failed), activity);
			}
		}
			break;

		case QM_TWITTER_SESSION: {
			_twitter.tweetToTwitter(data.get("message"), activity);
		}
			break;

		case QM_LINKEDIN_SESSION: {
			String xmlContent = getExtraXMLContentForLinkedIn(data);
			String message = null;
			if (xmlContent == null) {
				message = String.format("%s, Commented about %s, %s", data
						.get("message"), data.get("name"), data.get("address"));
				xmlContent = "";
			} else {
				message = data.get("message");
			}

			String resultXML = String.format(
					getXMLPOSTFormatForLinkedIn(activity), message, xmlContent);
			try {
				_linkedIn.shareMessage(resultXML);
				showDialogOk("", activity
						.getString(R.string.LinkedIn_post_success), activity);

			} catch (Exception e) {
				e.printStackTrace();
				showDialogOk("", activity
						.getString(R.string.LinkedIn_post_failed), activity);

			}

		}
		}

	}

	public static void showDialogOk(String title, String message,
			Context context) {
		if (context != null) {
			Dialog dlg = new AlertDialog.Builder(context).setTitle(title)
					.setMessage(message).setPositiveButton("OK",
							new DialogInterface.OnClickListener() {
								public void onClick(DialogInterface dialog,
										int whichButton) {

								}
							}).create();
			dlg.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			dlg.show();
		}
	}

	public static void showDialogOkWithGoBack(String title, String message,
			final Activity activity) {
		if (activity.getApplicationContext() != null) {
			AlertDialog.Builder adb = new AlertDialog.Builder(activity);
			adb.setTitle(title);
			adb.setMessage(message);
			adb.setCancelable(false);
			adb.setNeutralButton("OK", new DialogInterface.OnClickListener() {

				@Override
				public void onClick(DialogInterface dialog, int which) {
					dialog.cancel();
					activity.onBackPressed();
				}
			});
			AlertDialog ad = adb.create();
			ad.setVolumeControlStream(AudioManager.STREAM_MUSIC);
			ad.show();
		}
	}

	private String getXMLPOSTFormatForLinkedIn(Activity activity) {

		InputStream is;
		try {
			is = activity.getAssets().open("format.xml");
			BufferedReader r = new BufferedReader(new InputStreamReader(is));
			StringBuilder total = new StringBuilder();
			String line;
			while ((line = r.readLine()) != null) {
				total.append(line);
			}

			return total.toString();
		} catch (IOException e) {

			e.printStackTrace();
		}

		return null;

	}

	private String getExtraXMLContentForLinkedIn(HashMap<String, String> data) {

		String xmlContent = null;

		if (data.containsKey("picture")) {
			String format = "<content><title>%s</title><submitted-url>%s</submitted-url><submitted-image-url>%s</submitted-image-url><description>%s</description></content>";
			xmlContent = String.format(format, data.get("name"), data
					.get("picture"), data.get("picture"), data
					.get("description"));
		}

		return xmlContent;

	}

	public void setShareListener(QBShareListener listener) {
		this._listener = listener;
	}

	public void logout(Activity callingActivity, int sessionType) {
		switch (sessionType) {
		case QM_FACEBOOK_SESSION:
			try {
				if (_facebook != null)
					_facebook.logout(callingActivity);
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case QM_TWITTER_SESSION:
			try {
				if (_twitter != null) {
					_twitter.logout(callingActivity);
					_twitter.setAccessToken(null);
					_twitter.setSecretToken(null);
				}
			} catch (MalformedURLException e) {
				e.printStackTrace();
			} catch (IOException e) {
				e.printStackTrace();
			}
			break;

		case QM_LINKEDIN_SESSION: {
			if (_linkedIn != null) {
				try {
					_linkedIn.logout(callingActivity);
				} catch (MalformedURLException e) {
					e.printStackTrace();
				} catch (IOException e) {
					e.printStackTrace();
				}
			}
		}
		}
	}

	public HashMap<String, String> getUserInfo() {
		return _userInfo;
	}

	public JSONObject fetchUserInfo() {

		try {
			response = new JSONObject(_facebook.request("me"));

		} catch (FileNotFoundException e) {
			e.printStackTrace();
		} catch (MalformedURLException e) {
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		} catch (JSONException e) {
			e.printStackTrace();
		}
		return response;
	}

	class fbLoginAsyncTask extends AsyncTask<Void, Void, JSONObject> {

		protected void onPreExecute() {

			if (dialog != null) {
				dialog.setMessage("Please wait...");
				dialog.setIndeterminate(true);
				dialog.setCancelable(false);
				dialog.show();
			}

		}

		protected JSONObject doInBackground(Void... unused) {

			return fetchUserInfo();
		}

		protected void onProgressUpdate(Void... item) {

		}

		protected void onPostExecute(JSONObject _response) {
			try {
				if (_response != null) {
					_userInfo = new HashMap<String, String>();
					_userInfo.put("email", _response.getString("email"));
					_userInfo.put("id", _response.getString("id"));
					_userInfo.put("name", _response.getString("name"));
				}
				if (_listener != null) {
					_listener.sessionAuthorized(QM_FACEBOOK_SESSION);
				}
			} catch (Exception e) {
				if (_listener != null) {
					_listener.sessionAuthorized(QM_FACEBOOK_SESSION);
				}
			}
			dialog.cancel();
		}
	}

}
