package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;


public class StartMenuActivity extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_start_menu );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }
    }

    public static class StartMenuFragment extends Fragment
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

            view.findViewById( R.id.createGame ).setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onCreateGameClick();
                }
            } );
            view.findViewById( R.id.joinGame ).setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onJoinGameClick();
                }
            } );
            view.findViewById( R.id.settings ).setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    onSettingsClick();
                }
            } );
        }

        @Override
        public void onResume()
        {
            super.onResume();

            getActivity().setTitle( R.string.app_name );
        }

        private void onCreateGameClick()
        {
            final Intent intent = new Intent( getActivity(), GameActivity.class );
            intent.putExtra( GameActivity.EXTRA_IS_SERVER, true );
            startActivity( intent );
        }

        private void onJoinGameClick()
        {
            startActivity( new Intent( getActivity(), ServerListActivity.class ) );
        }

        private void onSettingsClick()
        {
            Deck.toast( "You clicked Settings!" );
        }
    }
}
