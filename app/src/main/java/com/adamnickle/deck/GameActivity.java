package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ProgressBar;

import butterknife.Bind;
import butterknife.ButterKnife;


public class GameActivity extends AppCompatActivity
{
    public static final String EXTRA_IS_SERVER = BuildConfig.APPLICATION_ID + ".extra.is_server";

    @Bind( R.id.progress_bar ) ProgressBar mIndeterminateProgressBar;
    @Bind( R.id.above_content ) View mAboveContent;

    private boolean mTableOpen = false;

    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );
        ButterKnife.bind( this );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        final Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        final View rootView = findViewById( R.id.rootView );
        rootView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                mAboveContent.setTranslationY( -mAboveContent.getHeight() );
            }
        } );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final boolean isServer = intent.getBooleanExtra( EXTRA_IS_SERVER, true );
            if( isServer )
            {
                final BluetoothFragment btFragment = BluetoothFragment.newInstance( true );
                getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                        .add( btFragment, BluetoothFragment.FRAGMENT_TAG )
                        .add( R.id.main_content, PlayerGameFragment.newInstance( btFragment ) )
                        .add( R.id.above_content, TableGameFragment.newInstance( btFragment ) )
                        .commit();
            }
            else
            {
                final BluetoothFragment btFragment = BluetoothFragment.newInstance( false );
                getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                        .add( btFragment, BluetoothFragment.FRAGMENT_TAG )
                        .add( R.id.main_content, ServerListFragment.newInstance( btFragment ) )
                        .commit();
            }
        }
    }

    public void toggleTable()
    {
        if( mTableOpen )
        {
            mAboveContent.animate()
                    .translationY( -mAboveContent.getHeight() )
                    .setDuration( 300 )
                    .start();
        }
        else
        {
            mAboveContent.animate()
                    .translationY( 0.0f )
                    .setDuration( 300 )
                    .start();
        }
        mTableOpen = !mTableOpen;
    }

    public void setIndeterminateProgressVisibility( boolean visible )
    {
        mIndeterminateProgressBar.setVisibility( visible ? View.VISIBLE : View.GONE );
    }
}
