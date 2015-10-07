package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;


public interface BluetoothSearchListener
{
    void onDeviceFound( BluetoothDevice device );
    void onDiscoveryStarted();
    void onDiscoveryEnded();
}
