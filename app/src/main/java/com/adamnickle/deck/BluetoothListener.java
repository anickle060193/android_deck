package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;


public abstract class BluetoothListener
{
    protected final BluetoothFragment mBluetoothFragment;
    protected final String mAddress;

    public BluetoothListener( BluetoothFragment fragment )
    {
        mBluetoothFragment = fragment;
        mAddress = mBluetoothFragment.getAdapter().getAddress();
    }

    public abstract void onDeviceConnect( BluetoothDevice device );

    public abstract void onDeviceDisconnect( BluetoothDevice device );

    public abstract void onDataReceived( BluetoothDevice device, byte[] data );
}
