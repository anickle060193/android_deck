package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.Player;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;


public class Messenger
{
    public static final String ALL_ADDRESSES = BuildConfig.APPLICATION_ID + ".all_addresses";

    private final BluetoothFragment mBluetoothFragment;
    private final String mAddress;

    private final Game mGame = new Game();

    private boolean mListening = true;

    public Messenger( BluetoothFragment fragment )
    {
        mBluetoothFragment = fragment;
        mAddress = mBluetoothFragment.getAdapter().getAddress();

        mGame.registerListener( new Game.GameListener()
        {
            @Override
            public void onPlayerAdded( Game game, Player player )
            {
                player.registerListener( mPlayerListener );
            }

            @Override
            public void onPlayerRemoved( Game game, Player player )
            {
                player.unregisterListener( mPlayerListener );
            }
        } );

        mGame.addPlayer( new Player( mBluetoothFragment.getAdapter().getName(), mAddress ) );
    }

    public boolean isMe( String address )
    {
        return mAddress.equals( address );
    }

    public Game getGame()
    {
        return mGame;
    }

    public void onDeviceConnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Player player = new Player( device.getName(), device.getAddress() );
            mGame.addPlayer( player );
            sendUpdatedGame( null );
        }
    }

    public void onDeviceDisconnect( BluetoothDevice device )
    {
        if( mBluetoothFragment.isServer() )
        {
            final Player player = new Player( device.getName(), device.getAddress() );
            mGame.removePlayer( player );
            sendUpdatedGame( null );
        }
    }

    public void onDataReceived( BluetoothDevice device, byte[] data )
    {
        final ByteArrayInputStream input = new ByteArrayInputStream( data );
        final InputStreamReader inputReader = new InputStreamReader( input );
        final JsonReader reader = new JsonReader( inputReader );
        try
        {
            final Game updatedGame = Game.readFromJson( reader );
            if( updatedGame != null )
            {
                mListening = false;
                mGame.update( updatedGame );
                mListening = true;

                if( mBluetoothFragment.isServer() )
                {
                    sendUpdatedGame( device.getAddress() );
                }
            }
        }
        catch( IOException ex )
        {
            Deck.debugToast( "An error occurred reading updated Game.", ex );
        }
        finally
        {
            try
            {
                reader.close();
            }
            catch( IOException ex )
            {
                Deck.debugToast( "An error occurred closing the JSON Reader", ex );
            }
        }
    }

    private void sendUpdatedGame( String exceptAddress )
    {
        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final OutputStreamWriter outputWriter = new OutputStreamWriter( output );
        final JsonWriter writer = new JsonWriter( outputWriter );
        try
        {
            mGame.writeToJson( writer );
            writer.flush();
            final byte[] data = output.toByteArray();

            if( exceptAddress == null )
            {
                mBluetoothFragment.sendToAll( data );
            }
            else
            {
                mBluetoothFragment.sendToAllExcept( data, exceptAddress );
            }
        }
        catch( IOException ex )
        {
            Deck.debugToast( "An error occurred while writing updated Game.", ex );
        }
        finally
        {
            try
            {
                writer.close();
            }
            catch( IOException ex )
            {
                Deck.debugToast( "An error occurred while closing JSON writer.", ex );
            }
        }
    }

    private final Player.PlayerListener mPlayerListener = new Player.PlayerListener()
    {
        @Override
        public void onCardAdded( Player player, Card card )
        {
            if( mListening )
            {
                sendUpdatedGame( null );
            }
        }

        @Override
        public void onCardRemoved( Player player, Card card )
        {
            if( mListening )
            {
                sendUpdatedGame( null );
            }
        }
    };
}
