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
    private static final String TABLE_NAME = "Table";
    private static final String TABLE_ADDRESS = BuildConfig.APPLICATION_ID + ".table_address";

    private final BluetoothFragment mBluetoothFragment;
    private final String mAddress;

    private final Game mGame = new Game();

    private boolean mFirstUpdate = true;

    private boolean mListening = true;
    private int mModCount = 0;
    private int mLastModCount = -1;
    private boolean mPaused = false;

    public Messenger( BluetoothFragment fragment )
    {
        mBluetoothFragment = fragment;
        mAddress = mBluetoothFragment.getAdapter().getAddress();

        mBluetoothFragment.registerBluetoothListener( mBluetoothListener );

        mGame.registerListener( new Game.GameListener()
        {
            @Override
            public void onPlayerAdded( Game game, Player player )
            {
                player.registerListener( mPlayerListener );
                mModCount++;
            }

            @Override
            public void onPlayerRemoved( Game game, Player player )
            {
                player.unregisterListener( mPlayerListener );
                mModCount++;
            }
        } );

        mGame.addPlayer( new Player( mBluetoothFragment.getAdapter().getName(), mAddress ) );
        mGame.addPlayer( new Player( TABLE_NAME, TABLE_ADDRESS ) );
    }

    public Player getMe()
    {
        for( Player player : mGame.getPlayers() )
        {
            if( mAddress.equals( player.getAddress() ) )
            {
                return player;
            }
        }
        return null;
    }

    public Player getTable()
    {
        for( Player player : mGame.getPlayers() )
        {
            if( TABLE_ADDRESS.equals( player.getAddress() ) )
            {
                return player;
            }
        }
        return null;
    }

    public boolean isServer()
    {
        return mBluetoothFragment.isServer();
    }

    public Game getGame()
    {
        return mGame;
    }

    private void pauseUpdates()
    {
        if( !mPaused )
        {
            mPaused = true;
            mLastModCount = mModCount;
        }
    }

    private void resumeUpdates()
    {
        if( mPaused )
        {
            mPaused = false;
            if( mLastModCount != mModCount )
            {
                sendUpdatedGame();
            }
            mLastModCount = -1;
        }
    }

    public interface Action
    {
        void run();
    }

    public void performAction( Action action )
    {
        pauseUpdates();
        action.run();
        resumeUpdates();
    }

    public void openGame( Game game )
    {
        onGameReceived( game );
    }

    private final BluetoothFragment.BluetoothListener mBluetoothListener = new BluetoothFragment.BluetoothListener()
    {
        @Override
        public void onDeviceConnect( BluetoothDevice device )
        {
            if( Messenger.this.isServer() )
            {
                final Player player = new Player( device.getName(), device.getAddress() );
                mGame.addPlayer( player );
                sendUpdatedGame();
            }
        }

        @Override
        public void onDeviceDisconnect( BluetoothDevice device )
        {
            if( Messenger.this.isServer() )
            {
                final Player player = new Player( device.getName(), device.getAddress() );
                mGame.removePlayer( player );
                sendUpdatedGame();
            }
            else
            {
                mBluetoothFragment.getActivity().finish();
                Deck.toast( "Disconnected from server." );
            }
        }

        @Override
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
                    onGameReceived( updatedGame );

                    if( mFirstUpdate )
                    {
                        mFirstUpdate = false;
                        sendUpdatedGame();
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
    };

    private void onGameReceived( Game game )
    {
        mListening = false;
        mGame.update( game );
        mListening = true;

        if( this.isServer() )
        {
            sendUpdatedGame();
        }
    }

    private void sendUpdatedGame()
    {
        if( mPaused )
        {
            return;
        }

        final ByteArrayOutputStream output = new ByteArrayOutputStream();
        final OutputStreamWriter outputWriter = new OutputStreamWriter( output );
        final JsonWriter writer = new JsonWriter( outputWriter );
        try
        {
            mGame.writeToJson( writer );
            writer.flush();
            final byte[] data = output.toByteArray();

            mBluetoothFragment.sendToAll( data );
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

    private void onGameChanged()
    {
        if( mListening )
        {
            sendUpdatedGame();
            mModCount++;
        }
    }

    private final Player.PlayerListener mPlayerListener = new Player.PlayerListener()
    {
        @Override
        public void onCardAdded( Player player, Card card )
        {
            onGameChanged();
        }

        @Override
        public void onCardRemoved( Player player, Card card )
        {
            onGameChanged();
        }

        @Override
        public void onNameChanged( Player player, String oldName )
        {
            onGameChanged();
        }
    };
}
