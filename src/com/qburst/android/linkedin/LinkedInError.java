package com.qburst.android.linkedin;

public class LinkedInError extends Throwable {

	private static final long serialVersionUID = 6626439442641443626L;

	private int mErrorCode = 0;
	private String mErrorType;
	
	public LinkedInError(String message) {
		super(message);
	}

	public LinkedInError(String message, String errorType, int errorCode) {
		super(message);
		mErrorType = errorType;
		mErrorCode = errorCode;
	}

	public int getErrorCode() {
		return mErrorCode;
	}

	public String getErrorType() {
		return mErrorType;
	}

}
