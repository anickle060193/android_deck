package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;

import com.adamnickle.deck.Game.Player;

import java.util.ArrayList;
import java.util.List;


public class Messenger
{
    public static final String ALL_ADDRESSES = BuildConfig.APPLICATION_ID + ".all_addresses";

    private final List<Client> mClients = new ArrayList<>();
    private final BluetoothFragment mBluetoothFragment;
    private final String mAddress;

    public Messenger( BluetoothFragment fragment )
    {
        mBluetoothFragment = fragment;
        mAddress = mBluetoothFragment.getAdapter().getAddress();
    }

    public void registerClient( Client client )
    {
        if( mClients.contains( client ) )
        {
            throw new IllegalStateException( "Client " + client + " is already registered." );
        }
        mClients.add( client );
    }

    public void unregisterClient( Client client )
    {
        if( !mClients.remove( client ) )
        {
            throw new IllegalStateException( "Client " + client + " was never registered." );
        }
    }

    public void onDeviceConnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Message message = Message.playerConnected( ALL_ADDRESSES, device.getAddress(), device.getName() );
            mBluetoothFragment.sendToAllExcept( message.toBytes(), device.getAddress() );

            onMessageReceived( message );
        }
    }

    public void onDeviceDisconnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Message message = Message.playerDisconnected( ALL_ADDRESSES, device.getAddress(), device.getName() );
            mBluetoothFragment.sendToAllExcept( message.toBytes(), device.getAddress() );

            onMessageReceived( message );
        }
    }

    public void onDataReceived( byte[] data )
    {
        final Message message = Message.fromBytes( data );
        if( message != null && message.isValid() )
        {
            final String destination = message.destination();
            if( mBluetoothFragment.isServer() )
            {
                if( ALL_ADDRESSES.equals( destination ) )
                {
                    mBluetoothFragment.sendToAllExcept( data, message.sender() );

                    this.onMessageReceived( message );
                }
                else
                {
                    mBluetoothFragment.sendTo( message.destination(), data );
                }
            }
            else
            {
                if( ALL_ADDRESSES.equals( destination ) || mAddress.equals( destination ) )
                {
                    this.onMessageReceived( message );
                }
            }
        }
    }

    public void onMessageReceived( Message message )
    {
        switch( message.messageType() )
        {
            case PlayerConnected:
            {
                final String playerName = message.deviceName();
                final String playerAddress = message.deviceAddress();
                for( Client client : mClients )
                {
                    final Player player = new Player( playerName, playerAddress );
                    client.onPlayerConnect( player );
                }
                break;
            }

            case PlayerDisconnected:
            {
                final String playerName = message.deviceName();
                final String playerAddress = message.deviceAddress();
                for( Client client : mClients )
                {
                    final Player player = new Player( playerName, playerAddress );
                    client.onPlayerDisconnect( player );
                }
                break;
            }
        }
    }
}
