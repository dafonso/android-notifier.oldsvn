package org.damazio.notifier.notification;

import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import org.damazio.notifier.NotifierConstants;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;
import android.util.Log;

/**
 * Utilities for dealing with bluetooth devices.
 * This can be used safely even in systems that don't support bluetooth,
 * in which case a dummy implementation will be used.
 *
 * @author rdamazio
 */
public abstract class BluetoothDeviceUtils {
  public static final String ANY_DEVICE = "any";
  public static final String ALL_DEVICES = "all";
  private static BluetoothDeviceUtils instance;

  /**
   * Dummy implementation, for systems that don't support bluetooth.
   */
  private static class DummyImpl extends BluetoothDeviceUtils {
    @Override
    public void populateDeviceLists(List<String> deviceNames, List<String> deviceAddresses) {
      // Do nothing - no devices to add
    }

    @SuppressWarnings("unchecked")
    @Override
    public Iterable<BluetoothDevice> findDevicesMatching(String targetDeviceAddress) {
      return Collections.EMPTY_LIST;
    }
  }

  /**
   * Real implementation, for systems that DO support bluetooth.
   */
  private static class RealImpl extends BluetoothDeviceUtils {
    private final BluetoothAdapter bluetoothAdapter;

    public RealImpl() {
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();

      if (bluetoothAdapter == null) {
        throw new IllegalStateException("Unable to get bluetooth adapter");
      }
    }

    @Override
    public void populateDeviceLists(List<String> deviceNames, List<String> deviceAddresses) {
      ensureNotDiscovering();

      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        BluetoothClass bluetoothClass = device.getBluetoothClass();
        if (bluetoothClass != null &&
            bluetoothClass.getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
          deviceAddresses.add(device.getAddress());
          deviceNames.add(device.getName());
        }
      }
    }

    @Override
    public Iterable<BluetoothDevice> findDevicesMatching(String targetDeviceAddress) {
      if (targetDeviceAddress.equals(ANY_DEVICE)) {
        return Collections.singletonList(findAnyDevice());
      } else if (targetDeviceAddress.equals(ALL_DEVICES)) {
        return getAllDevices();
      } else {
        return Collections.singletonList(findDeviceByAddress(targetDeviceAddress));
      }
    }

    /**
     * Finds and returns the first suitable device for sending notifications to.
     */
    private BluetoothDevice findAnyDevice() {
      ensureNotDiscovering();

      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        // Look for the first paired computer device
        if (isSuitableDevice(device)) {
          return device;
        }
      }

      return null;
    }

    /**
     * Finds and returns all devices suitablefor sending notifications to.
     */
    private Iterable<BluetoothDevice> getAllDevices() {
      ensureNotDiscovering();

      // Filter out unsuitable devices
      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      Set<BluetoothDevice> result = new HashSet<BluetoothDevice>(pairedDevices.size());
      for (BluetoothDevice device : pairedDevices) {
        if (isSuitableDevice(device)) {
          result.add(device);
        }
      }

      return result;
    }

    /**
     * Finds and returns a device with the given address, or null if it's not
     * suitable for notifications.
     */
    private BluetoothDevice findDeviceByAddress(String targetDeviceAddress) {
      ensureNotDiscovering();

      BluetoothDevice device = bluetoothAdapter.getRemoteDevice(targetDeviceAddress);
      if (isSuitableDevice(device)) {
        return device;
      }

      return null;
    }

    /**
     * Ensures the bluetooth adapter is not in discovery mode.
     */
    private void ensureNotDiscovering() {
      // If it's in discovery mode, cancel that for now.
      bluetoothAdapter.cancelDiscovery();
    }

    /**
     * Checks whether the given device is suitable for sending notifications to.
     * Being suitable means it's bonded and is a computer-type device.
     *
     * @param device the device to check
     * @return true if it's suitable, false otherwise
     */
    private boolean isSuitableDevice(BluetoothDevice device) {
      // Check that the device is bonded
      if (device.getBondState() != BluetoothDevice.BOND_BONDED) {
        return false;
      }

      // Check that the device is a computer
      BluetoothClass deviceClass = device.getBluetoothClass();
      if (deviceClass == null ||
          deviceClass.getMajorDeviceClass() != BluetoothClass.Device.Major.COMPUTER) {
        return false;
      }

      return true;
    }
  }

  /**
   * Returns the proper (singleton) instance of this class.
   */
  public static BluetoothDeviceUtils getInstance() {
    if (instance == null) {
      if (!isBluetoothMethodSupported()) {
        Log.d(NotifierConstants.LOG_TAG, "Using dummy bluetooth utils");
        instance = new DummyImpl();
      } else {
        Log.d(NotifierConstants.LOG_TAG, "Using real bluetooth utils");
        try {
          instance = new RealImpl();
        } catch (IllegalStateException ise) {
          Log.w(NotifierConstants.LOG_TAG, "Oops, I mean, using dummy bluetooth utils", ise);
          instance = new DummyImpl();
        }
      }
    }
    return instance;
  }

  /**
   * Populates the given lists with the names and addresses of all suitable
   * bluetooth devices.
   *
   * @param deviceNames the list to populate with user-visible names
   * @param deviceAddresses the list to populate with device addresses
   */
  public abstract void populateDeviceLists(List<String> deviceNames, List<String> deviceAddresses);

  /**
   * Finds the bluetooth device with the given address.
   *
   * @param targetDeviceAddress the address of the device,
   *        {@link #ANY_DEVICE} for using the first suitable device, or
   *        {@link #ALL_DEVICES} for returning all devices
   * @return a list of descriptors for matching devices, or null if none found
   */
  public abstract Iterable<BluetoothDevice> findDevicesMatching(String targetDeviceAddress);

  /**
   * @return whether the bluetooth method is supported on this device
   */
  public static boolean isBluetoothMethodSupported() {
    return NotifierConstants.ANDROID_SDK_INT >= 5;
  }
}
