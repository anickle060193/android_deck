package com.adamnickle.deck;

import android.os.Bundle;
import android.app.Fragment;
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

    @OnClick( R.id.createGame )
    void onCreateGameClick()
    {

    }

    @OnClick( R.id.joinGame )
    void onJoinGameClick()
    {

    }

    @OnClick( R.id.settings )
    void onSettingsClick()
    {

    }
}
