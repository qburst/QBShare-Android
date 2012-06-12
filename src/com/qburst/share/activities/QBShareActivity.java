package com.qburst.share.activities;

import java.util.HashMap;

import com.qburst.android.interfaces.share.QBShareManager;
import com.qburst.share.R;

import android.app.Activity;
import android.os.Bundle;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.Button;

public class QBShareActivity extends Activity {
	
	String name, url, description;
	HashMap<String, String> data = new HashMap<String, String>();

	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		setContentView(R.layout.main);
		
		name = "QBShare";
		url = "http://www.digimouth.com/news/media/2011/09/Google1.jpg";
		description = "Test_description";
		
		
		data.put("n", name);
		data.put("u", url);
		data.put("d", description);
		
		Button bShare = (Button)findViewById(R.id.bShare);
		bShare.setOnClickListener(new OnClickListener() {
			
			@Override
			public void onClick(View v) {
			
				QBShareManager manager = new QBShareManager( QBShareActivity.this, data);
				manager.displayShareDialog();
			}
		});
	}
}
