package us.wmwm.happyschedule.api;

import java.util.List;

import us.wmwm.happyschedule.fragment.ChatFragment;

/**
 * Created by gravener on 12/20/14.
 */
public class JoinResponse extends Response {

    public List<User> data;
    public User self;
    public List<ChatFragment.Message> history;

}
