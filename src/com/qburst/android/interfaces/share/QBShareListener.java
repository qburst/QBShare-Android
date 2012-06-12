package com.qburst.android.interfaces.share;

public interface QBShareListener {

	void onSessionError(Throwable e, int sessionType);

	void onError(Throwable e, int sessionType);

	void sessionAuthorized(int sessionType);

	void onSessionCancel(int sessionType);

}
