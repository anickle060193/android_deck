package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
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

        findViewById( R.id.create_game ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                final Intent intent = new Intent( StartMenuActivity.this, GameActivity.class );
                intent.putExtra( GameActivity.EXTRA_IS_SERVER, true );
                startActivity( intent );
            }
        } );
        findViewById( R.id.join_game ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                startActivity( new Intent( StartMenuActivity.this, ServerListActivity.class ) );
            }
        } );
        findViewById( R.id.settings ).setOnClickListener( new View.OnClickListener()
        {
            @Override
            public void onClick( View v )
            {
                startActivity( new Intent( StartMenuActivity.this, SettingsActivity.class ) );
            }
        } );
    }
}
