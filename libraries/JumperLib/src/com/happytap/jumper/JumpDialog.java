package com.happytap.jumper;

import java.util.Iterator;
import java.util.LinkedHashSet;

import android.app.Dialog;
import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.TypedValue;
import android.view.Display;
import android.view.Gravity;
import android.view.View;
import android.view.Window;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.LinearLayout.LayoutParams;

public class JumpDialog extends Dialog {

	private LinkedHashSet<Character> primaryCharacters;

	private LinkedHashSet<Character> enabledCharacters;

	private LinkedHashSet<Character> secondaryCharacters;

	private LinkedHashSet<Character> currentCharacters;

	private JumpListener listener;
	
	private boolean useHapticFeedback = true;
	
	private Character primaryCharacter;
	private Character secondaryCharacter;
	
	private ColorStateList colorStateList;
	
	public void setColorStateList(ColorStateList colorStateList) {
		this.colorStateList = colorStateList;
	}

	public boolean isUseHapticFeedback() {
		return useHapticFeedback;
	}

	public void setUseHapticFeedback(boolean useHapticFeedback) {
		this.useHapticFeedback = useHapticFeedback;
	}

	
	public LinkedHashSet<Character> getPrimaryCharacters() {
		return primaryCharacters;
	}

	public void setPrimaryCharacters(LinkedHashSet<Character> primaryCharacters) {
		this.primaryCharacters = primaryCharacters;
	}

	public LinkedHashSet<Character> getSecondaryCharacters() {
		return secondaryCharacters;
	}

	public void setSecondaryCharacters(LinkedHashSet<Character> secondaryCharacters) {
		this.secondaryCharacters = secondaryCharacters;
	}

	public JumpDialog(Context context, boolean cancelable,
			OnCancelListener cancelListener, JumpListener listener) {
		super(context, cancelable, cancelListener);
		this.listener = listener;
	}

	public JumpDialog(Context context, int theme, JumpListener listener) {
		super(context, theme);
		this.listener = listener;
	}

	public JumpDialog(Context context, JumpListener listener) {
		super(context);
		this.listener = listener;
	}

	@Override
	protected void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);
		requestWindowFeature(Window.FEATURE_NO_TITLE);
		ColorDrawable colorDrawable = new ColorDrawable(0xA0000000);
		getWindow().setBackgroundDrawable(colorDrawable);
		setContentView(R.layout.jumper);
	}
	
	@Override
	public void onAttachedToWindow() {
		if (currentCharacters == null || currentCharacters.isEmpty()) {
			initializePrimaryCharacters();
			initializeSecondaryCharacters();
			initializeEnabledCharacters();
			currentCharacters = primaryCharacters;
		}
		LinearLayout linearLayout = (LinearLayout) findViewById(R.id.jumper_root);
		linearLayout.removeAllViews();
		LinearLayout row = null;
		Display display = getWindow().getWindowManager().getDefaultDisplay();
		int perRow;
		if (display.getHeight() < display.getWidth()) {			
				perRow = 7;
		} else {
			perRow = 4;
		}
		int currentCharactersSize = currentCharacters.size();
		if(currentCharacters.equals(primaryCharacters)) {
			if(secondaryCharacter!=null) {
				currentCharactersSize++;
			}
		} else
		if(currentCharacters==secondaryCharacters) {
			if(primaryCharacter!=null) {
				currentCharactersSize++;
			}
		}
		if(perRow>(.4*currentCharactersSize)) {
			perRow = currentCharactersSize / 2;
		}
		int rows = Math.round(currentCharactersSize / perRow);
		int height = display.getHeight() / rows;
		Iterator<Character> iterator = currentCharacters.iterator();
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Character character = ((TextView) v).getText().charAt(0);
				if(character.equals(secondaryCharacter) && currentCharacters.equals(primaryCharacters)) {
					currentCharacters = secondaryCharacters;
					cancel();
					show();
				} else
				if(character.equals(primaryCharacter) && currentCharacters.equals(secondaryCharacters)) {
					currentCharacters = primaryCharacters;
					cancel();
					show();
				} else {
					JumpDialog.this.cancel();
					if (JumpDialog.this.listener != null) {
						JumpDialog.this.listener.onJump(character);
					}
				}
			}
		};
		for (int i = 0; i < currentCharactersSize; i++) {
			if (i % perRow == 0) {
				LayoutParams lp = new LayoutParams(display.getWidth(), height,
						1);
				row = new LinearLayout(getContext());
				row.setLayoutParams(lp);
				row.setBackgroundColor(Color.YELLOW);
				row.setBackgroundDrawable(new ColorDrawable(0));
				row.setOrientation(LinearLayout.HORIZONTAL);
				linearLayout.addView(row);
			}
			TextView textView = new TextView(getContext());
			textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP, 52);
			textView.setGravity(Gravity.CENTER);
			final Character character;
			if(iterator.hasNext()) {
				character = iterator.next();
			} else {
				if(currentCharacters.equals(primaryCharacters)) {
					character = secondaryCharacter;
				} else {
					character = primaryCharacter;
				}
			}
			
			textView.setText(String.valueOf(character.toString()));		
			textView.setLayoutParams(new LinearLayout.LayoutParams(
					LayoutParams.FILL_PARENT, LayoutParams.FILL_PARENT, 1));
			if(colorStateList!=null) {					
				textView.setTextColor(colorStateList);
			} else {
				textView.setTextColor(Color.WHITE);
			}
			textView.setHapticFeedbackEnabled(useHapticFeedback);
			if (enabledCharacters.contains(character) || isDaryCharacter(character)) {
				textView.setEnabled(true);
				textView.setOnClickListener(listener);
			} else {
				textView.setOnClickListener(null);
				textView.setEnabled(false);
				textView.setTextSize(TypedValue.COMPLEX_UNIT_DIP,30);
			}

			row.addView(textView);
		}
	}
	
	private boolean isDaryCharacter(Character c) {
		if(currentCharacters.equals(primaryCharacters) && c.equals(secondaryCharacter)) {
			return true;
		}
		if(currentCharacters.equals(secondaryCharacters) && c.equals(primaryCharacter)) {
			return true;
		}
		return false;
	}

	private void initializePrimaryCharacters() {
		if(primaryCharacters==null) {
			int A = (int) 'A';
			int Z = (int) 'Z';
			primaryCharacters = new LinkedHashSet<Character>();
			for (int i = A; i <= Z; i++) {
				Character character = Character.valueOf((char) i);
				primaryCharacters.add(character);
			}
		}
		initializeSecondaryCharacter();
//		if(!primaryCharacters.contains(secondaryCharacter)) {
//			primaryCharacters.add(secondaryCharacter);
//		}
	}

	private void initializeSecondaryCharacters() {
		if(secondaryCharacters==null) {
			secondaryCharacters = new LinkedHashSet<Character>();
			for (int i = 0; i <= 9; i++) {
				secondaryCharacters.add(Character.valueOf(Character.forDigit(i, 10)));
			}
		}
		initializePrimaryCharacter();
//		if(!secondaryCharacters.contains(primaryCharacter)) {
//			secondaryCharacters.add(primaryCharacter);
//		}
	}
	
	private void initializeEnabledCharacters() {
		if(enabledCharacters==null) {
			enabledCharacters = new LinkedHashSet<Character>();
			enabledCharacters.addAll(primaryCharacters);
			enabledCharacters.addAll(secondaryCharacters);
		}
	}
	
	public LinkedHashSet<Character> getEnabledCharacters() {
		return enabledCharacters;
	}

	public void setEnabledCharacters(LinkedHashSet<Character> enabledCharacters) {
		this.enabledCharacters = enabledCharacters;
	}

	private void initializePrimaryCharacter() {
		if(primaryCharacter==null) {
			primaryCharacter = 'A';
		}
	}
	
	private void initializeSecondaryCharacter() {
		if(secondaryCharacter==null) {
			secondaryCharacter = '#';
		}
	}

	public Character getPrimaryCharacter() {
		return primaryCharacter;
	}

	public void setPrimaryCharacter(Character primaryCharacter) {
		this.primaryCharacter = primaryCharacter;
	}

	public Character getSecondaryCharacter() {
		return secondaryCharacter;
	}

	public void setSecondaryCharacter(Character secondaryCharacter) {
		this.secondaryCharacter = secondaryCharacter;
	}

}
