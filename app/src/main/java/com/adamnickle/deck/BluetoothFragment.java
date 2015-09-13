package com.adamnickle.deck;

import android.app.Activity;
import android.app.Fragment;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;


public class BluetoothFragment extends Fragment
{
    public static final int REQUEST_ENABLE_BT = 1001;

    private static final String SERVICE_NAME = BuildConfig.APPLICATION_ID + ".bluetooth_service";
    private static final UUID DECK_UUID = UUID.fromString( "a21ecfda-1ac7-4a8d-a5fa-bfc52ba0da07" );

    private BluetoothAdapter mAdapter;

    private final List<ConnectedThread> mConnectedThreads = new ArrayList<>();
    private AcceptThread mAcceptThread;

    public static BluetoothFragment newInstance()
    {
        return new BluetoothFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        mAdapter = BluetoothAdapter.getDefaultAdapter();

        if( mAdapter == null )
        {
            Deck.toast( "Bluetooth must be supported." );
            getActivity().finish();
            return;
        }

        if( !mAdapter.isEnabled() )
        {
            startActivityForResult( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), BluetoothFragment.REQUEST_ENABLE_BT );
        }

        getActivity().registerReceiver( mReceiver, new IntentFilter( BluetoothDevice.ACTION_FOUND ) );
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        getActivity().unregisterReceiver( mReceiver );
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == BluetoothFragment.REQUEST_ENABLE_BT )
        {
            if( resultCode != Activity.RESULT_OK )
            {
                Deck.toast( "Bluetooth must be enabled." );
                getActivity().finish();
            }
        }
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = intent.getAction();
            if( BluetoothDevice.ACTION_FOUND.equals( action ) )
            {
                final BluetoothDevice device = intent.getParcelableExtra( BluetoothDevice.EXTRA_DEVICE );

                connectToDevice( device );
            }
        }
    };

    public void createServer()
    {
        closeServer();

        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
    }

    public void closeServer()
    {
        mAcceptThread.cancel();
        mAcceptThread = null;
        for( ConnectedThread thread : mConnectedThreads )
        {
            thread.cancel();
        }
        mConnectedThreads.clear();
    }

    public void connectToDevice( BluetoothDevice device )
    {
        final ConnectThread thread = new ConnectThread( device );
        thread.start();
    }

    private void manageConnectedSocket( BluetoothSocket socket )
    {
        final ConnectedThread thread = new ConnectedThread( socket );
        mConnectedThreads.add( thread );
        thread.start();
    }

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mServerSocket;

        private boolean mCancelled = false;

        public AcceptThread()
        {
            BluetoothServerSocket temp = null;
            try
            {
                temp = mAdapter.listenUsingRfcommWithServiceRecord( SERVICE_NAME, DECK_UUID );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while opening the server connection.", ex );
            }
            mServerSocket = temp;
        }

        @Override
        public void run()
        {
            mCancelled = false;
            while( !mCancelled )
            {
                try
                {
                    final BluetoothSocket socket = mServerSocket.accept();
                    manageConnectedSocket( socket );
                }
                catch( IOException ex )
                {
                    Deck.log( "An error occurred while accepting a connection.", ex );
                    break;
                }
            }
        }

        public void cancel()
        {
            mCancelled = true;
            try
            {
                mServerSocket.close();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while closing the server connection.", ex );
            }
        }
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final BluetoothDevice mDevice;

        public ConnectThread( BluetoothDevice device )
        {
            mDevice = device;
            BluetoothSocket temp = null;
            try
            {
                temp = mDevice.createRfcommSocketToServiceRecord( DECK_UUID );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while starting connecting.", ex );
            }
            mSocket = temp;
        }

        @Override
        public void run()
        {
            mAdapter.cancelDiscovery();

            try
            {
                mSocket.connect();
                manageConnectedSocket( mSocket );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while connecting.", ex );
                try
                {
                    mSocket.close();
                }
                catch( IOException ex2 )
                {
                    Deck.log( "An error occurred while closing a connecting socket.", ex2 );
                }
            }
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while aborting connecting.", ex );
            }
        }
    }

    private class ConnectedThread extends Thread
    {
        private final BluetoothSocket mSocket;
        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread( BluetoothSocket socket )
        {
            mSocket = socket;

            InputStream tempIn = null;
            OutputStream tempOut = null;

            try
            {
                tempIn = mSocket.getInputStream();
                tempOut = mSocket.getOutputStream();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while opening a connection.", ex );
            }

            mInputStream = tempIn;
            mOutputStream = tempOut;
        }

        @Override
        public void run()
        {
            final byte[] buffer = new byte[ 1024 ];

            while( true )
            {
                try
                {
                    final int bytes = mInputStream.read( buffer );

                    //TODO Send data somewhere
                }
                catch( IOException ex )
                {
                    Deck.log( "An error occurred while reading from a connection.", ex );
                    break;
                }
            }
        }

        public void write( byte[] bytes )
        {
            try
            {
                mOutputStream.write( bytes );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while writing to a connection.", ex );
            }
        }

        public void cancel()
        {
            try
            {
                mSocket.close();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while closing a connection.", ex );
            }
        }
    }
}
