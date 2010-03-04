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
    private final BluetoothAdapter bluetoothAdapter;

    public RealImpl() {
      bluetoothAdapter = BluetoothAdapter.getDefaultAdapter();
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
    public BluetoothDevice findDeviceMatching(String targetDeviceAddress) {
      if (targetDeviceAddress.equals(ANY_DEVICE)) {
        return findAnyDevice();
      } else {
        return findDeviceByAddress(targetDeviceAddress);
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
