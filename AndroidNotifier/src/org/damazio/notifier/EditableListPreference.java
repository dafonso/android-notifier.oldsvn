package org.damazio.notifier;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.res.TypedArray;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.Preference;
import android.text.InputType;
import android.util.AttributeSet;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

/**
 * Preference which displays a list of strings and lets the user add, edit or
 * remove elements. This list is displayed as a separate screen.
 * 
 * @author rdamazio
 */
public class EditableListPreference extends Preference implements AdapterView.OnItemClickListener {

  private static final String DEFAULT_LIST_DELIMITER = ",";

  private final String editDialogTitle;
  private final String editDialogMessage;
  private final String addButtonTitle;
  private final String removeButtonTitle;
  private final String listDelimiter;
  private final boolean allowDuplicates;

  // Data binding
  private ArrayAdapter<String> adapter;
  private List<String> contents;

  // State to be saved if needed
  private AlertDialog entryDialog;
  private int editingPosition;

  public EditableListPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
        R.styleable.EditableListPreference);
    editDialogTitle = withDefault("",
        styledAttrs.getString(R.styleable.EditableListPreference_editDialogTitle));
    editDialogMessage = withDefault("",
        styledAttrs.getString(R.styleable.EditableListPreference_editDialogMessage));
    addButtonTitle = withDefault("",
        styledAttrs.getString(R.styleable.EditableListPreference_addButtonTitle));
    removeButtonTitle = withDefault("",
        styledAttrs.getString(R.styleable.EditableListPreference_removeButtonTitle));
    listDelimiter = withDefault(DEFAULT_LIST_DELIMITER,
        styledAttrs.getString(R.styleable.EditableListPreference_listDelimiter));
    allowDuplicates = styledAttrs.getBoolean(R.styleable.EditableListPreference_allowDuplicates,
        true);

    contents = Collections.synchronizedList(new ArrayList<String>());
    adapter = new ArrayAdapter<String>(context, android.R.layout.simple_list_item_1, contents);
  }

  private static String withDefault(String def, String str) {
    return (str == null ? def : str);
  }

  @Override
  protected void onClick() {
    Context context = getContext();
    LayoutInflater inflater = (LayoutInflater) context
        .getSystemService(Context.LAYOUT_INFLATER_SERVICE);

    View rootView = inflater.inflate(R.layout.editable_list_preference, null);

    Button addButton = (Button) rootView.findViewById(R.id.add_list_item);
    addButton.setText(addButtonTitle);
    addButton.setOnClickListener(new OnClickListener() {
      @Override
      public void onClick(View v) {
        onAddButtonClick();
      }
    });

    ListView listView = (ListView) rootView.findViewById(R.id.editable_list);
    listView.setOnItemClickListener(this);
    listView.setAdapter(adapter);

    onAttachedToActivity();

    Dialog dialog = new Dialog(context, android.R.style.Theme);
    dialog.setTitle(getTitle());
    dialog.setContentView(rootView);

    dialog.show();
  }

  private void onAddButtonClick() {
    showEntryDialog(-1, null);
  }

  @Override
  public void onItemClick(AdapterView<?> parent, View view, int pos, long id) {
    showEntryDialog(pos, null);
  }

  private void showEntryDialog(final int editPosition, Bundle restoreState) {
    // Keep the state in case we need to save state
    this.editingPosition = editPosition;

    final boolean isNew = editPosition < 0;

    Context context = getContext();
    AlertDialog.Builder alert = new AlertDialog.Builder(context);
    alert.setTitle(editDialogTitle);
    alert.setMessage(editDialogMessage);

    if (restoreState == null) {
      // Create the view and attach it
      // (if we're restoring state then this will be restored as well)
      EditText entryText = new EditText(context);
      entryText.setId(R.id.entry_dialog_text);
      entryText.setInputType(InputType.TYPE_CLASS_TEXT | InputType.TYPE_TEXT_VARIATION_URI);

      if (!isNew) {
        entryText.setText(contents.get(editPosition));
      }

      alert.setView(entryText);
    }

    alert.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
      public void onClick(DialogInterface dialog, int whichButton) {
        EditText entryText = (EditText) entryDialog.findViewById(R.id.entry_dialog_text);
        String value = entryText.getText().toString();
        if (isNew) {
          addNewValue(value);
        } else {
          updateValue(editPosition, value);
        }
      }
    });

    alert.setNeutralButton(android.R.string.cancel, null);

    if (!isNew) {
      alert.setNegativeButton(removeButtonTitle, new DialogInterface.OnClickListener() {
        @Override
        public void onClick(DialogInterface dialog, int which) {
          removeValue(editPosition);
          persistList();
        }
      });
    }

    entryDialog = alert.create();

    if (restoreState != null) {
      // Restore the dialog with its view hierarchy
      entryDialog.onRestoreInstanceState(restoreState);
    }

    entryDialog.show();
  }

  private void removeValue(int position) {
    synchronized (adapter) {
      contents.remove(position);
      adapter.notifyDataSetChanged();
    }
  }

  private void updateValue(int position, String value) {
    synchronized (adapter) {
      if (!allowDuplicates && contents.contains(value)) {
        warnDuplicate(value);
        return;
      }

      String oldValue = contents.get(position);
      contents.remove(position);
      contents.add(position, value);

      if (!persistList()) {
        // Failed to validate new value, revert it
        contents.remove(position);
        contents.add(position, oldValue);
      }
      adapter.notifyDataSetChanged();
    }
  }

  private void addNewValue(String value) {
    synchronized (adapter) {
      if (!allowDuplicates && contents.contains(value)) {
        warnDuplicate(value);
        return;
      }

      contents.add(value);

      if (!persistList()) {
        // Failed to validate new value, remove it
        contents.remove(contents.size() - 1);
      }
      adapter.notifyDataSetChanged();
    }
  }

  private void warnDuplicate(String value) {
    String warning = getContext().getString(R.string.duplicate_entry, value);
    Toast.makeText(getContext(), warning, Toast.LENGTH_LONG).show();
  }

  @Override
  protected Object onGetDefaultValue(TypedArray a, int index) {
    int resourceId = a.getResourceId(index, -1);
    if (resourceId == -1) {
      // Assume it's a plain string
      return a.getString(index);
    }

    return a.getResources().getStringArray(resourceId);
  }

  @Override
  protected void onSetInitialValue(boolean restore, Object defaultValue) {
    if (restore) {
      // Load the previous values
      setValueString(getPersistedString(""));
    } else {
      // Accept either a list of strings or a comma-separate string as the
      // default
      if (defaultValue instanceof String) {
        setValueString((String) defaultValue);
      } else {
        setValues((String[]) defaultValue);
      }
    }
  }

  public void setValueString(String value) {
    if (value.length() > 0) {
      setValues(value.split(listDelimiter));
    } else {
      setValues(new String[0]);
    }
  }

  public void setValues(String[] values) {
    synchronized (adapter) {
      // Make a copy of the current values
      ArrayList<String> oldValues = new ArrayList<String>(contents);

      contents.clear();
      for (String value : values) {
        contents.add(value);
      }

      if (!persistList()) {
        // Uh-oh, something went wrong, restore the old values
        contents.clear();
        contents.addAll(oldValues);
      }
      adapter.notifyDataSetChanged();
    }
  }

  private boolean persistList() {
    if (!callChangeListener(contents)) {
      return false;
    }

    StringBuilder builder = new StringBuilder();
    synchronized (adapter) {
      int count = contents.size();
      for (int i = 0; i < count; i++) {
        if (i > 0)
          builder.append(listDelimiter);
        builder.append(contents.get(i));
      }
    }

    String newValue = builder.toString();
    persistString(newValue);
    notifyChanged();
    return true;
  }

  @Override
  protected Parcelable onSaveInstanceState() {
    final Parcelable superState = super.onSaveInstanceState();
    if (entryDialog == null || !entryDialog.isShowing()) {
      return superState;
    }

    final SavedState myState = new SavedState(superState);
    myState.entryDialogBundle = entryDialog.onSaveInstanceState();
    myState.editingPosition = this.editingPosition;
    return myState;
  }

  @Override
  protected void onRestoreInstanceState(Parcelable state) {
    Log.d(NotifierConstants.LOG_TAG, "Restoring state");
    if (state == null || !(state instanceof SavedState)) {
      super.onRestoreInstanceState(state);
      return;
    }

    SavedState savedState = (SavedState) state;
    super.onRestoreInstanceState(savedState.getSuperState());
    showEntryDialog(savedState.editingPosition, savedState.entryDialogBundle);
  }

  private static class SavedState extends BaseSavedState {
    Bundle entryDialogBundle;
    int editingPosition;

    public SavedState(Parcel source) {
      super(source);

      entryDialogBundle = source.readBundle();
      editingPosition = source.readInt();
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
      super.writeToParcel(dest, flags);
      dest.writeBundle(entryDialogBundle);
      dest.writeInt(editingPosition);
    }

    public SavedState(Parcelable superState) {
      super(superState);
    }

    @SuppressWarnings("unused")
    public static final Parcelable.Creator<SavedState> CREATOR =
        new Parcelable.Creator<SavedState>() {
          @Override
          public SavedState createFromParcel(Parcel in) {
            return new SavedState(in);
          }
    
          @Override
          public SavedState[] newArray(int size) {
            return new SavedState[size];
          }
        };
  }
}