package com.qburst.android.twitter;

import java.io.IOException;
import java.net.MalformedURLException;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import twitter4j.TwitterException;
import twitter4j.TwitterFactory;
import twitter4j.auth.AccessToken;
import android.Manifest;
import android.app.Activity;
import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.PackageManager;
import android.media.AudioManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Window;
import android.webkit.CookieSyncManager;

import com.qburst.android.facebook.Util;
import com.qburst.share.R;

public class Twitter {
	public static final String TAG = "twitter";
	private Activity _activity;

	//	public static final String CALLBACK_URI = "twitter://callback";
	public static final String CANCEL_URI = "twitter://cancel";
	public static final String ACCESS_TOKEN = "access_token";
	public static final String SECRET_TOKEN = "secret_token";

	public static final String REQUEST = "request";
	public static final String AUTHORIZE = "authorize";

	protected static String REQUEST_ENDPOINT = "https://api.twitter.com/1";

	protected static String OAUTH_REQUEST_TOKEN = "https://api.twitter.com/oauth/request_token";
	protected static String OAUTH_ACCESS_TOKEN = "https://api.twitter.com/oauth/access_token";
	protected static String OAUTH_AUTHORIZE = "https://api.twitter.com/oauth/authorize";

	private String mAccessToken = null;
	private String mSecretToken = null;
	private ProgressDialog _progressDialog;

	private int mIcon;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	private CommonsHttpOAuthProvider mHttpOauthProvider;

	public Twitter(int icon) {
		mIcon = icon;
	}

	public void authorize(final Context ctx,
			Handler handler,
			String consumerKey,
			String consumerSecret,
			final TwitterDialogListener listener) {
		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(
				consumerKey, consumerSecret);
		mHttpOauthProvider = new CommonsHttpOAuthProvider(
				OAUTH_REQUEST_TOKEN, OAUTH_ACCESS_TOKEN, OAUTH_AUTHORIZE);
		CookieSyncManager.createInstance(ctx);
		dialog(ctx, handler, new TwitterDialogListener() {

			@Override
			public void onComplete(Bundle values) {
				CookieSyncManager.getInstance().sync();
				setAccessToken(values.getString(ACCESS_TOKEN));
				setSecretToken(values.getString(SECRET_TOKEN));
				if (isSessionValid()) {
					Log.d(TAG, "token "+getAccessToken()+" "+getSecretToken());
					listener.onComplete(values);
				} else {
					onTwitterError(new TwitterError(ctx.getString(R.string.Oauth_failed_received)));
				}
			}

			@Override
			public void onTwitterError(TwitterError e) {
				Log.d(TAG, "Login failed: "+e);
				listener.onTwitterError(e);
			}

			@Override
			public void onError(DialogError e) {
				Log.d(TAG, "Login failed: "+e);
				listener.onError(e);
			}

			@Override
			public void onCancel() {
				Log.d(TAG, "Login cancelled");
				listener.onCancel();
			}

		});
	}

	public String logout(Context context) throws MalformedURLException, IOException {
		 Util.clearCookies(context);
	        Bundle b = new Bundle();
	        b.putString("method", "auth.expireSession");
	        //String response = request(b);
	        setAccessToken(null);
	        //setAccessExpires(0);
	        return null;
//         return "true";
	}

	public void dialog(final Context ctx,
			Handler handler,
			final TwitterDialogListener listener) {
		if (ctx.checkCallingOrSelfPermission(Manifest.permission.INTERNET) !=
			PackageManager.PERMISSION_GRANTED) {
			Util.showAlert(ctx, "Error", ctx.getString(R.string.App_requires_internet));
			return;
		}
		new TwDialog(ctx, mHttpOauthProvider, mHttpOauthConsumer,
				listener, mIcon).show();
	}

	public boolean isSessionValid() {
		return getAccessToken() != null && getSecretToken() != null;
	}

	public String getAccessToken() {
		return mAccessToken;
	}

	public void setAccessToken(String accessToken) {
		mAccessToken = accessToken;
	}

	public String getSecretToken() {
		return mSecretToken;
	}

	public void setSecretToken(String secretToken) {
		mSecretToken = secretToken;
	}

	public static interface TwitterDialogListener {
		public void onComplete(Bundle values);
		public void onTwitterError(TwitterError e);
		public void onError(DialogError e);
		public void onCancel();
	}
	
	public void tweetToTwitter(String message, Activity activity){
		
		_activity=activity;
		
		AccessToken accessToken = new AccessToken(getAccessToken(), getSecretToken());
		
		twitter4j.Twitter twitter = new TwitterFactory().getInstance();
		
		twitter.setOAuthConsumer(mHttpOauthConsumer.getConsumerKey(), mHttpOauthConsumer.getConsumerSecret());
		
		twitter.setOAuthAccessToken(accessToken);
		
		 
		try {
			
			showProgressDialog();
			twitter.updateStatus(message);		
			dismissProgressDialog() ;
			showDialogOkWithGoBack("",activity.getString(R.string.Twitter_tweet_success),activity);
			
			
			
		} catch (TwitterException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			Log.e("twitter error", e.toString());
			showDialogOkWithGoBack("",activity.getString(R.string.Twitter_tweet_failed),activity);
		}
		
		
	}
	private void showProgressDialog() {
		

		_progressDialog = new ProgressDialog(_activity);
		_progressDialog.setMessage(_activity.getString(R.string.wait));
		_progressDialog.setCancelable(false);
		_progressDialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
		_progressDialog.show();
	}
	private void dismissProgressDialog() {
		// TODO Auto-generated method stub
		if (_progressDialog != null) 
		{
			try {
				_progressDialog.dismiss();
			} catch (Exception e) 
			{
				Log.e(e.getClass().getName()+": dismissProgressDialog", e.getMessage(), e);
			}
		}
		
	}
	public static void showDialogOkWithGoBack(String title, String message,
			final Activity activity) {
		if(activity.getApplicationContext()!=null) { 
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
}