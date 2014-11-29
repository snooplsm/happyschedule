package com.happytap.jumper;

import java.util.Iterator;
import java.util.LinkedHashSet;

import android.content.Context;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.TextView;

public class JumpView extends LinearLayout {

	public JumpView(Context context) {
		this(context, null);
	}

	
	public JumpView(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	public void initializeViews() {
		if (currentCharacters == null || currentCharacters.isEmpty()) {
			initializePrimaryCharacters();
			initializeSecondaryCharacters();
			initializeEnabledCharacters();
			currentCharacters = primaryCharacters;
		}
		setOrientation(LinearLayout.VERTICAL);
		LinearLayout linearLayout = this;
		linearLayout.removeAllViews();
		LinearLayout row = null;
		float height = getMeasuredHeight();
		float width = getMeasuredWidth();
		int perRow;
		if (width <= height) {			
				perRow = 4;
		} else {
			perRow = 7;
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
		height = height / rows;
		Iterator<Character> iterator = currentCharacters.iterator();
		View.OnClickListener listener = new View.OnClickListener() {

			@Override
			public void onClick(View v) {
				Character character = ((TextView) v).getText().charAt(0);
				if(character.equals(secondaryCharacter) && currentCharacters.equals(primaryCharacters)) {
					currentCharacters = secondaryCharacters;
					initializeViews();
				} else
				if(character.equals(primaryCharacter) && currentCharacters.equals(secondaryCharacters)) {
					currentCharacters = primaryCharacters;
					initializeViews();
				} else {
//					JumpDialog.this.cancel();
					if (jumpListener != null) {
						jumpListener.onJump(character);
					}
				}
			}
		};
		for (int i = 0; i < currentCharactersSize; i++) {
			if (i % perRow == 0) {
				LayoutParams lp = new LayoutParams(LayoutParams.FILL_PARENT, 0);
				lp.weight = 1;
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
					0, LayoutParams.FILL_PARENT,1));
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
	
	private boolean isDaryCharacter(Character c) {
		if(currentCharacters.equals(primaryCharacters) && c.equals(secondaryCharacter)) {
			return true;
		}
		if(currentCharacters.equals(secondaryCharacters) && c.equals(primaryCharacter)) {
			return true;
		}
		return false;
	}



	private LinkedHashSet<Character> primaryCharacters;

	private LinkedHashSet<Character> enabledCharacters;

	private LinkedHashSet<Character> secondaryCharacters;

	private LinkedHashSet<Character> currentCharacters;

	private JumpListener jumpListener;
	
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

	public void setJumpListener(JumpListener jumpListener) {
		this.jumpListener = jumpListener;
	}
	
}
