package us.wmwm.happyschedule.activity;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.json.DataObjectFactory;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentTweet;
import android.net.Uri;
import android.os.Bundle;

public class TweetActivity extends HappyActivity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_tweet);
		Uri u = getIntent().getData();
		String json = u.getQueryParameter("tweet");
		Status status;
		try {
			status = (Status) DataObjectFactory.createStatus(json);
		} catch (TwitterException e) {
			throw new RuntimeException(e);
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_tweet, FragmentTweet.newInstance(status)).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}
	
}
