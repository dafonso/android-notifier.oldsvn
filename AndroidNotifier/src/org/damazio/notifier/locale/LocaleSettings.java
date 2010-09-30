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

import java.util.Arrays;

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
  private OnOffKeep ipEnabledState = OnOffKeep.KEEP;
  private OnOffKeep bluetoothEnabledState = OnOffKeep.KEEP;
  private String targetIp = OnOffKeep.KEEP.name();
  private String bluetoothTarget = OnOffKeep.KEEP.name();
  private String[] customIps = new String[0];

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

  public OnOffKeep getIpEnabledState() {
    return ipEnabledState;
  }

  public void setIpEnabledState(OnOffKeep ipEnabledState) {
    this.ipEnabledState = ipEnabledState;
  }

  public String getTargetIp() {
    return targetIp;
  }

  public void setTargetIp(String targetIp) {
    this.targetIp = targetIp;
  }

  public String[] getCustomIps() {
    return customIps;
  }

  public void setCustomIps(String[] customIps) {
    this.customIps = customIps;
  }

  public OnOffKeep getBluetoothEnabledState() {
    return bluetoothEnabledState;
  }

  public void setBluetoothEnabledState(OnOffKeep bluetoothEnabledState) {
    this.bluetoothEnabledState = bluetoothEnabledState;
  }

  public String getBluetoothTarget() {
    return bluetoothTarget;
  }

  public void setBluetoothTarget(String bluetoothTarget) {
    this.bluetoothTarget = bluetoothTarget;
  }

  public boolean hasChanges() {
    if (enabledState != OnOffKeep.KEEP) return true;
    if (ipEnabledState != OnOffKeep.KEEP) return true;
    if (bluetoothEnabledState != OnOffKeep.KEEP) return true;
    if (customIps.length > 0) return true;
    if (!targetIp.equals(OnOffKeep.KEEP.name())) return true;
    if (!bluetoothTarget.equals(OnOffKeep.KEEP.name())) return true;
    return false;
  }

  private void parseBundle(Bundle bundle) {
    if (bundle == null) {
      return;
    }

    enabledState = getOnOffKeep(bundle,
        context.getString(R.string.locale_change_enabled_key));
    ipEnabledState = getOnOffKeep(bundle,
        context.getString(R.string.locale_ip_enabled_key));
    bluetoothEnabledState = getOnOffKeep(bundle,
        context.getString(R.string.locale_bt_enabled_key));

    targetIp = bundle.getString(context.getString(R.string.locale_target_ip_key));
    if (targetIp == null) targetIp = OnOffKeep.KEEP.name();

    customIps = bundle.getStringArray(context.getString(R.string.locale_custom_ip_key));
    if (customIps == null) customIps = new String[0];

    bluetoothTarget = bundle.getString(context.getString(R.string.locale_bt_target_key));
    if (bluetoothTarget == null) bluetoothTarget = OnOffKeep.KEEP.name();
  }

  public Bundle toBundle() {
    Bundle result = new Bundle();
    putOnOffKeep(context.getString(R.string.locale_change_enabled_key), enabledState, result);
    putOnOffKeep(context.getString(R.string.locale_ip_enabled_key), ipEnabledState, result);
    putOnOffKeep(context.getString(R.string.locale_bt_enabled_key), bluetoothEnabledState, result);

    if (!OnOffKeep.KEEP.name().equals(targetIp)) {
      result.putString(context.getString(R.string.locale_target_ip_key), targetIp);
    }
    if (customIps.length > 0) {
      result.putStringArray(context.getString(R.string.locale_custom_ip_key), customIps);
    }
    if (!OnOffKeep.KEEP.name().equals(bluetoothTarget)) {
      result.putString(context.getString(R.string.locale_bt_target_key), bluetoothTarget);
    }
    return result;
  }

  @Override
  public String toString() {
    // Output the blurb
    StringBuilder blurbBuilder = new StringBuilder();
    boolean first = true;
    first = appendOnOffKeepBlurb(R.string.locale_notifications_enabled_blurb, enabledState, blurbBuilder, first);
    first = appendOnOffKeepBlurb(R.string.locale_ip_enabled_blurb, ipEnabledState, blurbBuilder, first);
    first = appendOnOffKeepBlurb(R.string.locale_bt_enabled_blurb, bluetoothEnabledState, blurbBuilder, first);

    if (!OnOffKeep.KEEP.name().equals(targetIp)) {
      first = appendBlurbDelimiter(blurbBuilder, first);
      blurbBuilder.append(context.getString(R.string.locale_target_ip_blurb, targetIp));
    }
    if (customIps.length > 0) {
      first = appendBlurbDelimiter(blurbBuilder, first);
      blurbBuilder.append(context.getString(R.string.locale_custom_ip_blurb, Arrays.toString(customIps)));
    }
    if (!OnOffKeep.KEEP.name().equals(bluetoothTarget)) {
      first = appendBlurbDelimiter(blurbBuilder, first);
      // TODO: Use bluetooth device name instead of MAC
      blurbBuilder.append(context.getString(R.string.locale_bt_target_blurb, bluetoothTarget));
    }
    return blurbBuilder.toString();
  }

  private OnOffKeep getOnOffKeep(Bundle fromBundle, String key) {
    String valueStr = fromBundle.getString(key);
    if (valueStr == null) return OnOffKeep.KEEP;
    return OnOffKeep.valueOf(valueStr);
  }

  private void putOnOffKeep(String key, OnOffKeep value, Bundle toBundle) {
    toBundle.putString(key, value.name());
  }

  private boolean appendOnOffKeepBlurb(int blurbRes, OnOffKeep value,
      StringBuilder blurbBuilder, boolean first) {
    switch (value) {
      case ON:
        first = appendBlurbDelimiter(blurbBuilder, first);
        blurbBuilder.append(
            context.getString(blurbRes, 
                context.getString(R.string.locale_enabled_on_value)));
        break;
      case OFF:
        first = appendBlurbDelimiter(blurbBuilder, first);
        blurbBuilder.append(
            context.getString(blurbRes, 
                context.getString(R.string.locale_enabled_off_value)));
        break;
      default:
        // Don't output anything if there's no change in the setting
        break;
    }

    return first;
  }

  private boolean appendBlurbDelimiter(StringBuilder blurbBuilder, boolean first) {
    if (!first) {
      blurbBuilder.append(", ");
    }
    return false;
  }
}
