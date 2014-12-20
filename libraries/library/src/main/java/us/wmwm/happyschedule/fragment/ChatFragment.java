package us.wmwm.happyschedule.fragment;

import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.util.StateSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.view.inputmethod.EditorInfo;
import android.widget.AdapterView;
import android.widget.BaseAdapter;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.facebook.LoggingBehavior;
import com.facebook.Request;
import com.facebook.Response;
import com.facebook.Session;
import com.facebook.SessionState;
import com.facebook.Settings;
import com.facebook.UiLifecycleHelper;
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;

import org.jibble.pircbot.ConnectionSettings;
import org.jibble.pircbot.IrcException;
import org.jibble.pircbot.NickAlreadyInUseException;
import org.jibble.pircbot.PircBot;
import org.jibble.pircbot.User;

import java.io.IOException;
import java.sql.Connection;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.api.Api;
import us.wmwm.happyschedule.util.ImageUtil;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.FacebookView;
import us.wmwm.happyschedule.views.TweetView;

/**
 * Created by gravener on 12/11/14.
 */
public class ChatFragment extends HappyFragment implements IPrimary {

    RailBot bot;

    private static final String TAG = ChatFragment.class.getSimpleName();

    boolean enabled = false;

    Session.StatusCallback statusCallback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (state == SessionState.OPENED) {

                if (bot == null) {
                    bot = new RailBot();
                }
                bot.scheduleReconnect();

            }
        }
    };

    ListView list,users;

    NotificationManagerCompat manager;

    Api api;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if(BuildConfig.DEBUG) {
            Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        api = new Api(getActivity());
        manager = NotificationManagerCompat.from(getActivity());
        manager.cancel(6000);
        Session session = Session.getActiveSession();
        if (session == null) {
            if (savedInstanceState != null) {
                session = Session.restoreSession(getActivity(), null, statusCallback, savedInstanceState);
            }
            if (session == null) {
                session = new Session(getActivity());
            }
            Session.setActiveSession(session);
            if (session.getState().equals(SessionState.CREATED_TOKEN_LOADED)) {
                session.openForRead(new Session.OpenRequest(this).setCallback(statusCallback));
            }
        }

        getActivity().registerReceiver(chatReceiver,new IntentFilter(getAction()));

    }

    BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            Log.d(TAG,"chatReceiver");
            if(notification!=null) {
                manager.cancel(6000);
            }
            ThreadHelper.getScheduler().submit(new Runnable() {
                @Override
                public void run() {
                    if(bot!=null) {
                        isDestroying = true;
                        bot.disconnect();
                    }
                }
            });
        }
    };

    View loginContainer;
    ImageView send;
    LoginButton loginButton;
    SlidingPaneLayout slidingPane;
    View textContainer;

    ImageView usersText;
    View progress;
    TextView progressText;

    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_chat, container, false);
        loginButton = (LoginButton) view.findViewById(R.id.login_button);
        loginContainer = view.findViewById(R.id.login_container);
        list = (ListView) view.findViewById(R.id.list);
        users = (ListView) view.findViewById(R.id.users);
        text = (EditText) view.findViewById(R.id.text);
        usersText = (ImageView) view.findViewById(R.id.users_text);
        textContainer = view.findViewById(R.id.text_container);
        send = (ImageView) view.findViewById(R.id.send);
        slidingPane = (SlidingPaneLayout) view.findViewById(R.id.slidingPane);
        send.setColorFilter(getResources().getColor(R.color.get_schedule_11));
        chatHeader = (DepartureVisionHeader) view.findViewById(R.id.chat_header);
        userHeader = (DepartureVisionHeader) view.findViewById(R.id.user_header);
        progress = view.findViewById(R.id.progress);
        progressText = (TextView) view.findViewById(R.id.progressText);
        return view;
    }

    DepartureVisionHeader chatHeader;
    DepartureVisionHeader userHeader;

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        updateView();
        list.setAdapter(adapter = new ChatAdapter());
        users.setAdapter(userAdapter = new UserAdapter());
        usersText.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (slidingPane.isOpen()) {
                    usersText.setSelected(false);
                    slidingPane.closePane();

                } else {
                    usersText.setSelected(true);
                    if (bot != null) {
                        userAdapter.setData(bot.getUsers());
                    }
                    slidingPane.openPane();
                }
            }
        });
        slidingPane.setPanelSlideListener(new SlidingPaneLayout.PanelSlideListener() {
            @Override
            public void onPanelSlide(View panel, float slideOffset) {

            }

            @Override
            public void onPanelOpened(View panel) {
                textContainer.setVisibility(View.GONE);
            }

            @Override
            public void onPanelClosed(View panel) {
                textContainer.setVisibility(View.VISIBLE);
            }
        });
        users.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                User user = userAdapter.getItem(position);
                if(text.getText().toString().trim().length()==0) {
                    text.setText("@"+user.getNick()+": ");
                }
                slidingPane.closePane();
            }
        });
        StateListDrawable d = new StateListDrawable();
        d.addState(new int []{android.R.attr.state_selected},getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        d.addState(StateSet.WILD_CARD,new BitmapDrawable(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(),R.raw.people,getResources().getColor(R.color.get_schedule_11))));
        chatHeader.setData("CHAT");
        userHeader.setData("USERS");
        usersText.setImageDrawable(d);
    }

    private void updateView() {
        Session session = Session.getActiveSession();
        if (session.isOpened()) {
            //textInstructionsOrLink.setText(URL_PREFIX_FRIENDS + session.getAccessToken());
            loginContainer.setVisibility(View.GONE);
            loginButton.setVisibility(View.GONE);
            //loginButton.setText("Logout");
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onClickLogout();
                }
            });
        } else {
            loginContainer.setVisibility(View.VISIBLE);
            loginButton.setVisibility(View.VISIBLE);
            //loginButton.setText("Login");
            loginButton.setOnClickListener(new View.OnClickListener() {
                public void onClick(View view) {
                    onClickLogin();
                }
            });
        }
    }

    private void onClickLogin() {

    }

    private void onClickLogout() {

    }

    @Override
    public void setPrimaryItem() {
        enabled = true;
        if (bot == null && Session.getActiveSession().isOpened()) {
            bot = new RailBot();
            if(BuildConfig.DEBUG) {
                bot.setVerbose(true);
            }
            bot.scheduleReconnect();
        }
    }


    ChatAdapter adapter;
    UserAdapter userAdapter;

    GraphUser user = null;

    EditText text;

    boolean isDestroying;

    class RailBot extends PircBot {

        Future<?> reconnect;

        int next = 0;

        public RailBot() {
            setAutoNickChange(true);
        }

        public List<User> getUsers() {
            String[] chans = getChannels();
            if(chans==null || chans.length==0) {
                return Collections.emptyList();
            }
            List<User> users = Arrays.asList(getUsers(chans[0]));
            Collections.sort(users, new Comparator<User>() {
                @Override
                public int compare(User lhs, User rhs) {
                    return lhs.getNick().compareToIgnoreCase(rhs.getNick());
                }

            });
            return users;
        }

        @Override
        protected void onDisconnect() {
            Log.d(TAG, "onDisconnect");
            super.onDisconnect();
            if(isDestroying) {
                return;
            }
            Message m = new Message();
            m.type = Type.DISCONNECTED;
            m.nick = null;
            m.message = "<disconnected>";
            m.timestamp = System.currentTimeMillis();
            adapter.addData(m);
            scheduleReconnect();
        }

        @Override
        protected void onConnect() {
            super.onConnect();
            next /= 4;
            Log.d(TAG, "onConnect, joining " + getChannel());
            handler.post(new Runnable() {
                @Override
                public void run() {
                    progress.setVisibility(View.VISIBLE);
                    progressText.setText("joining room");
                }
            });
            reconnect = ThreadHelper.getScheduler().submit(new Runnable() {
                @Override
                public void run() {
                    joinChannel(getChannel());
                }
            });
        }

        @Override
        protected void onUserList(String s, User[] users) {
            super.onUserList(s, users);
            Message m = new Message();
            m.timestamp = System.currentTimeMillis();
            m.message = "<there are " + users.length + " in the chat>";
            m.type = Type.USER_COUNT;
            adapter.addData(m);
            userAdapter.setData(Arrays.asList(users));
        }

        @Override
        protected void onPart(String s, String sender, String s3, String s4) {
            super.onPart(s, sender, s3, s4);
            Message m = new Message();
            m.type = Type.DISCONNECTED;
            m.nick = sender;
            m.timestamp = System.currentTimeMillis();
            m.message = "<" + m.nick + " disconnected>";
            adapter.addData(m);
            userAdapter.setData(bot.getUsers());
        }


        @Override
        protected void onJoin(String channel, String sender, String login, String hostname) {
            super.onJoin(channel, sender, login, hostname);

            Message m = new Message();
            m.type = Type.CONNECTED;
            m.nick = sender;
            m.message = "joined";
            m.timestamp = System.currentTimeMillis();
            if (sender.equals(bot.getNick())) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        progress.setVisibility(View.GONE);
                        progressText.setVisibility(View.GONE);
                    }
                });
                m.message = "<connected>";
            } else {
                m.message = "<" + m.nick + " joined>";
            }
            userAdapter.setData(bot.getUsers());
            adapter.addData(m);
        }

        @Override
        protected void onMessage(String channel, String sender, String login, String hostname, String message) {
            super.onMessage(channel, sender, login, hostname, message);
            Message m = new Message();
            m.type = Type.MESSAGE;
            m.nick = sender;
            m.message = message;
            m.timestamp = System.currentTimeMillis();
            adapter.addData(m);
            Log.d(TAG, "message: " + message);
        }

        private void scheduleReconnect() {
            if (reconnect != null) {
                reconnect.cancel(true);
            }
            if (isConnected()) {
                return;
            }
            reconnect = ThreadHelper.getScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    handler.post(new Runnable() {
                        @Override
                        public void run() {
                            progress.setVisibility(View.VISIBLE);
                            progressText.setText("connecting...");
                        }
                    });
                    try {
                        if (user == null) {
                            Request.executeAndWait(Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {

                                @Override
                                public void onCompleted(GraphUser u, Response response) {
                                    user = u;
                                    setName(u.getFirstName() + u.getLastName().substring(0, 1));
                                    setRealName(u.getId());
                                    setFinger(u.getInnerJSONObject().toString());
                                    ThreadHelper.getScheduler().submit(new Runnable() {
                                        @Override
                                        public void run() {
                                            try {
                                                api.registerFacebookUser(user, SettingsFragment.getRegistrationId());
                                                Log.d(TAG,"saved facebook user on server");
                                            } catch (Exception e) {
                                                Log.e(TAG,"erorr saving user",e);
                                            }
                                        }
                                    });
                                }
                            }));
                        }
                        ConnectionSettings s = new ConnectionSettings("irc.freenode.net");
                        s.useSSL = true;
                        s.port = 6697;
                        Log.d(TAG, "connecting to " + s.server + ":" + s.port);
                        connect(s);
                        Log.d(TAG, "connected");
                    } catch (IOException e) {
                        Log.e(TAG, "error connecting", e);
                        scheduleReconnect();
                    } catch (NickAlreadyInUseException e) {
                        Log.e(TAG, "username already in use: " + getNick());
                        scheduleReconnect();
                    } catch (IrcException e) {
                        Log.e(TAG, "error connecting", e);
                        scheduleReconnect();
                    }
                }
            }, next, TimeUnit.MILLISECONDS);
            next *= 2;
            Log.d(TAG, "scheduleReconnect in " + next + " milliseconds");
        }
    }

    Notification notification;


    private String getAction() {
        return getActivity().getPackageName() + ".DisconnectChat";
    }

    @Override
    public void onPause() {
        super.onPause();
        if(bot!=null) {
            if(notification==null) {
                NotificationCompat.Builder b = new NotificationCompat.Builder(getActivity());
                b.setContentTitle(getString(us.wmwm.happyschedule.views.R.string.app_name));
                b.setContentText("Chat is active");
                Intent i = new Intent(getAction());
                Intent activity = new Intent(getActivity(), getActivity().getClass());
                PendingIntent pi = PendingIntent.getBroadcast(getActivity(), 0, i, 0);
                b.addAction(R.drawable.ic_action_cancel, "Disconnect", pi);
                b.setOngoing(true);
                b.setSmallIcon(R.drawable.ic_stat_512);
                PendingIntent pendingActivity = PendingIntent.getActivity(getActivity(), 0, activity, 0);
                b.setContentIntent(pendingActivity);
                notification = b.build();
            }
            manager.notify(6000, notification);
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isDestroying = false;
        if(bot!=null && !bot.isConnected()) {
            bot.next = 0;
            bot.scheduleReconnect();
        }
        updateView();
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
        Session session = Session.getActiveSession();
        Session.saveSession(session, outState);

    }

    @Override
    public void onStop() {
        super.onStop();
        Session.getActiveSession().removeCallback(statusCallback);
    }

    @Override
    public void onStart() {
        isDestroying = false;
        super.onStart();
        Session.getActiveSession().addCallback(statusCallback);
    }

    @Override
    public void onDestroy() {
        isDestroying = true;
        if(notification!=null) {
            manager.cancel(notification.hashCode());
        }
        getActivity().unregisterReceiver(chatReceiver);
        super.onDestroy();
        ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                if(bot!=null) {
                    bot.disconnect();
                }
            }
        });
    }

    private String getChannel() {
        return "#"+getResources().getString(R.string.promotional_account);
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (text.getText().toString().trim().length() == 0) {
                    return;
                }
                if(!bot.isConnected()) {
                    Toast.makeText(getActivity(),"You are not connected yet.",Toast.LENGTH_SHORT).show();;
                    return;
                }
                bot.sendMessage(getChannel(), user.getId()+"|"+ text.getText().toString());
                Message m = new Message();
                m.type = Type.MESSAGE;
                m.nick = bot.getNick();
                m.message = user.getId()+"|"+text.getText().toString();
                m.timestamp = System.currentTimeMillis();
                adapter.addData(m);
                Log.d(TAG, "message: " + m.message);
                text.setText("");

            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
    }

    public class Message {
        public String nick;
        public String message;
        public long timestamp;
        public Type type;
    }

    enum Type {
        MESSAGE, CONNECTED, DISCONNECTED, USER_COUNT
    }

    class UserAdapter extends BaseAdapter {

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public User getItem(int position) {
            return users.get(position);
        }

        List<User> users = new ArrayList<User>();

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
//            int type = getItemViewType(position);
//            if(type==0) {
//                DepartureVisionHeader header = (DepartureVisionHeader)convertView;
//                if(header==null) {
//                    header = new DepartureVisionHeader(parent.getContext(),null);
//                }
//                header.setData("USERS");
//                return header;
//            }

            TextView t = (TextView) convertView;
            if (t == null) {
                t = new TextView(parent.getContext());
            }
            User u = users.get(position);
            t.setText(u.getNick());
            return t;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            if(position==0) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getCount() {
            return users.size();
        }

        public void setData(final List<User> us) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Log.d(TAG,"setting users: " + us.size());
                    users = us;
                    notifyDataSetChanged();
                }
            });
        }
    }

    Handler handler = new Handler();

    class ChatAdapter extends BaseAdapter {

        List<Message> messages = new ArrayList<Message>();

        Map<String,Message> nickToMessage = new HashMap<String,Message>();

        SimpleDateFormat TIME = new SimpleDateFormat("hh:mm");

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public Message getItem(int position) {
            return messages.get(position);
        }

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public int getItemViewType(int position) {
            Message m = messages.get(position);
            if(m.type==Type.MESSAGE) {
                return 0;
            }
            return 1;
        }

        @Override
        public int getViewTypeCount() {
            return 2;
        }

        @Override
        public View getView(int position, View convertView, ViewGroup parent) {
            int type = getItemViewType(position);
            Message m = getItem(position);
            if(type==0) {
                FacebookView t = (FacebookView) convertView;
                if (t == null) {
                    t = new FacebookView(parent.getContext());
                }
                boolean hasPrevious = false;

                if(position>0) {
                    Message prev = getItem(position - 1);
                    if(prev.type==Type.MESSAGE && prev.nick.equals(m.nick)) {
                        hasPrevious = true;
                    }
                }
                t.setData(hasPrevious, m,nickToMessage.get(m.nick));
                return t;
            }
            TextView t = (TextView) convertView;
            if(t==null) {
                t = new TextView(parent.getContext());
            }
            else {
                t.setGravity(Gravity.CENTER_HORIZONTAL);
                if (m.type == Type.CONNECTED) {
                    t.setText(m.message);
                }
                if (m.type == Type.DISCONNECTED) {
                    t.setText("<disconnected>");
                }
            }
            return t;
        }

        public void addData(final Message message) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    messages.add(message);
                    if(message.type==Type.MESSAGE) {
                        nickToMessage.put(message.nick, message);
                    }
                    notifyDataSetChanged();
                    list.setSelection(0);
                }
            });

        }
    }
}
