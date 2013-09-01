package us.wmwm.happyschedule.activity;

import us.wmwm.happyschedule.R;
import us.wmwm.happyschedule.fragment.FragmentRaiLines;
import android.os.Bundle;
import android.view.MenuItem;

public class RailLinesActivity extends HappyActivity {

	@Override
	protected void onCreate(Bundle arg0) {
		// TODO Auto-generated method stub
		super.onCreate(arg0);
		setContentView(R.layout.activity_rail_lines);
		getSupportFragmentManager().beginTransaction().replace(R.id.rail_fragment,new FragmentRaiLines()).commit();
		getActionBar().setSubtitle(getString(R.string.activity_name_rail_alerts_name));
		getActionBar().setHomeButtonEnabled(true);
		getActionBar().setDisplayHomeAsUpEnabled(true);
	}
	

	
}
