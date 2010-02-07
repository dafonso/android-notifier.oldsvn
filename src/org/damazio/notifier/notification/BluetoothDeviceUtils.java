package org.damazio.notifier.notification;

import java.util.List;
import java.util.Set;

import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothClass;
import android.bluetooth.BluetoothDevice;

/**
 * Utilities for dealing with bluetooth devices.
 * This can be used safely even in systems that don't support bluetooth,
 * in which case a dummy implementation will be used.
 *
 * @author rdamazio
 */
public abstract class BluetoothDeviceUtils {
  public static final String ANY_DEVICE = "any";
  private static BluetoothDeviceUtils instance;

  /**
   * Dummy implementation, for systems that don't support bluetooth.
   */
  private static class DummyImpl extends BluetoothDeviceUtils {
    @Override
    public void populateDeviceLists(List<String> deviceNames, List<String> deviceAddresses) {
      // Do nothing - no devices to add
    }

    @Override
    public BluetoothDevice findDeviceMatching(String targetDeviceAddress) {
      return null;
    }
  }

  /**
   * Real implementation, for systems that DO support bluetooth.
   */
  private static class RealImpl extends BluetoothDeviceUtils {
    @Override
    public void populateDeviceLists(List<String> deviceNames, List<String> deviceAddresses) {
      BluetoothAdapter bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
      Set<BluetoothDevice> pairedDevices = bluetoothAdapter.getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        if (device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER) {
          deviceAddresses.add(device.getAddress());
          deviceNames.add(device.getName());
        }
      }
    }

    @Override
    public BluetoothDevice findDeviceMatching(String targetDeviceAddress) {
      boolean findAnyDevice = (targetDeviceAddress.equals(ANY_DEVICE));
      Set<BluetoothDevice> pairedDevices = BluetoothAdapter.getDefaultAdapter().getBondedDevices();
      for (BluetoothDevice device : pairedDevices) {
        // Either look for the first paired computer device (if findAnyDevice is true),
        // or look for a device with a matching address
        if ((findAnyDevice &&
             device.getBluetoothClass().getMajorDeviceClass() == BluetoothClass.Device.Major.COMPUTER)
            || device.getAddress().equals(targetDeviceAddress)) {
          return device;
        }
      }
      return null;
    }
  }

  /**
   * Returns the proper (singleton) instance of this class.
   */
  public static BluetoothDeviceUtils getInstance() {
    if (instance == null) {
      if (!NotificationMethods.isBluetoothMethodSupported()) {
        instance = new DummyImpl();
      } else {
        instance = new RealImpl();
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
   * @param targetDeviceAddress the address of the device, or
   *        {@link #ANY_DEVICE} for using the first suitable device
   * @return the device's descriptor, or null if not found
   */
  public abstract BluetoothDevice findDeviceMatching(String targetDeviceAddress);
}
