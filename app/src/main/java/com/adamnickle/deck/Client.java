package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;


public class Client extends BluetoothListener
{
    public Client( BluetoothFragment fragment )
    {
        super( fragment );
    }

    @Override
    public void onDeviceConnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            Deck.toast( "Device connected: %s", device.getName() );
        }
        else
        {
            Deck.toast( "Connected to server: %s", device.getName() );
        }
    }

    @Override
    public void onDeviceDisconnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            Deck.toast( "Device disconnected: %s", device.getName() );
        }
        else
        {
            Deck.toast( "Disconnected from server: %s", device.getName() );
            MainActivity.backToMenu( mBluetoothFragment.getActivity() );
        }
    }

    @Override
    public void onDataReceived( BluetoothDevice device, byte[] data )
    {

    }
}
