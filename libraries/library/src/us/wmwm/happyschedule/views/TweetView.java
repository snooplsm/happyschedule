package us.wmwm.happyschedule.views;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.text.SpannableStringBuilder;
import android.text.style.ClickableSpan;
import android.text.util.Linkify;
import android.view.LayoutInflater;
import android.view.View;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.util.Calendar;

import twitter4j.Status;
import twitter4j.URLEntity;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentAlarmPicker;

/**
 * Created by gravener on 10/12/14.
 */
public class TweetView extends LinearLayout {

    ImageView avatar;
    TextView name;
    TextView screenname;
    TextView ago;
    TextView text;

    public TweetView(Context ctx) {
        super(ctx);
        LayoutInflater.from(ctx).inflate(R.layout.view_tweet,this);
        View view = this;
        avatar = (ImageView) view.findViewById(R.id.avatar);
        name = (TextView) view.findViewById(R.id.name);
        ago = (TextView) view.findViewById(R.id.ago);
        screenname = (TextView) view.findViewById(R.id.screenname);
        text = (TextView) view.findViewById(R.id.tweet_text);
    }

    public void setData(Status status) {
        name.setText(status.getUser().getName());
        String url = null;
        SpannableStringBuilder text = new SpannableStringBuilder(status.getText());
        if (status.getURLEntities() != null) {
            for (int i = status.getURLEntities().length - 1; i >= 0; i--) {
                final URLEntity e = status.getURLEntities()[i];
                url = e.getExpandedURL();
                ClickableSpan cs = new ClickableSpan() {

                    @Override
                    public void onClick(View widget) {
                        Intent i = new Intent(Intent.ACTION_VIEW, Uri.parse(e.getExpandedURL()));
                        getContext().startActivity(i);
                    }
                };
                text.replace(e.getStart(), e.getEnd(), e.getDisplayURL());
                text.setSpan(cs, e.getStart(), e.getStart() + e.getDisplayURL().length(), 0);
            }
        }
        screenname.setText("");
        this.text.setAutoLinkMask(Linkify.WEB_URLS|Linkify.PHONE_NUMBERS);
        this.text.setText(text);
        Calendar created = Calendar.getInstance();
        created.setTime(status.getCreatedAt());
        ago.setText(FragmentAlarmPicker.buildMessage(Calendar.getInstance(), created));
        Picasso.with(getContext())
                .load(status.getUser().getBiggerProfileImageURL()).into(avatar);
    }

}
