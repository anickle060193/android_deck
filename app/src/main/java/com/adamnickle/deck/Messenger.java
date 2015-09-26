package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;

import com.adamnickle.deck.Game.Player;

import java.util.ArrayList;
import java.util.List;


public class Messenger extends BluetoothListener
{
    private final List<Client> mClients = new ArrayList<>();

    public Messenger( BluetoothFragment fragment )
    {
        super( fragment );
    }

    @Override
    public void onDeviceConnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Message message = Message.playerConnected( device.getAddress(), device.getName() );
            mBluetoothFragment.writeToAll( message.toBytes() );

            onMessageReceived( message );
        }
    }

    @Override
    public void onDeviceDisconnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Message message = Message.playerDisconnected( device.getAddress(), device.getName() );
            mBluetoothFragment.writeToAll( message.toBytes() );

            onMessageReceived( message );
        }
    }

    @Override
    public void onDataReceived( BluetoothDevice device, byte[] data )
    {
        final Message message = Message.fromBytes( data );
        if( message != null && message.isValid() )
        {
            this.onMessageReceived( message );
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
}
