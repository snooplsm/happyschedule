package us.wmwm.happyschedule.views;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.squareup.picasso.Picasso;

import java.text.SimpleDateFormat;
import java.util.Date;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.ChatFragment;

/**
 * Created by gravener on 12/13/14.
 */
public class FacebookView extends LinearLayout {

    ImageView avatar;
    TextView name;
    TextView ago;
    TextView text;

    public FacebookView(Context ctx) {
        super(ctx);
        LayoutInflater.from(ctx).inflate(R.layout.view_facebook, this);
        View view = this;
        avatar = (ImageView) view.findViewById(us.wmwm.happyschedule.R.id.avatar);
        name = (TextView) view.findViewById(us.wmwm.happyschedule.R.id.name);
        //ago = (TextView) view.findViewById(us.wmwm.happyschedule.R.id.ago);
        //screenname = (TextView) view.findViewById(us.wmwm.happyschedule.R.id.screenname);
        text = (TextView) view.findViewById(R.id.text);
    }

    static SimpleDateFormat time = new SimpleDateFormat("h:mm a");

    public void setData(boolean showNick, ChatFragment.Message m, ChatFragment.Message newest) {
        String[] data = m.text.split("\\|");
        String id = data[0];
        String message = data[1];
        name.setText(m.name + (newest==null?"":" "+ time.format(new Date(newest.timestamp)).toLowerCase()));
        int nameVis = showNick?View.GONE:View.VISIBLE;
        name.setVisibility(nameVis);
        text.setText(message);
        //ago.setText();
        Picasso.with(getContext())
                .load(id)   .into(avatar);
    }

}
