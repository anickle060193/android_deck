package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import butterknife.ButterKnife;
import butterknife.OnClick;


public class StartMenuFragment extends Fragment
{
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
        final Intent intent = new Intent( getActivity(), GameActivity.class );
        intent.putExtra( GameActivity.EXTRA_IS_SERVER, true );
        startActivity( intent );
    }

    @OnClick( R.id.joinGame )
    void onJoinGameClick()
    {
        final Intent intent = new Intent( getActivity(), GameActivity.class );
        intent.putExtra( GameActivity.EXTRA_IS_SERVER, false );
        startActivity( intent );
    }

    @OnClick( R.id.settings )
    void onSettingsClick()
    {
        Deck.toast( "You clicked Settings!" );
    }
}
