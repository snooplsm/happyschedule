package us.wmwm.happyschedule.activity;

import java.util.Collections;

import twitter4j.Status;
import twitter4j.TwitterException;
import twitter4j.User;
import twitter4j.json.DataObjectFactory;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentTweet;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.flurry.android.FlurryAgent;

public class TweetActivity extends FragmentActivity {

	@Override
	protected void onCreate(Bundle bundle) {
		super.onCreate(bundle);
		setContentView(R.layout.activity_tweet);
		Uri u = getIntent().getData();
		String json = u.getQueryParameter("tweet");
		Status status;
		try {
			status = (Status) DataObjectFactory.createStatus(json);
			User user = status.getUser();
			String url = "https://twitter.com/%s/status/%s";
			FlurryAgent.logEvent("TweetActivity", Collections.singletonMap("url", String.format(url,user.getScreenName(),status.getId())));
		} catch (TwitterException e) {
			throw new RuntimeException(e);
		}
		getSupportFragmentManager().beginTransaction().replace(R.id.fragment_tweet, FragmentTweet.newInstance(status)).commit();
		getActionBar().setDisplayHomeAsUpEnabled(true);
		getActionBar().setHomeButtonEnabled(true);
	}
	
}
