package us.wmwm.happyschedule.views;

import android.graphics.Bitmap;
import android.graphics.Bitmap.Config;
import android.graphics.Canvas;
import android.graphics.drawable.BitmapDrawable;
import android.util.Log;
import android.view.DragEvent;
import android.view.View;
import android.view.View.OnDragListener;

public abstract class ClipDrawListener implements OnDragListener {

	//BitmapDrawable bitmap;
	
	private static final String TAG = ClipDrawListener.class.getSimpleName();

	public ClipDrawListener(View view) {
		Bitmap b = Bitmap.createBitmap(Math.round(view.getWidth()*1.5f), Math.round(view.getHeight()*1.5f),
				Config.ARGB_4444);
		//Canvas canvas = new Canvas(b);		
		//view.draw(canvas);
		//canvas.scale(3f, 3f,b.getWidth()/2.0f,b.getHeight()/2.0f);
		//canvas.rotate(50);
		//bitmap = new BitmapDrawable(view.getContext().getResources(), b);
	}
	
	float xStart;
	
	protected abstract void onDrop(float xStart, View view, DragEvent event);
	protected abstract void onStart(float xStart, View view, DragEvent event);

	@Override
	public boolean onDrag(View v, DragEvent event) {
		int action = event.getAction();
		Log.d(TAG,"ACTION: " + action);
		switch (event.getAction()) {
		case DragEvent.ACTION_DRAG_STARTED:
			// Do nothing
			xStart = event.getX();
			Log.d(TAG,"STARTED");
			onStart(xStart, v, event);
			break;
		case DragEvent.ACTION_DRAG_ENTERED:
			Log.d(TAG,"ENTERED");
			//v.setBackground(bitmap);
			break;
		case DragEvent.ACTION_DRAG_EXITED:
			Log.d(TAG,"EXITED");
			//v.setBackground(null);
			v.setVisibility(View.VISIBLE);
			break;
		case DragEvent.ACTION_DROP:
			Log.d(TAG,"DROP");
			onDrop(xStart, v,event);			
			// Dropped, reassign View to ViewGroup
//			View view = (View) event.getLocalState();
//			ViewGroup owner = (ViewGroup) view.getParent();
//			owner.removeView(view);
//			LinearLayout container = (LinearLayout) v;
//			container.addView(view);
			//v.setVisibility(View.VISIBLE);
			break;
		case DragEvent.ACTION_DRAG_ENDED:
			Log.d(TAG,"ENDED");
		default:
			break;
		}
		return true;
	}

}
