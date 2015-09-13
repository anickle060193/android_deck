package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.io.UnsupportedEncodingException;


public class GameFragment extends Fragment
{
    private BluetoothFragment mBluetoothFragment;

    public static GameFragment newInstance( BluetoothFragment bluetoothFragment )
    {
        final GameFragment gameFragment = new GameFragment();
        gameFragment.mBluetoothFragment = bluetoothFragment;
        return gameFragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        mBluetoothFragment.registerBluetoothListener( mListener );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_game, container, false );
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        view.setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                try
                {
                    mBluetoothFragment.write( ( Long.toString( System.currentTimeMillis() ).getBytes( "UTF-8" ) ) );
                }
                catch( UnsupportedEncodingException ex )
                {
                    Deck.log( "An error occurred encoding the message.", ex );
                }
            }
        } );
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if( mBluetoothFragment.isServer() )
        {
            mBluetoothFragment.createServer();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mBluetoothFragment.unregisterBluetoothListener( mListener );
        if( mBluetoothFragment.isServer() )
        {
            mBluetoothFragment.closeServer();
        }
        else
        {
            mBluetoothFragment.disconnect();
        }
    }

    private final BluetoothFragment.BluetoothListener mListener = new BluetoothFragment.BluetoothListener()
    {
        @Override
        public void onDeviceConnect( BluetoothDevice device )
        {
            if( mBluetoothFragment.isServer() )
            {
                Deck.toast( "Device connected: %s - %s", device.getName(), device.getAddress() );
            }
            else
            {
                Deck.toast( "Connected to server: %s - %s", device.getName(), device.getAddress() );
            }
        }

        @Override
        public void onDeviceDisconnect( BluetoothDevice device )
        {
            if( mBluetoothFragment.isServer() )
            {
                Deck.toast( "Device disconnected: %s - %s", device.getName(), device.getAddress() );
            }
            else
            {
                Deck.toast( "Disconnected from server: %s - %s", device.getName(), device.getAddress() );
                MainActivity.backToMenu( getActivity() );
            }
        }

        @Override
        public void onDataReceived( BluetoothDevice device, byte[] data )
        {
            try
            {
                final String message = new String( data, "UTF-8" );
                Deck.toast( message );
            }
            catch( UnsupportedEncodingException ex )
            {
                Deck.log( "An error occurred decoding the message data.", ex );
            }
        }
    };
}
