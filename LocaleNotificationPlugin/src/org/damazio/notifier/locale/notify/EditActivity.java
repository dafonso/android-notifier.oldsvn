/*
 * Copyright 2010 Rodrigo Damazio <rodrigo@damazio.org>
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions
 * are met:
 * 
 * 1. Redistributions of source code must retain the above copyright
 *    notice, this list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright
 *    notice, this list of conditions and the following disclaimer in the
 *    documentation and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE AUTHOR ``AS IS'' AND ANY EXPRESS OR
 * IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED WARRANTIES
 * OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE DISCLAIMED.
 * IN NO EVENT SHALL THE AUTHOR BE LIABLE FOR ANY DIRECT, INDIRECT,
 * INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES (INCLUDING, BUT
 * NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES; LOSS OF USE,
 * DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND ON ANY
 * THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF
 * THIS SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */
package org.damazio.notifier.locale.notify;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;

/**
 * Activity displayed when the user wants to edit the Locale notification settings.
 *
 * @author rdamazio
 */
public class EditActivity extends Activity implements View.OnClickListener {
  private boolean isCancelled;
  private Button saveButton;
  private Button cancelButton;
  private EditText titleText;
  private EditText descriptionText;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    isCancelled = false;

    setContentView(R.layout.main);

    // Create the UI
    String breadcrumbString = getIntent().getStringExtra(com.twofortyfouram.Intent.EXTRA_STRING_BREADCRUMB);
    if (breadcrumbString != null) {
      setTitle(String.format("%s%s%s", breadcrumbString,
          com.twofortyfouram.Intent.BREADCRUMB_SEPARATOR, getString(R.string.app_name)));
    }
    titleText = (EditText) findViewById(R.id.title_text);
    descriptionText = (EditText) findViewById(R.id.description_text);
    saveButton = (Button) findViewById(R.id.save_button);
    cancelButton = (Button) findViewById(R.id.cancel_button);
    saveButton.setOnClickListener(this);
    cancelButton.setOnClickListener(this);

    // Parse the input bundle
    Bundle forwardedBundle = getIntent().getBundleExtra(com.twofortyfouram.Intent.EXTRA_BUNDLE);
    if (forwardedBundle != null) {
      titleText.setText(forwardedBundle.getString(Constants.EXTRA_TITLE));
      descriptionText.setText(forwardedBundle.getString(Constants.EXTRA_DESCRIPTION));
    }
  }

  @Override
  public void finish() {
    if (isCancelled) {
      setResult(RESULT_CANCELED);
      super.finish();
      return;
    }

    String title = titleText.getText().toString();
    String description = descriptionText.getText().toString();
    boolean hasTitle = (title.length() > 0);
    boolean hasDescription = (description.length() > 0);
    if (!hasTitle && !hasDescription) {
      setResult(com.twofortyfouram.Intent.RESULT_REMOVE);
      return;
    }

    Bundle resultBundle = new Bundle();
    if (hasTitle) {
      resultBundle.putString(Constants.EXTRA_TITLE, title);
    }
    if (hasDescription) {
      resultBundle.putString(Constants.EXTRA_DESCRIPTION, description);
    }

    String blurb = title + "/" + description;
    if (blurb.length() > com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH) {
      blurb = blurb.substring(0, com.twofortyfouram.Intent.MAXIMUM_BLURB_LENGTH - 3) + "...";
    }

    Intent returnIntent = new Intent();
    returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_BUNDLE, resultBundle);
    returnIntent.putExtra(com.twofortyfouram.Intent.EXTRA_STRING_BLURB, blurb);
    setResult(RESULT_OK, returnIntent);

    super.finish();
  }

  @Override
  public void onClick(View v) {
    isCancelled = (R.id.cancel_button == v.getId());

    finish();
  }
}
