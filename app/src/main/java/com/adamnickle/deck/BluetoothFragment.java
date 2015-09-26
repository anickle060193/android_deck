package com.adamnickle.deck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.bluetooth.BluetoothServerSocket;
import android.bluetooth.BluetoothSocket;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


public class BluetoothFragment extends Fragment
{
    public static final String FRAGMENT_TAG = BluetoothFragment.class.getName();

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_MAKE_DISCOVERABLE = 1002;

    private static final String SERVICE_NAME = BuildConfig.APPLICATION_ID + ".bluetooth_service";
    private static final UUID DECK_UUID = UUID.fromString( "a21ecfda-1ac7-4a8d-a5fa-bfc52ba0da07" );


    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private final HashMap<String, ConnectedThread> mConnectedThreads = new HashMap<>();
    private AcceptThread mAcceptThread;
    private boolean mIsServer;
    private Messenger mMessenger;

    private final List<BluetoothSearchListener> mSearchListeners = new ArrayList<>();

    public static BluetoothFragment newInstance( boolean isServer )
    {
        final BluetoothFragment fragment = new BluetoothFragment();
        fragment.mIsServer = isServer;
        return fragment;
    }

    public BluetoothFragment()
    {
        mMessenger = new Messenger( this );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( mAdapter == null )
        {
            Deck.toast( "Bluetooth must be supported." );
            getActivity().finish();
            return;
        }

        enableBluetooth();

        final IntentFilter filter = new IntentFilter();
        filter.addAction( BluetoothDevice.ACTION_FOUND );
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
        filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
        filter.addAction( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED );
        getActivity().registerReceiver( mReceiver, filter );

        if( this.isServer() )
        {
            if( !this.createServer() )
            {
                MainActivity.backToMenu( getActivity() );
                Deck.toast( "The server could not be opened." );
            }
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        MainActivity.setIndeterminateProgressVisibility( getActivity(), false );
        getActivity().unregisterReceiver( mReceiver );

        if( this.isServer() )
        {
            this.closeServer();
        }
        else
        {
            this.disconnect();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == BluetoothFragment.REQUEST_ENABLE_BT )
        {
            if( resultCode != Activity.RESULT_OK )
            {
                Deck.toast( "Bluetooth must be enabled." );
                MainActivity.backToMenu( getActivity() );
            }
        }
        else if( requestCode == BluetoothFragment.REQUEST_MAKE_DISCOVERABLE )
        {
            if( resultCode == Activity.RESULT_CANCELED )
            {
                Deck.toast( "Must be discoverable to create a server." );
                MainActivity.backToMenu( getActivity() );
            }
        }
    }

    public BluetoothAdapter getAdapter()
    {
        return mAdapter;
    }

    public boolean isServer()
    {
        return mIsServer;
    }

    public void enableBluetooth()
    {
        if( !mAdapter.isEnabled() )
        {
            startActivityForResult( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), BluetoothFragment.REQUEST_ENABLE_BT );
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
                for( BluetoothSearchListener listener : mSearchListeners )
                {
                    listener.onDeviceFound( device );
                }
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) )
            {
                MainActivity.setIndeterminateProgressVisibility( getActivity(), true );
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
            {
                MainActivity.setIndeterminateProgressVisibility( getActivity(), false );
            }
            else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
            {
                final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF );
                if( state == BluetoothAdapter.STATE_OFF
                 || state == BluetoothAdapter.STATE_TURNING_OFF )
                {
                    enableBluetooth();
                }
            }
            else if( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals( action ) )
            {
                final int scanMode = intent.getIntExtra( BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE );
                switch( scanMode )
                {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        Deck.toast( "Device is not connectable or discoverable." );
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        Deck.toast( "Device is connectable." );
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        Deck.toast( "Device is discoverable." );
                        break;
                }
            }
            else
            {
                Deck.debugToast( "BluetoothFragment received unhandled action: " + action );
            }
        }
    };

    public void registerBluetoothSearchListener( BluetoothSearchListener listener )
    {
        if( mSearchListeners.contains( listener ) )
        {
            throw new IllegalStateException( "BluetoothSearchListener " + listener + " is already registered." );
        }
        mSearchListeners.add( listener );
    }

    public void unregisterBluetoothSearchListener( BluetoothSearchListener listener )
    {
        if( !mSearchListeners.remove( listener ) )
        {
            throw new IllegalStateException( "BluetoothSearchListener " + listener + " was never registered." );
        }
    }

    public Messenger getMessenger()
    {
        return mMessenger;
    }

    public void findDevices()
    {
        mAdapter.startDiscovery();
    }

    public boolean createServer()
    {
        try
        {
            closeServer();
            mAcceptThread = new AcceptThread();
            mAcceptThread.start();

            final Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            //ajn intent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, 300 );
            startActivityForResult( intent, REQUEST_MAKE_DISCOVERABLE );
            return true;
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred while opening the server connection.", ex );
        }
        return false;
    }

    public void closeServer()
    {
        if( mAcceptThread != null )
        {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
        disconnect();
    }

    public void disconnect()
    {
        for( ConnectedThread thread : mConnectedThreads.values() )
        {
            thread.cancel();
        }
        mConnectedThreads.clear();
    }

    public void connectToDevice( BluetoothDevice device )
    {
        try
        {
            new ConnectThread( device ).start();
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred while starting connecting.", ex );
        }
    }

    public void writeToAll( byte[] data )
    {
        for( String addressKey : mConnectedThreads.keySet() )
        {
            this.write( addressKey, data );
        }
    }

    public void writeToAllExcept( byte[] data, String address )
    {
        for( String addressKey : mConnectedThreads.keySet() )
        {
            if( !addressKey.equals( address ) )
            {
                this.write( addressKey, data );
            }
        }
    }

    public void write( String address, byte[] data )
    {
        final ConnectedThread thread = mConnectedThreads.get( address );
        if( thread != null )
        {
            thread.write( data );
        }
    }

    private void manageConnectedSocket( BluetoothSocket socket )
    {
        try
        {
            new ConnectedThread( socket ).start();
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred while opening a connection.", ex );
        }
    }

    private void onDeviceConnected( ConnectedThread thread )
    {
        final BluetoothDevice device = thread.mSocket.getRemoteDevice();
        mConnectedThreads.put( device.getAddress(), thread );
        mMessenger.onDeviceConnect( device );
    }

    private void onDeviceDisconnected( ConnectedThread thread )
    {
        final BluetoothDevice device = thread.mSocket.getRemoteDevice();
        mConnectedThreads.remove( device.getAddress() );
        mMessenger.onDeviceDisconnect( device );
    }

    private class AcceptThread extends Thread
    {
        private final BluetoothServerSocket mServerSocket;

        private boolean mCancelled = false;

        public AcceptThread() throws IOException
        {
            mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord( SERVICE_NAME, DECK_UUID );
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
        private final BluetoothDevice mDevice;
        private final BluetoothSocket mSocket;

        public ConnectThread( BluetoothDevice device ) throws IOException
        {
            mDevice = device;
            mSocket = mDevice.createRfcommSocketToServiceRecord( DECK_UUID );
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

                if( isServer() )
                {
                    Deck.toast( "Failed to connect to device." );
                }
                else
                {
                    Deck.toast( "Failed to connect to server." );
                }
                MainActivity.backToMenu( getActivity() );
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
        private final BluetoothDevice mDevice;

        private final InputStream mInputStream;
        private final OutputStream mOutputStream;

        public ConnectedThread( BluetoothSocket socket ) throws IOException
        {
            mSocket = socket;
            mDevice = mSocket.getRemoteDevice();

            mInputStream = mSocket.getInputStream();
            mOutputStream = mSocket.getOutputStream();
        }

        @Override
        public void run()
        {
            onDeviceConnected( this );

            final byte[] buffer = new byte[ 1024 ];

            while( true )
            {
                try
                {
                    final int bytes = mInputStream.read( buffer );
                    final byte[] data = Arrays.copyOf( buffer, bytes );
                    mMessenger.onDataReceived( mDevice, data );
                }
                catch( IOException ex )
                {
                    Deck.log( "An error occurred while reading from a connection.", ex );

                    onDeviceDisconnected( this );
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
