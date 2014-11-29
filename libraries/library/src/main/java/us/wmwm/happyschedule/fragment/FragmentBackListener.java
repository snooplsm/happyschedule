package us.wmwm.happyschedule.fragment;

import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.util.Log;

import java.util.ArrayList;
import java.util.List;
import java.util.TreeMap;

import us.wmwm.happyschedule.views.BackListener;

/**
 * Created by gravener on 10/12/14.
 */
public class FragmentBackListener implements BackListener {

    private static final String TAG = FragmentBackListener.class.getSimpleName();

    Fragment fragment;

    public FragmentBackListener(Fragment fragment) {
        this.fragment = fragment;
    }

    @Override
    public boolean onBack() {
        List<Fragment> fragments = fragment.getChildFragmentManager()
                .getFragments();
        if (fragments != null) {
            TreeMap<Integer, List<Fragment>> levelToFragment = new TreeMap<Integer, List<Fragment>>();
            levelToFragment.put(1, fragments);
            int currLevel = 1;
            while (true) {
                List<Fragment> frags = levelToFragment.get(currLevel);
                List<Fragment> fragments2 = new ArrayList<Fragment>();
                if (frags == null || frags.isEmpty()) {
                    break;
                }
                for (Fragment frag : frags) {
                    if (frag == null) {
                        continue;
                    }
                    if (frag.getChildFragmentManager() != null
                            && frag.getChildFragmentManager()
                            .getFragments() != null) {
                        fragments2.addAll(frag.getChildFragmentManager()
                                .getFragments());
                    }

                }
                if (fragments2.isEmpty()) {
                    break;
                }
                levelToFragment.put(++currLevel, fragments2);
            }

            for (Integer level : levelToFragment.descendingKeySet()) {
                List<Fragment> frags = levelToFragment.get(level);
                for (Fragment frag : frags) {
                    if (frag == null) {
                        // can somehow happen
                        continue;
                    }
                    Log.d(TAG, frag.getClass().getSimpleName());
                    if (BackListener.class.isAssignableFrom(frag.getClass())) {
                        BackListener back = (BackListener) frag;
                        if (back.onBack()) {
                            return true;
                        }
                    }
                }

            }

            FragmentManager m = fragment.getChildFragmentManager();
            if (m.getBackStackEntryCount() > 0) {
                m.popBackStack();
                return true;
            }

            // while (!fragStack.empty()) {
            // Fragment fragment = fragStack.pop();
            //
            // if (fragment.getChildFragmentManager().getFragments() !=
            // null) {
            // fragStack.addAll(fragment.getChildFragmentManager()
            // .getFragments());
            // }
            // }

            // if (fragment instanceof BackListener) {
            // BackListener backListener = (BackListener) fragment;
            // if (backListener.onBack()) {
            // return true;
            // }
            // }
        }
        return false;
    }
}
