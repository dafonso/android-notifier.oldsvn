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
package org.damazio.notifier.locale;

import org.damazio.notifier.R;

import android.content.Context;
import android.os.Bundle;

/**
 * Wrapper for reading and writing Locale settings from/to a {@link Bundle}.
 *
 * @author rdamazio
 */
public class LocaleSettings {
  public enum OnOffKeep {
    ON,
    OFF,
    KEEP;
  };

  private final Context context;
  private OnOffKeep enabledState = OnOffKeep.KEEP;

  public LocaleSettings(Context context, Bundle forwardedBundle) {
    this.context = context;

    parseBundle(forwardedBundle);
  }

  public OnOffKeep getEnabledState() {
    return enabledState;
  }

  public void setEnabledState(OnOffKeep enabledState) {
    this.enabledState = enabledState;
  }

  public boolean hasChanges() {
    if (enabledState != OnOffKeep.KEEP) return true;
    return false;
  }

  private void parseBundle(Bundle bundle) {
    if (bundle == null) {
      return;
    }

    enabledState = getOnOffKeep(bundle,
        context.getString(R.string.locale_change_enabled_key));
  }

  public Bundle toBundle() {
    Bundle result = new Bundle();
    putOnOffKeep(context.getString(R.string.locale_change_enabled_key), enabledState, result);
    return result;
  }

  @Override
  public String toString() {
    // Output the blurb
    StringBuilder blurbBuilder = new StringBuilder();
    appendOnOffKeepBlurb(R.string.locale_notifications_enabled_blurb, enabledState, blurbBuilder);
    return blurbBuilder.toString();
  }

  private OnOffKeep getOnOffKeep(Bundle fromBundle, String key) {
    return OnOffKeep.valueOf(fromBundle.getString(key));
  }

  private void putOnOffKeep(String key, OnOffKeep value, Bundle toBundle) {
    toBundle.putString(key, value.name());
  }

  private void appendOnOffKeepBlurb(int blurbRes, OnOffKeep value,
      StringBuilder blurbBuilder) {
    // TODO: Proper i18n here
    switch (value) {
      case ON:
        blurbBuilder.append(
            context.getString(blurbRes, 
                context.getString(R.string.locale_enabled_on)));
        break;
      case OFF:
        blurbBuilder.append(
            context.getString(blurbRes, 
                context.getString(R.string.locale_enabled_off)));
        break;
      default:
        // Don't output anything if there's no change in the setting
        break;
    }
  }
}
