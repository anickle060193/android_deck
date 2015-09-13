package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class GameFragment extends Fragment
{
    private boolean mIsServer;
    private BluetoothFragment mBluetoothFragment;

    public static GameFragment newInstance( boolean isServer, BluetoothFragment bluetoothFragment )
    {
        final GameFragment gameFragment = new GameFragment();
        gameFragment.mIsServer = isServer;
        gameFragment.mBluetoothFragment = bluetoothFragment;
        return new GameFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_game, container, false );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );
    }
}
