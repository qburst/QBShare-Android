package com.qburst.android.linkedin;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;

import oauth.signpost.OAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;

import org.apache.http.HttpResponse;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.webkit.CookieSyncManager;

public class LinkedIn {
	public static final String TAG = "LinkedIn";

	public static final String ACCESS_TOKEN = "access_token";
	public static final String SECRET_TOKEN = "secret_token";

	public static final String REQUEST = "request";
	public static final String AUTHORIZE = "authorize";

	protected static String REQUEST_ENDPOINT = "https://api.linkedin.com/uas/";

	protected static String OAUTH_REQUEST_TOKEN = "https://api.linkedin.com/uas/oauth/requestToken";
	protected static String OAUTH_ACCESS_TOKEN = "https://api.linkedin.com/uas/oauth/accessToken";
	protected static String OAUTH_AUTHORIZE = "https://www.linkedin.com/uas/oauth/authorize";
	public static String OAUTH_AUTHENTICATE = "https://www.linkedin.com/uas/oauth/authenticate";
	protected static String SHARE_URL = "https://api.linkedin.com/v1/people/~/shares";
	
	private String mAccessToken = null;
	private String mSecretToken = null;

	private int mIcon;
	private CommonsHttpOAuthConsumer mHttpOauthConsumer;
	private CommonsHttpOAuthProvider mHttpOauthProvider;

	public LinkedIn(int icon) {
		mIcon = icon;
	}

	public void authorize(Context ctx,
			Handler handler,
			String consumerKey,
			String consumerSecret,
			final LinkedInDialogListener listener) {
		mHttpOauthConsumer = new CommonsHttpOAuthConsumer(
				consumerKey, consumerSecret);
		mHttpOauthProvider = new CommonsHttpOAuthProvider(
				OAUTH_REQUEST_TOKEN, OAUTH_ACCESS_TOKEN, OAUTH_AUTHORIZE);
		
		CookieSyncManager.createInstance(ctx);
		dialog(ctx, handler, new LinkedInDialogListener() {

			@Override
			public void onComplete(Bundle values) {
				CookieSyncManager.getInstance().sync();
				setAccessToken(values.getString(ACCESS_TOKEN));
				setSecretToken(values.getString(SECRET_TOKEN));
				if (isSessionValid()) {
					Log.d(TAG, "token "+getAccessToken()+" "+getSecretToken());
					listener.onComplete(values);
				} else {
					onLinkedInError(new LinkedInError("failed to receive oauth token"));
				}
			}

			@Override
			public void onLinkedInError(LinkedInError e) {
				Log.d(TAG, "Login failed: "+e);
				listener.onLinkedInError(e);
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
		return "true";
	}

	public void dialog(final Context ctx,
			Handler handler,
			final LinkedInDialogListener listener) {
		if (ctx.checkCallingOrSelfPermission(Manifest.permission.INTERNET) !=
			PackageManager.PERMISSION_GRANTED) {
			Util.showAlert(ctx, "Error", "Application requires permission to access the Internet");
			return;
		}
		new LnDialog(ctx, mHttpOauthProvider, mHttpOauthConsumer,
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

	public static interface LinkedInDialogListener {
		public void onComplete(Bundle values);
		public void onLinkedInError(LinkedInError e);
		public void onError(DialogError e);
		public void onCancel();
	}

	public void shareMessage(String resultXML) throws Exception {
		// create a consumer object and configure it with the access
        // token and token secret obtained from the service provider
		
        OAuthConsumer consumer = new CommonsHttpOAuthConsumer(mHttpOauthConsumer.getConsumerKey(),
        		mHttpOauthConsumer.getConsumerSecret());
        consumer.setTokenWithSecret(getAccessToken(), getSecretToken());
        
        DefaultHttpClient client = new DefaultHttpClient();
        HttpPost post = new HttpPost(SHARE_URL);
        
        consumer.sign(post);
		post.addHeader("Content-Type", "text/xml");
		post.addHeader("User-Agent", System.getProperties().
                getProperty("http.agent") + " LinkedInAndroidSDK");
		
		StringEntity entity = new StringEntity(resultXML);
		post.setEntity(entity);
		HttpResponse response = client.execute(post);
		System.out.println("Statusline : " + response.getStatusLine());
		InputStream data = response.getEntity().getContent();
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(data));
		String responeLine;
		StringBuilder responseBuilder = new StringBuilder();
		while ((responeLine = bufferedReader.readLine()) != null) {
			responseBuilder.append(responeLine);
		}
		String responseString = responseBuilder.toString();
		System.out.println("Response : " + responseString);		
        
		if(response.getStatusLine().getStatusCode()!=201){
			Exception ex = new Exception("Post failed");
			throw ex;
		}
        
		
	}	
	
}
