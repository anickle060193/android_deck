package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import java.util.ArrayList;
import java.util.List;


public class GameFragment extends Fragment
{
    private View mMainView;
    private BluetoothFragment mBluetoothFragment;
    private final List<PlayingCardView> mPlayingCardViews = new ArrayList<>();

    private int mOrientation;

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

        mOrientation = getResources().getConfiguration().orientation;
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_game, container, false );

            mMainView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    final PlayingCardView playingCardView = new PlayingCardView( getActivity(), new Card() );
                    ( (ViewGroup)v ).addView( playingCardView );
                    mPlayingCardViews.add( playingCardView );
                }
            } );
        }
        else
        {
            final ViewGroup parent = (ViewGroup)mMainView.getParent();
            if( parent != null )
            {
                parent.removeView( mMainView );
            }
        }
        return mMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        if( mBluetoothFragment.isServer() )
        {
            //ajn mBluetoothFragment.createServer();
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );

        final int newOrientation = getResources().getConfiguration().orientation;
        if( newOrientation != mOrientation )
        {
            for( PlayingCardView view : mPlayingCardViews )
            {
                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
                final int temp = params.leftMargin;
                //noinspection SuspiciousNameCombination
                params.leftMargin = params.topMargin;
                params.topMargin = temp;
                view.setLayoutParams( params );
            }
            mOrientation = newOrientation;
        }
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
                MainActivity.backToMenu( getActivity() );
            }
        }
    };
}
