package us.wmwm.happyschedule.fragment;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.StateListDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.widget.SlidingPaneLayout;
import android.util.Log;
import android.util.StateSet;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
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
import com.facebook.model.GraphUser;
import com.facebook.widget.LoginButton;
import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.plus.Plus;
import com.google.android.gms.plus.model.people.Person;
import com.google.gson.Gson;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.Future;

import us.wmwm.happyschedule.BuildConfig;
import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.ThreadHelper;
import us.wmwm.happyschedule.api.Api;
import us.wmwm.happyschedule.api.JoinResponse;
import us.wmwm.happyschedule.api.MessageResponse;
import us.wmwm.happyschedule.api.User;
import us.wmwm.happyschedule.application.HappyApplication;
import us.wmwm.happyschedule.dao.WDb;
import us.wmwm.happyschedule.util.ImageUtil;
import us.wmwm.happyschedule.views.DepartureVisionHeader;
import us.wmwm.happyschedule.views.FacebookView;

/**
 * Created by gravener on 12/11/14.
 */
public class ChatFragment extends HappyFragment implements IPrimary {

    private static final String TAG = ChatFragment.class.getSimpleName();

    boolean enabled = false;

    Calendar connected;

    Session.StatusCallback statusCallback = new Session.StatusCallback() {

        @Override
        public void call(Session session, SessionState state, Exception exception) {
            if (state == SessionState.OPENED) {
                if (isConnected() == false) {
                    scheduleReconnect();
                }
            }
        }
    };
    Future<?> reconnect;
    User me;
    ListView list, users;
    NotificationManagerCompat manager;
    Api api;
    View loginContainer;
    ImageView send;
    LoginButton loginButton;
    SignInButton signInButton;
    SlidingPaneLayout slidingPane;
    View textContainer;
    ImageView usersText;
    View progress;
    TextView progressText;
    DepartureVisionHeader chatHeader;
    DepartureVisionHeader userHeader;
    ChatAdapter adapter;
    UserAdapter userAdapter;
    GraphUser user = null;
    Person person;

    EditText text;
    BroadcastReceiver chatReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getAction().endsWith("Disconnect")) {
                Log.d(TAG, "chatReceiver");
                WDb.get().savePreference("chatEnabled", Boolean.FALSE.toString());
            } else {
                if (me == null) {
                    return;
                }
                Gson gson = new Gson();
                Message message = gson.fromJson(intent.getStringExtra("message"), Message.class);
                if (message.userId.equals(me.id)) {
                    //confirmed sent
                    return;
                }
                message.type = Type.MESSAGE;
                adapter.addData(message);

                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                if (imm.isActive(text)) {
                    Log.d(TAG, "active keyboard");
                    list.invalidate();
                    list.setSelection(adapter.getCount() - 1);
                }
            }
        }
    };
    boolean isDestroying;
    Handler handler = new Handler();

    public static String getDisconnectAction() {
        return HappyApplication.get().getPackageName() + ".DisconnectChat";
    }

    private static String getMessageReceivedAction() {
        return HappyApplication.get().getPackageName() + ".MessageReceived";
    }

    public static Intent getMessageReceivedIntent(String messageJson) {
        Intent intent = new Intent(getMessageReceivedAction());
        intent.putExtra("message", messageJson);
        return intent;
    }

    private boolean isConnected() {
        Calendar now = Calendar.getInstance();
        now.add(Calendar.HOUR_OF_DAY, -1);
        if (connected == null) {
            return false;
        }
        if (connected.after(now)) {
            return true;
        }
        return false;
    }

    private void scheduleReconnect() {
        if (reconnect != null) {
            reconnect.cancel(true);
        }
        Log.d(TAG, "scheduleReconnect()");
        reconnect = ThreadHelper.getScheduler().submit(new Runnable() {
            @Override
            public void run() {
                if (person == null) {
                    Log.d(TAG, "getting facebook user");
                    person = Plus.PeopleApi.getCurrentPerson(client);
//                    Request.executeAndWait(Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
//
//                        @Override
//                        public void onCompleted(GraphUser u, Response response) {
//                            user = u;
//                            Log.d(TAG, "facebook user received " + u.getName());
//                            ThreadHelper.getScheduler().submit(new Runnable() {
//                                @Override
//                                public void run() {
//                                    try {
//                                        api.registerFacebookUser(user, SettingsFragment.getRegistrationId());
//                                        Log.d(TAG, "saved facebook user on server");
//                                    } catch (Exception e) {
//                                        Log.e(TAG, "erorr saving user", e);
//                                    }
//                                }
//                            });
//                        }
//                    }));
                }
                if (person != null) {
                    try {
                        Log.d(TAG, "joining chat ");
                        JoinResponse resp = api.joinChat(person);
                        Log.d(TAG, "chat joined " + resp.toString());
                        connected = Calendar.getInstance();
                        me = resp.self;
                        adapter.addData(resp);
                        userAdapter.setData(resp.data);
                        System.out.println(resp);
                    } catch (Exception e) {
                        Log.e(TAG, "error joining chat", e);
                    }
                }
            }
        });
    }



    GoogleApiClient.ConnectionCallbacks connectionCallbacks = new GoogleApiClient.ConnectionCallbacks() {

        @Override
        public void onConnected(Bundle bundle) {
            updateButton();
            if(!isConnected()) {
                scheduleReconnect();
            }
        }

        @Override
        public void onConnectionSuspended(int i) {
            Log.d(TAG,"onConnectionSuspended " + i);
        }
    };

    GoogleApiClient.OnConnectionFailedListener connectionFailedListener = new GoogleApiClient.OnConnectionFailedListener() {
        @Override
        public void onConnectionFailed(ConnectionResult connectionResult) {
            Log.d(TAG,"onConnectionFailed " + connectionResult.getErrorCode());
            if (!mIntentInProgress) {
                // Store the ConnectionResult so that we can use it later when the user clicks
                // 'sign-in'.
                mConnectionResult = connectionResult;

                if (mSignInClicked) {
                    // The user has already clicked 'sign-in' so we attempt to resolve all
                    // errors until the user is signed in, or they cancel.
                    resolveSignInError();
                }
            }
        }
    };

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        client = new GoogleApiClient.Builder(getActivity())
                .addConnectionCallbacks(connectionCallbacks)
                .addOnConnectionFailedListener(connectionFailedListener)
                .addScope(Plus.SCOPE_PLUS_LOGIN)
                .addApi(Plus.API)
                .build();
        if (BuildConfig.DEBUG) {
            Settings.addLoggingBehavior(LoggingBehavior.INCLUDE_ACCESS_TOKENS);
        }
        api = new Api(getActivity());
        manager = NotificationManagerCompat.from(getActivity());
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

        IntentFilter filter = new IntentFilter(getDisconnectAction());
        filter.addAction(getMessageReceivedAction());
        getActivity().registerReceiver(chatReceiver, filter);

    }

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
        signInButton = (SignInButton) view.findViewById(R.id.sign_in_button);
        return view;
    }

//    class RailBot extends PircBot {
//
//        Future<?> reconnect;
//
//        int next = 0;
//
//        public RailBot() {
//            setAutoNickChange(true);
//        }
//
//        public List<User> getUsers() {
//            String[] chans = getChannels();
//            if(chans==null || chans.length==0) {
//                return Collections.emptyList();
//            }
//            List<User> users = Arrays.asList(getUsers(chans[0]));
//            Collections.sort(users, new Comparator<User>() {
//                @Override
//                public int compare(User lhs, User rhs) {
//                    return lhs.getNick().compareToIgnoreCase(rhs.getNick());
//                }
//
//            });
//            return users;
//        }
//
//        @Override
//        protected void onDisconnect() {
//            Log.d(TAG, "onDisconnect");
//            super.onDisconnect();
//            if(isDestroying) {
//                return;
//            }
//            Message m = new Message();
//            m.type = Type.DISCONNECTED;
//            m.text = "<disconnected>";
//            m.timestamp = System.currentTimeMillis();
//            adapter.addData(m);
//            scheduleReconnect();
//        }
//
//        @Override
//        protected void onConnect() {
//            super.onConnect();
//            next /= 4;
//            Log.d(TAG, "onConnect, joining " + getChannel());
//            handler.post(new Runnable() {
//                @Override
//                public void run() {
//                    progress.setVisibility(View.VISIBLE);
//                    progressText.setText("joining room");
//                }
//            });
//            reconnect = ThreadHelper.getScheduler().submit(new Runnable() {
//                @Override
//                public void run() {
//                    joinChannel(getChannel());
//                }
//            });
//        }
//
//        @Override
//        protected void onUserList(String s, User[] users) {
//            super.onUserList(s, users);
//            Message m = new Message();
//            m.timestamp = System.currentTimeMillis();
//            m.text = "<there are " + users.length + " in the chat>";
//            m.type = Type.USER_COUNT;
//            adapter.addData(m);
//            userAdapter.setData(Arrays.asList(users));
//        }
//
//        @Override
//        protected void onPart(String s, String sender, String s3, String s4) {
//            super.onPart(s, sender, s3, s4);
//            Message m = new Message();
//            m.type = Type.DISCONNECTED;
//            m.timestamp = System.currentTimeMillis();
//            adapter.addData(m);
//            userAdapter.setData(bot.getUsers());
//        }
//
//
//        @Override
//        protected void onJoin(String channel, String sender, String login, String hostname) {
//            super.onJoin(channel, sender, login, hostname);
//
//            Message m = new Message();
//            m.type = Type.CONNECTED;
//            m.name = sender;
//            m.text = "joined";
//            m.timestamp = System.currentTimeMillis();
//            if (sender.equals(bot.getNick())) {
//                handler.post(new Runnable() {
//                    @Override
//                    public void run() {
//                        progress.setVisibility(View.GONE);
//                        progressText.setVisibility(View.GONE);
//                    }
//                });
//                m.text = "<connected>";
//            } else {
//                m.text = "<" + m.name + " joined>";
//            }
//            userAdapter.setData(bot.getUsers());
//            adapter.addData(m);
//        }
//
//        @Override
//        protected void onMessage(String channel, String sender, String login, String hostname, String text) {
//            super.onMessage(channel, sender, login, hostname, text);
//            Message m = new Message();
//            m.type = Type.MESSAGE;
//            m.name = sender;
//            m.text = text;
//            m.timestamp = System.currentTimeMillis();
//            adapter.addData(m);
//            Log.d(TAG, "text: " + text);
//        }
//
//        private void scheduleReconnect() {
//            if (reconnect != null) {
//                reconnect.cancel(true);
//            }
//            if (isConnected()) {
//                return;
//            }
//            reconnect = ThreadHelper.getScheduler().schedule(new Runnable() {
//                @Override
//                public void run() {
//                    handler.post(new Runnable() {
//                        @Override
//                        public void run() {
//                            progress.setVisibility(View.VISIBLE);
//                            progressText.setText("connecting...");
//                        }
//                    });
//                    try {
//                        if (user == null) {
//                            Request.executeAndWait(Request.newMeRequest(Session.getActiveSession(), new Request.GraphUserCallback() {
//
//                                @Override
//                                public void onCompleted(GraphUser u, Response response) {
//                                    user = u;
//                                    setName(u.getFirstName() + u.getLastName().substring(0, 1));
//                                    setRealName(u.getId());
//                                    setFinger(u.getInnerJSONObject().toString());
//                                    ThreadHelper.getScheduler().submit(new Runnable() {
//                                        @Override
//                                        public void run() {
//                                            try {
//                                                api.registerFacebookUser(user, SettingsFragment.getRegistrationId());
//                                                Log.d(TAG,"saved facebook user on server");
//                                            } catch (Exception e) {
//                                                Log.e(TAG,"erorr saving user",e);
//                                            }
//                                        }
//                                    });
//                                }
//                            }));
//                        }
//                        ConnectionSettings s = new ConnectionSettings("irc.freenode.net");
//                        s.useSSL = true;
//                        s.port = 6697;
//                        Log.d(TAG, "connecting to " + s.server + ":" + s.port);
//                        connect(s);
//                        Log.d(TAG, "connected");
//                    } catch (IOException e) {
//                        Log.e(TAG, "error connecting", e);
//                        scheduleReconnect();
//                    } catch (NickAlreadyInUseException e) {
//                        Log.e(TAG, "username already in use: " + getNick());
//                        scheduleReconnect();
//                    } catch (IrcException e) {
//                        Log.e(TAG, "error connecting", e);
//                        scheduleReconnect();
//                    }
//                }
//            }, next, TimeUnit.MILLISECONDS);
//            next *= 2;
//            Log.d(TAG, "scheduleReconnect in " + next + " milliseconds");
//        }
//    }

    @Override
    public void onActivityCreated(Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);
        client.connect();
        //updateView();
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
                if (text.getText().toString().trim().length() == 0) {
                    text.setText("@" + user.name + ": ");
                }
                slidingPane.closePane();
            }
        });
        StateListDrawable d = new StateListDrawable();
        d.addState(new int[]{android.R.attr.state_selected}, getResources().getDrawable(R.drawable.abc_ic_ab_back_mtrl_am_alpha));
        d.addState(StateSet.WILD_CARD, new BitmapDrawable(ImageUtil.loadBitmapFromSvgWithColorOverride(getActivity(), R.raw.people, getResources().getColor(R.color.get_schedule_11))));
        chatHeader.setData("CHAT");
        userHeader.setData("USERS");
        usersText.setImageDrawable(d);
        updateButton();
    }

//    private void updateView() {
//        Session session = Session.getActiveSession();
//        if (session.isOpened()) {
//            //textInstructionsOrLink.setText(URL_PREFIX_FRIENDS + session.getAccessToken());
//            loginContainer.setVisibility(View.GONE);
//            loginButton.setVisibility(View.GONE);
//            //loginButton.setText("Logout");
//            loginButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//                    onClickLogout();
//                }
//            });
//        } else {
//            loginContainer.setVisibility(View.VISIBLE);
//            loginButton.setVisibility(View.VISIBLE);
//            //loginButton.setText("Login");
//            loginButton.setOnClickListener(new View.OnClickListener() {
//                public void onClick(View view) {
//                    onClickLogin();
//                }
//            });
//        }
//    }

    private boolean mSignInClicked = false;

    public void updateButton() {
        if(!client.isConnected()) {
            signInButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!client.isConnecting()) {
                        mSignInClicked = true;
                        resolveSignInError();
                    }
                }
            });
        } else {
            loginContainer.setVisibility(View.GONE);
        }
    }

    private ConnectionResult mConnectionResult;

    private boolean mIntentInProgress = false;

    private int RC_SIGN_IN = 100067;

    private void resolveSignInError() {
        if (mConnectionResult.hasResolution()) {
            try {
                mIntentInProgress = true;
                getActivity().startIntentSenderForResult(mConnectionResult.getResolution().getIntentSender(),
                        RC_SIGN_IN, null, 0, 0, 0);
            } catch (IntentSender.SendIntentException e) {
                // The intent was canceled before it was sent.  Return to the default
                // state and attempt to connect to get an updated ConnectionResult.
                mIntentInProgress = false;
                client.connect();
            }
        }
    }

    private void onClickLogin() {

    }

    private void onClickLogout() {

    }

    @Override
    public void setPrimaryItem() {
        enabled = true;
        if (!isConnected() && client.isConnected()) {
            scheduleReconnect();
        }
    }

    @Override
    public void onPause() {
        super.onPause();
        if (isConnected()) {

        }
    }

    @Override
    public void onResume() {
        super.onResume();
        isDestroying = false;
        if(!client.isConnected() && !client.isConnecting()) {
            client.connect();
        } else
        if (!isConnected()) {
            scheduleReconnect();
        }
        //updateView();
        //updateButton();
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
        if(client.isConnected()) {
            client.disconnect();
        }
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
        getActivity().unregisterReceiver(chatReceiver);
        super.onDestroy();
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
                if (!isConnected()) {
                    Toast.makeText(getActivity(), "You are not connected yet.", Toast.LENGTH_SHORT).show();
                    ;
                    return;
                }
                final Message m = new Message();
                m.type = Type.MESSAGE_PENDING;
                m.name = me.name;
                m.userId = me.id;
                m.text = person.getImage().getUrl() + "|" + text.getText().toString();
                m.timestamp = System.currentTimeMillis();
                adapter.addData(m);
                ThreadHelper.getScheduler().submit(new Runnable() {
                    @Override
                    public void run() {
                        MessageResponse resp = api.sendMessage(m);
                        if (resp.code == 200) {
                            m.type = Type.MESSAGE;
                            adapter.notifyDataSetChanged();
                        }
                    }
                });
                InputMethodManager imm = (InputMethodManager) getActivity().getSystemService(
                        Context.INPUT_METHOD_SERVICE);
                imm.hideSoftInputFromWindow(text.getWindowToken(), 0);
                Log.d(TAG, "text: " + m.text);
                text.setText("");
                list.setSelection(adapter.getCount() - 1);
            }
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        Session.getActiveSession().onActivityResult(getActivity(), requestCode, resultCode, data);
        if(requestCode==RC_SIGN_IN) {
            if (resultCode != Activity.RESULT_OK) {
                mSignInClicked = false;
            }

            mIntentInProgress = false;

            if (!client.isConnecting()) {
                client.connect();
            }
        }
    }

    enum Type {
        MESSAGE, MESSAGE_PENDING, CONNECTED, DISCONNECTED, USER_COUNT
    }

    public class Message {
        public String userId;
        public String name;
        public String text;
        public long timestamp;
        public Type type;
    }

    class UserAdapter extends BaseAdapter {

        List<User> users = new ArrayList<User>();

        @Override
        public long getItemId(int position) {
            return 0;
        }

        @Override
        public User getItem(int position) {
            return users.get(position);
        }

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
            t.setText(u.name);
            return t;
        }

        @Override
        public int getViewTypeCount() {
            return 1;
        }

        @Override
        public int getItemViewType(int position) {
            if (position == 0) {
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
                    Log.d(TAG, "setting users: " + us.size());
                    users = us;
                    notifyDataSetChanged();
                }
            });
        }
    }

    GoogleApiClient client;

    class ChatAdapter extends BaseAdapter {

        List<Message> messages = new ArrayList<Message>();

        Map<String, Message> nickToMessage = new HashMap<String, Message>();

        SimpleDateFormat TIME = new SimpleDateFormat("hh:mm");

        @Override
        public int getCount() {
            return messages.size();
        }

        @Override
        public void notifyDataSetChanged() {
            if (Looper.myLooper() != Looper.getMainLooper()) {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        ChatAdapter.super.notifyDataSetChanged();
                    }
                });
            }
            super.notifyDataSetChanged();
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
            if (m.type == Type.MESSAGE || m.type == Type.MESSAGE_PENDING) {
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
            if (type == 0) {
                FacebookView t = (FacebookView) convertView;
                if (t == null) {
                    t = new FacebookView(parent.getContext());
                }
                boolean hasPrevious = false;

                if (position > 0) {
                    Message prev = getItem(position - 1);
                    if ((prev.type == Type.MESSAGE || prev.type == Type.MESSAGE_PENDING) && prev.userId.equals(m.userId)) {
                        hasPrevious = true;
                    }
                }
                t.setData(hasPrevious, m, nickToMessage.get(m.name));
                return t;
            }
            TextView t = (TextView) convertView;
            if (t == null) {
                t = new TextView(parent.getContext());
            } else {
                t.setGravity(Gravity.CENTER_HORIZONTAL);
                if (m.type == Type.CONNECTED) {
                    t.setText(m.text);
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
                    if (message.type == Type.MESSAGE) {
                        nickToMessage.put(message.name, message);
                    }
                    notifyDataSetChanged();
                }
            });

        }

        public void addData(final JoinResponse resp) {
            handler.post(new Runnable() {
                @Override
                public void run() {
                    Collections.reverse(resp.history);
                    for (Message m : resp.history) {
                        m.type = Type.MESSAGE;
                        messages.add(m);
                        nickToMessage.put(m.name, m);
                    }
                    notifyDataSetChanged();
                }
            });
        }
    }
}
