package com.adamnickle.deck;

import android.app.Activity;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothDevice;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.util.ArrayList;
import java.util.List;


public class BluetoothSearchFragment extends Fragment
{
    public interface BluetoothSearchListener
    {
        void onDeviceFound( BluetoothDevice device );
        void onDiscoveryStarted();
        void onDiscoveryEnded();
    }

    public static final String FRAGMENT_TAG = BluetoothSearchFragment.class.getName();

    private static final int REQUEST_ENABLE_BT = 1001;

    private final BluetoothAdapter mAdapter = BluetoothAdapter.getDefaultAdapter();
    private final List<BluetoothSearchListener> mListeners = new ArrayList<>();

    public static BluetoothSearchFragment newInstance()
    {
        return new BluetoothSearchFragment();
    }

    @Override
    public void onAttach( Context context )
    {
        super.onAttach( context );

        final IntentFilter filter = new IntentFilter();
        filter.addAction( BluetoothDevice.ACTION_FOUND );
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_STARTED );
        filter.addAction( BluetoothAdapter.ACTION_DISCOVERY_FINISHED );
        filter.addAction( BluetoothAdapter.ACTION_STATE_CHANGED );
        filter.addAction( BluetoothAdapter.ACTION_SCAN_MODE_CHANGED );
        getActivity().registerReceiver( mReceiver, filter );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        if( savedInstanceState == null )
        {
            if( mAdapter == null )
            {
                Deck.toast( "Bluetooth must be supported." );
                getActivity().finish();
                return;
            }

            enableBluetooth();
        }
    }

    @Override
    public void onDetach()
    {
        getActivity().unregisterReceiver( mReceiver );

        super.onDetach();
    }

    @Override
    public void onActivityResult( int requestCode, int resultCode, Intent data )
    {
        if( requestCode == REQUEST_ENABLE_BT )
        {
            if( resultCode != Activity.RESULT_OK )
            {
                Deck.toast( "Bluetooth must be enabled." );
                getActivity().finish();
            }
        }
    }

    private void enableBluetooth()
    {
        if( !mAdapter.isEnabled() )
        {
            startActivityForResult( new Intent( BluetoothAdapter.ACTION_REQUEST_ENABLE ), REQUEST_ENABLE_BT );
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
                for( BluetoothSearchListener listener : mListeners )
                {
                    listener.onDeviceFound( device );
                }
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_STARTED.equals( action ) )
            {
                for( BluetoothSearchListener listener : mListeners )
                {
                    listener.onDiscoveryStarted();
                }
            }
            else if( BluetoothAdapter.ACTION_DISCOVERY_FINISHED.equals( action ) )
            {
                for( BluetoothSearchListener listener : mListeners )
                {
                    listener.onDiscoveryEnded();
                }
            }
            else if( BluetoothAdapter.ACTION_STATE_CHANGED.equals( action ) )
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

    public void registerBluetoothSearchListener( BluetoothSearchListener listener )
    {
        if( mListeners.contains( listener ) )
        {
            throw new IllegalStateException( BluetoothSearchListener.class.getSimpleName() + " " + listener + " is already registered." );
        }
        mListeners.add( listener );
    }

    public void unregisterBluetoothSearchListener( BluetoothSearchListener listener )
    {
        if( !mListeners.remove( listener ) )
        {
            throw new IllegalStateException( BluetoothSearchListener.class.getSimpleName() + " " + listener + " was never registered." );
        }
    }

    public void startDiscovery()
    {
        mAdapter.startDiscovery();
    }
}
