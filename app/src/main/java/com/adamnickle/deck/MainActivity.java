package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.view.WindowManager;


public class MainActivity extends AppCompatActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );

        if( savedInstanceState == null )
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add( R.id.main_content, StartMenuFragment.newInstance() )
                    .commit();
        }
    }
}
