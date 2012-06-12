/*
 * Copyright 2010 Facebook, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.qburst.android.linkedin;

import com.qburst.android.linkedin.LinkedIn.LinkedInDialogListener;

import oauth.signpost.commonshttp.CommonsHttpOAuthConsumer;
import oauth.signpost.commonshttp.CommonsHttpOAuthProvider;
import oauth.signpost.exception.OAuthCommunicationException;
import oauth.signpost.exception.OAuthExpectationFailedException;
import oauth.signpost.exception.OAuthMessageSignerException;
import oauth.signpost.exception.OAuthNotAuthorizedException;
import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Display;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.TextView;


public class LnDialog extends Dialog {
	public static final String TAG = "twitter";

    static final int TW_BLUE = 0xFF137EB8;
    static final float[] DIMENSIONS_LANDSCAPE = {460, 260};
    static final float[] DIMENSIONS_PORTRAIT = {280, 420};
    static final FrameLayout.LayoutParams FILL = 
        new FrameLayout.LayoutParams(ViewGroup.LayoutParams.FILL_PARENT, 
                         ViewGroup.LayoutParams.FILL_PARENT);
    static final int MARGIN = 4;
    static final int PADDING = 2;
    
	private int mIcon;
    private String mUrl;
    private LinkedInDialogListener mListener;
    private ProgressDialog mSpinner;
    private WebView mWebView;
    private LinearLayout mContent;
    private TextView mTitle;
    private Handler mHandler;

	private CommonsHttpOAuthConsumer mConsumer;
	private CommonsHttpOAuthProvider mProvider;

    public LnDialog(Context context,
    		CommonsHttpOAuthProvider provider,
    		CommonsHttpOAuthConsumer consumer,
    		LinkedInDialogListener listener, int icon) {
        super(context);
        mProvider = provider;
        mConsumer = consumer;
        mListener = listener;
		mIcon = icon;
		mHandler = new Handler();
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSpinner = new ProgressDialog(getContext());
        mSpinner.requestWindowFeature(Window.FEATURE_NO_TITLE);
        mSpinner.setMessage("Loading...");
        
        mContent = new LinearLayout(getContext());
        mContent.setOrientation(LinearLayout.VERTICAL);
        setUpTitle();
        setUpWebView();
                
        Display display = getWindow().getWindowManager().getDefaultDisplay();
        final float scale = getContext().getResources().getDisplayMetrics().density;
        float[] dimensions = display.getWidth() < display.getHeight() ?
        		DIMENSIONS_PORTRAIT : DIMENSIONS_LANDSCAPE;
        addContentView(mContent, new FrameLayout.LayoutParams(
        		(int) (dimensions[0] * scale + 0.5f),
        		(int) (dimensions[1] * scale + 0.5f)));

        retrieveRequestToken();
    }

    @Override
	public void show() {
		super.show();
		if (mSpinner.isShowing()) {
        	mSpinner.dismiss();
        }
		mSpinner.show();
	}

	private void setUpTitle() {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        Drawable icon = getContext().getResources().getDrawable(mIcon);
        mTitle = new TextView(getContext());
        mTitle.setText("LinkedIn");
        mTitle.setTextColor(Color.WHITE);
        mTitle.setTypeface(Typeface.DEFAULT_BOLD);
        mTitle.setBackgroundColor(TW_BLUE);
        mTitle.setPadding(MARGIN + PADDING, MARGIN, MARGIN, MARGIN);
        mTitle.setCompoundDrawablePadding(MARGIN + PADDING);
        mTitle.setCompoundDrawablesWithIntrinsicBounds(
                icon, null, null, null);
        mContent.addView(mTitle);
    }
    
    private void retrieveRequestToken() {
    	if (mSpinner.isShowing()) {
        	mSpinner.dismiss();
        }
        mSpinner.show();
        new Thread() {
        	@Override
        	public void run() {
            	try {
            		mUrl = null;
        			mUrl = mProvider.retrieveRequestToken(mConsumer, LinkedInConstants.LN_CALLBACK_URI);	
        			mWebView.loadUrl(mUrl);
        	    	
        		} catch (OAuthMessageSignerException e) {
        			mListener.onError(new DialogError(e.getMessage(), -1, LinkedIn.OAUTH_REQUEST_TOKEN));
        		} catch (OAuthNotAuthorizedException e) {
        			mListener.onError(new DialogError(e.getMessage(), -1, LinkedIn.OAUTH_REQUEST_TOKEN));
        		} catch (OAuthExpectationFailedException e) {
        			mListener.onError(new DialogError(e.getMessage(), -1, LinkedIn.OAUTH_REQUEST_TOKEN));
        		} catch (OAuthCommunicationException e) {
        			mListener.onError(new DialogError(e.getMessage(), -1, LinkedIn.OAUTH_REQUEST_TOKEN));
        		}catch (Exception e) {
        			mListener.onError(new DialogError(e.getMessage(), -1, LinkedIn.OAUTH_REQUEST_TOKEN));
        		} finally {
        			if(mUrl==null){
        				mHandler.post(new Runnable() {
        					@Override
        					public void run() {
        						if(mSpinner.isShowing())
        						mSpinner.dismiss();
        						if(LnDialog.this.isShowing())
        						LnDialog.this.dismiss();
        					}					
        				});
        			}
        		}
        	}
        }.start();
    }
    
    private void retrieveAccessToken(final String url) {
        mSpinner.show();
    	new Thread() {
    		@Override
    		public void run() {
    			Uri uri = Uri.parse(url);
    			String verifier = uri.getQueryParameter(oauth.signpost.OAuth.OAUTH_VERIFIER);
    			final Bundle values = new Bundle();
    			mHandler.post(new Runnable() {
					@Override
					public void run() {
						if(mSpinner.isShowing())
						mSpinner.dismiss();
						if(LnDialog.this.isShowing())
						LnDialog.this.dismiss();
					}					
				});
    			
    			try {
					mProvider.retrieveAccessToken(mConsumer, verifier);
					values.putString(LinkedIn.ACCESS_TOKEN, mConsumer.getToken());
					values.putString(LinkedIn.SECRET_TOKEN, mConsumer.getTokenSecret());
					mListener.onComplete(values);
				} catch (OAuthMessageSignerException e) {
					mListener.onError(new DialogError(e.getMessage(), -1, verifier));
				} catch (OAuthNotAuthorizedException e) {
					mListener.onLinkedInError(new LinkedInError(e.getMessage()));
				} catch (OAuthExpectationFailedException e) {
					mListener.onLinkedInError(new LinkedInError(e.getMessage()));
				} catch (OAuthCommunicationException e) {
					mListener.onError(new DialogError(e.getMessage(), -1, verifier));
				}
				
    		}
    	}.start();
    }
    
    private void setUpWebView() {
        mWebView = new WebView(getContext());
        mWebView.setVerticalScrollBarEnabled(false);
        mWebView.setHorizontalScrollBarEnabled(false);
        mWebView.setWebViewClient(new LnDialog.LnWebViewClient());
        mWebView.getSettings().setJavaScriptEnabled(true);
        //mWebView.loadUrl(mUrl);
        mWebView.setLayoutParams(FILL);
        mContent.addView(mWebView);
    }

    private class LnWebViewClient extends WebViewClient {

        @Override
        public boolean shouldOverrideUrlLoading(WebView view, String url) {
            Log.d(TAG, "Redirect URL: " + url);
            super.shouldOverrideUrlLoading(view, url);
            if (url.startsWith(LinkedInConstants.LN_CALLBACK_URI)) {
            	
            	Uri uri = Uri.parse(url);
            	if(uri.getQueryParameter("oauth_problem")!=null&&uri.getQueryParameter("oauth_problem").equalsIgnoreCase("user_refused")){
            		mListener.onCancel();
                    LnDialog.this.dismiss();
                    return true;
            	} else if(uri.getQueryParameter("oauth_token")!=null){
            		retrieveAccessToken(url);
            		LnDialog.this.dismiss();
            		return true;
            	}
                
            } else if(url.startsWith(LinkedIn.OAUTH_AUTHENTICATE)){
            	return false;
            }
            // launch non-dialog URLs in a full browser
            getContext().startActivity(
                    new Intent(Intent.ACTION_VIEW, Uri.parse(url))); 
            return true;
        }

        @Override
        public void onReceivedError(WebView view, int errorCode,
                String description, String failingUrl) {
            super.onReceivedError(view, errorCode, description, failingUrl);
            mListener.onError(
                    new DialogError(description, errorCode, failingUrl));
            LnDialog.this.dismiss();
        }

        @Override
        public void onPageStarted(WebView view, String url, Bitmap favicon) {
            Log.d(TAG, "WebView loading URL: " + url);
            super.onPageStarted(view, url, favicon);
            if (url.startsWith(LinkedInConstants.LN_CALLBACK_URI)) {
            	
            	LnDialog.this.dismiss();
            	mSpinner.dismiss();
            }else
            {if (mSpinner.isShowing()) {
            	mSpinner.dismiss();
            }
            mSpinner.show();
            }
        }

        @Override
        public void onPageFinished(WebView view, String url) {
            super.onPageFinished(view, url);
            String title = mWebView.getTitle();
            if (title != null && title.length() > 0) {
                mTitle.setText(title);
            }
            mSpinner.dismiss();
        }   
        
    }
    
    @Override
	public void onBackPressed() {
		// TODO Auto-generated method stub
		super.onBackPressed();
		mSpinner.dismiss();
		LnDialog.this.dismiss();
		this.mWebView.destroy();
		mListener.onCancel();
		
	}
}
