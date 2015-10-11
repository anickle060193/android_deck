package com.adamnickle.deck;

import android.annotation.SuppressLint;
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
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;


@SuppressLint("ValidFragment")
public class BluetoothFragment extends Fragment
{
    public interface BluetoothListener
    {
        void onDeviceConnect( BluetoothDevice device );
        void onDeviceDisconnect( BluetoothDevice device );
        void onDataReceived( BluetoothDevice device, byte[] data );
    }

    public static final String FRAGMENT_TAG = BluetoothFragment.class.getName();

    private static final int REQUEST_ENABLE_BT = 1001;
    private static final int REQUEST_MAKE_DISCOVERABLE = 1002;

    private static final String SERVICE_NAME = BuildConfig.APPLICATION_ID + ".bluetooth_service";
    private static final UUID DECK_UUID = UUID.fromString( "a21ecfda-1ac7-4a8d-a5fa-bfc52ba0da07" );


    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();

    private final HashMap<String, ConnectedThread> mConnectedThreads = new HashMap<>();
    private final boolean mIsServer;
    private AcceptThread mAcceptThread;

    private final List<BluetoothListener> mListeners = new ArrayList<>();

    public static BluetoothFragment newInstance( boolean isServer )
    {
        return new BluetoothFragment( isServer );
    }

    public BluetoothFragment( boolean isServer )
    {
        mIsServer = isServer;
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );

        final IntentFilter filter = new IntentFilter();
        filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
        filter.addAction( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED );
        getActivity().registerReceiver( mReceiver, filter );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );

        if( savedInstanceState == null )
        {
            if( mAdapter == null )
            {
                Deck.toast( "Bluetooth must be supported." );
                getActivity().finish();
                return;
            }

            if( !enableBluetooth() )
            {
                if( this.isServer() )
                {
                    this.createServer();
                }
            }
        }
    }

    @Override
    public void onDetach()
    {
        getActivity().unregisterReceiver( mReceiver );

        super.onDetach();
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        if( this.isServer() )
        {
            this.closeServer();
        }
        else
        {
            this.disconnectFromAll();
        }
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == BluetoothFragment.REQUEST_ENABLE_BT )
        {
            if( resultCode == Activity.RESULT_OK )
            {
                if( this.isServer() )
                {
                    this.createServer();
                }
            }
            else
            {
                Deck.toast( "Bluetooth must be enabled." );
                getActivity().finish();
            }
        }
        else if( requestCode == BluetoothFragment.REQUEST_MAKE_DISCOVERABLE )
        {
            if( resultCode == Activity.RESULT_CANCELED )
            {
                Deck.toast( "Other players will not be able to connect to the server." );
            }
        }
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.connection_server, menu );
    }

    @Override
    public void onPrepareOptionsMenu( Menu menu )
    {
        final boolean accepting = mAcceptThread != null;
        menu.findItem( R.id.resumeAccepting ).setVisible( !accepting );
        menu.findItem( R.id.stopAccepting ).setVisible( accepting );

        final boolean isDiscoverable = mAdapter.getScanMode() == BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE;
        menu.findItem( R.id.makeDiscoverable ).setVisible( accepting && !isDiscoverable );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.makeDiscoverable:
                setDiscoverable( 300 );
                return true;

            case R.id.resumeAccepting:
                startAccepting();
                return true;

            case R.id.stopAccepting:
                stopAccepting();
                return true;

            default:
                return super.onOptionsItemSelected( item );
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

    private boolean enableBluetooth()
    {
        if( !mAdapter.isEnabled() )
        {
            startActivityForResult( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), BluetoothFragment.REQUEST_ENABLE_BT );
            return true;
        }
        return false;
    }

    private final BroadcastReceiver mReceiver = new BroadcastReceiver()
    {
        @Override
        public void onReceive( Context context, Intent intent )
        {
            final String action = intent.getAction();
            if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
            {
                final int state = intent.getIntExtra( BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF );
                if( state == BluetoothAdapter.STATE_OFF
                 || state == BluetoothAdapter.STATE_TURNING_OFF )
                {
                    Deck.toast( "Bluetooth has been disabled." );
                    getActivity().finish();
                }
            }
            else if( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED.equals( action ) )
            {
                final int scanMode = intent.getIntExtra( BluetoothAdapter.EXTRA_SCAN_MODE, BluetoothAdapter.SCAN_MODE_NONE );
                switch( scanMode )
                {
                    case BluetoothAdapter.SCAN_MODE_NONE:
                        //ajn Deck.toast( "Device is not connectable or discoverable." );
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE:
                        //ajn Deck.toast( "Device is connectable." );
                        break;
                    case BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE:
                        //ajn Deck.toast( "Device is discoverable." );
                        break;
                }
            }
            else
            {
                Deck.debugToast( "BluetoothFragment received unhandled action: " + action );
            }
        }
    };

    public void registerBluetoothListener( BluetoothListener listener )
    {
        if( mListeners.contains( listener ) )
        {
            throw new IllegalStateException( BluetoothListener.class.getSimpleName() + " " + listener + " is already registered." );
        }
        mListeners.add( listener );
    }

    public void unregisterBluetoothListener( BluetoothListener listener )
    {
        if( !mListeners.remove( listener ) )
        {
            throw new IllegalStateException( BluetoothListener.class.getSimpleName() + " " + listener + " was never registered." );
        }
    }

    private void startAccepting()
    {
        stopAccepting();
        mAcceptThread = new AcceptThread();
        mAcceptThread.start();
        setDiscoverable( 300 );
    }

    private void stopAccepting()
    {
        if( mAcceptThread != null )
        {
            mAcceptThread.cancel();
            mAcceptThread = null;
        }
    }

    private void setDiscoverable( int duration )
    {
        if( mAdapter.getScanMode() != BluetoothAdapter.SCAN_MODE_CONNECTABLE_DISCOVERABLE )
        {
            final Intent intent = new Intent( BluetoothAdapter.ACTION_REQUEST_DISCOVERABLE );
            intent.putExtra( BluetoothAdapter.EXTRA_DISCOVERABLE_DURATION, duration );
            startActivityForResult( intent, REQUEST_MAKE_DISCOVERABLE );
        }
    }

    private void createServer()
    {
        closeServer();
        startAccepting();
    }

    private void closeServer()
    {
        stopAccepting();
        disconnectFromAll();
    }

    private void disconnectFromAll()
    {
        for( ConnectedThread thread : mConnectedThreads.values() )
        {
            thread.cancel();
        }
        mConnectedThreads.clear();
    }

    public void connectToDevice( BluetoothDevice device )
    {
        new ConnectThread( device ).start();
    }

    public void sendToAll( byte[] data )
    {
        for( String addressKey : mConnectedThreads.keySet() )
        {
            this.sendTo( addressKey, data );
        }
    }

    public void sendToAllExcept( byte[] data, String address )
    {
        for( String addressKey : mConnectedThreads.keySet() )
        {
            if( !addressKey.equals( address ) )
            {
                this.sendTo( addressKey, data );
            }
        }
    }

    public void sendTo( String address, byte[] data )
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

    private void onDeviceConnect( ConnectedThread thread )
    {
        final BluetoothDevice device = thread.mSocket.getRemoteDevice();
        mConnectedThreads.put( device.getAddress(), thread );
        for( BluetoothListener listener : mListeners )
        {
            listener.onDeviceConnect( device );
        }
    }

    private void onDeviceDisconnect( ConnectedThread thread )
    {
        final BluetoothDevice device = thread.mSocket.getRemoteDevice();
        mConnectedThreads.remove( device.getAddress() );
        for( BluetoothListener listener : mListeners )
        {
            listener.onDeviceDisconnect( device );
        }
    }

    private class AcceptThread extends Thread
    {
        private BluetoothServerSocket mServerSocket;

        private boolean mCancelled = false;

        public AcceptThread()
        {
        }

        @Override
        public void run()
        {
            try
            {
                mServerSocket = mAdapter.listenUsingRfcommWithServiceRecord( SERVICE_NAME, DECK_UUID );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred starting the AcceptThread", ex );
                Deck.toast( "The server could not be created." );
                getActivity().finish();
                return;
            }

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
                    if( !mAdapter.isEnabled() )
                    {
                        break;
                    }
                }
            }

            Utilities.close( mServerSocket );
        }

        public void cancel()
        {
            Deck.log( "Cancelling Accepting thread." );
            mCancelled = true;
            Utilities.close( mServerSocket );
        }
    }

    private class ConnectThread extends Thread
    {
        private final BluetoothDevice mDevice;
        private BluetoothSocket mSocket;

        public ConnectThread( BluetoothDevice device )
        {
            mDevice = device;
        }

        @Override
        public void run()
        {
            mAdapter.cancelDiscovery();

            try
            {
                mSocket = mDevice.createRfcommSocketToServiceRecord( DECK_UUID );

                mSocket.connect();
                manageConnectedSocket( mSocket );
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while connecting.", ex );
                Utilities.close( mSocket );

                if( isServer() )
                {
                    Deck.toast( "Failed to connect to device." );
                }
                else
                {
                    Deck.toast( "Failed to connect to server." );
                }
                getActivity().finish();
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
            onDeviceConnect( this );

            final byte[] buffer = new byte[ 1024 ];

            while( true )
            {
                try
                {
                    final int bytes = mInputStream.read( buffer );
                    final byte[] data = Arrays.copyOf( buffer, bytes );

                    for( BluetoothListener listener : mListeners )
                    {
                        listener.onDataReceived( mDevice, data );
                    }
                }
                catch( IOException ex )
                {
                    Deck.log( "An error occurred while reading from a connection.", ex );

                    onDeviceDisconnect( this );
                    break;
                }
            }
        }

        public void write( byte[] bytes )
        {
            if( bytes == null )
            {
                return;
            }
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
                Deck.log( "Cancelling Connected thread." );
                mSocket.close();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred while closing a connection.", ex );
            }
        }
    }
}
