package com.adamnickle.deck;

import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.WindowManager;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;


public class MainActivity extends AppCompatActivity
{
    @Bind( R.id.progress_bar ) ProgressBar mIndeterminateProgressBar;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_main );
        ButterKnife.bind( this );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        final Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        if( savedInstanceState == null )
        {
            getSupportFragmentManager()
                    .beginTransaction()
                    .add( R.id.main_content, StartMenuFragment.newInstance() )
                    .commit();
        }
    }

    public static void backToMenu( FragmentActivity activity )
    {
        activity.getSupportFragmentManager()
                .popBackStack( StartMenuFragment.FRAGMENT_STATE_START_MENU, FragmentManager.POP_BACK_STACK_INCLUSIVE );
    }

    public void setIndeterminateProgressVisibility( boolean visible )
    {
        mIndeterminateProgressBar.setVisibility( visible ? View.VISIBLE : View.GONE );
    }
}
