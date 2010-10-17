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
package org.damazio.notifier;

import org.damazio.notifier.util.Base64;
import org.damazio.notifier.util.Encryption;

import android.content.Context;
import android.content.res.TypedArray;
import android.preference.EditTextPreference;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;

/**
 * An {@link EditTextPreference} which expects password input and saves a hashed,
 * base64-encoded version of the password.
 *
 * @author Rodrigo Damazio
 */
public class EncodedPasswordPreference extends EditTextPreference {

  private static final String DEFAULT_HASH_ALGORITHM = "MD5";
  private String hashAlgorithm;
  private int numHashes;

  public EncodedPasswordPreference(Context context) {
    super(context);

    initialize(context, null);
  }

  public EncodedPasswordPreference(Context context, AttributeSet attrs, int defStyle) {
    super(context, attrs, defStyle);

    initialize(context, attrs);
  }

  public EncodedPasswordPreference(Context context, AttributeSet attrs) {
    super(context, attrs);

    initialize(context, attrs);
  }

  private void initialize(Context context, AttributeSet attrs) {
    TypedArray styledAttrs = context.obtainStyledAttributes(attrs,
        R.styleable.EncodedPasswordPreference);
    hashAlgorithm = styledAttrs.getString(
        R.styleable.EncodedPasswordPreference_hashAlgorithm);
    if (hashAlgorithm == null) {
      hashAlgorithm = DEFAULT_HASH_ALGORITHM;
    }
    numHashes = styledAttrs.getInt(R.styleable.EncodedPasswordPreference_numHashes, 1);

    getEditText().setInputType(
        InputType.TYPE_CLASS_TEXT |
        InputType.TYPE_TEXT_FLAG_NO_SUGGESTIONS |
        InputType.TYPE_TEXT_VARIATION_PASSWORD);
  }

  @Override
  protected void onDialogClosed(boolean positive) {
    if (positive) {
      // Encode the entered text
      EditText editText = getEditText();
      String encodedPassword = encodePassword(editText.getText().toString());
      editText.setText(encodedPassword);
    }

    // Now let it be saved
    super.onDialogClosed(positive);
  }

  @Override
  protected void onBindDialogView(View view) {
    super.onBindDialogView(view);

    // Makes no sense to allow the user to edit a hash, only accept new passwords.
    getEditText().setText("");
  }

  private String encodePassword(String password) {
    // Save the hashed password as base64
    byte[] keyBytes = Encryption.passPhraseToKey(password, hashAlgorithm, numHashes);
    return Base64.encodeToString(keyBytes, Base64.DEFAULT);
  }
}
