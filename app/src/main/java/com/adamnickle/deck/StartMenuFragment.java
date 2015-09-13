package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class StartMenuFragment extends Fragment
{
    public static StartMenuFragment newInstance()
    {
        return new StartMenuFragment();
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        return inflater.inflate( R.layout.fragment_start_menu, container, false );
    }

    @Override
    public void onViewCreated( View view, Bundle savedInstanceState )
    {
        super.onViewCreated( view, savedInstanceState );

        ButterKnife.bind( this, view );
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );
    }

    @OnClick( R.id.createGame )
    void onCreateGameClick()
    {
        final BluetoothFragment btFragment = BluetoothFragment.newInstance();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                .addToBackStack( null )
                .replace( R.id.main_content, GameFragment.newInstance( true, btFragment ) )
                .add( btFragment, BluetoothFragment.FRAGMENT_TAG )
                .commit();
    }

    @OnClick( R.id.joinGame )
    void onJoinGameClick()
    {
        final BluetoothFragment btFragment = BluetoothFragment.newInstance();
        getActivity().getSupportFragmentManager()
                .beginTransaction()
                .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                .addToBackStack( null )
                .replace( R.id.main_content, ServerListFragment.newInstance( btFragment ) )
                .add( btFragment, BluetoothFragment.FRAGMENT_TAG )
                .commit();
    }

    @OnClick( R.id.settings )
    void onSettingsClick()
    {
        Deck.toast( "You clicked Settings!" );
    }
}
