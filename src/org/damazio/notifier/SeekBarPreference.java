package org.damazio.notifier;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.DialogPreference;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.SeekBar;
import android.widget.TextView;

/**
 * Preference which shows a slider bar for choosing a number.
 *
 * This file is beased on code by Matthew Wiggins found on
 * http://android.hlidskialf.com/blog/code/android-seekbar-preference
 * and rewritten to be more responsive and flexible.
 * 
 * @author rdamazio
 * @author Matthew Wiggins
 */
public class SeekBarPreference extends DialogPreference
    implements SeekBar.OnSeekBarChangeListener {
  private static final int DEFAULT_MAX_VALUE = 100;

  private final Context context;
  private final int maxValue;
  private final String dialogMessage;
  private final String suffix;

  private SeekBar seekBar;
  private TextView valueText;
  private int currentValue;

  public SeekBarPreference(Context context, AttributeSet attrs) { 
    super(context,attrs);

    this.context = context;

    TypedArray styledAttrs =
        context.obtainStyledAttributes(attrs, R.styleable.SeekBarPreference);
    maxValue = styledAttrs.getInt(
        R.styleable.SeekBarPreference_max, DEFAULT_MAX_VALUE);
    dialogMessage = styledAttrs.getString(
        R.styleable.SeekBarPreference_dialogMessage);
    suffix = styledAttrs.getString(
        R.styleable.SeekBarPreference_suffix);
  }

  @Override 
  protected View onCreateDialogView() {
    LayoutInflater layoutInflater =
        (LayoutInflater) context.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
    View layout = layoutInflater.inflate(R.layout.seek_bar_preference, null); 

    seekBar = (SeekBar) layout.findViewById(R.id.seek_bar);
    valueText = (TextView) layout.findViewById(R.id.seekbar_value_text);
    TextView dialogText = (TextView) layout.findViewById(R.id.dialog_message);

    if (dialogMessage != null) {
      dialogText.setText(dialogMessage);
    } else {
      dialogText.setVisibility(View.GONE);
    }

    seekBar.setOnSeekBarChangeListener(this);
    seekBar.setMax(maxValue);
    seekBar.setProgress(currentValue);  // Also triggers onProgressChanged

    return layout;
  }

  @Override
  protected void onDialogClosed(boolean positiveResult) {
    super.onDialogClosed(positiveResult);

    if (positiveResult) {
      persistValue();
    }
  }

  private void persistValue() {
    int value = getValue();
    if (callChangeListener((Integer) value)) {
      persistInt(value);
      notifyChanged();
    }
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    return a.getInt(index, 0);
  }

  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue) {
    int value;
    if (restore) {
      value = getPersistedInt(0);
    } else {
      value = (Integer) defaultValue;
    }
    setValue(value);
  }

  public void onProgressChanged(SeekBar seek, int value, boolean fromUser) {
    setCurrentValue(value);
  }

  public void onStartTrackingTouch(SeekBar seek) {
    // Do nothing
  }

  public void onStopTrackingTouch(SeekBar seek) {
    // Do nothing
  }

  public int getMaxValue() {
    return maxValue;
  }

  public void setValue(int value) { 
    if (seekBar != null) {
      seekBar.setProgress(value);
    }
    setCurrentValue(value);
    persistValue();
  }

  private void setCurrentValue(int value) {
    if (valueText != null) {
      String valueStr = String.valueOf(value);
      valueText.setText(
          suffix == null ? valueStr : valueStr + suffix);
    }

    currentValue = value;
  }

  public int getValue() {
    return currentValue;
  }
}