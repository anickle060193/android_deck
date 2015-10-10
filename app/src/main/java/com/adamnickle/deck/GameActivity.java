package com.adamnickle.deck;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ProgressBar;


public class GameActivity extends AppCompatActivity
{
    public static final String EXTRA_IS_SERVER = BuildConfig.APPLICATION_ID + ".extra.is_server";

    private static final String KEY_TABLE_OPEN = GameActivity.class.getName() + ".table_open";

    private ProgressBar mIndeterminateProgressBar;
    private View mAboveContent;

    private boolean mTableOpen = false;

    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_game );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        final Toolbar toolbar = (Toolbar)findViewById( R.id.toolbar );
        setSupportActionBar( toolbar );

        mIndeterminateProgressBar = (ProgressBar)findViewById( R.id.progress_bar );
        mAboveContent = findViewById( R.id.above_content );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final boolean isServer = intent.getBooleanExtra( EXTRA_IS_SERVER, true );
            if( isServer )
            {
                final BluetoothFragment btFragment = BluetoothFragment.newInstance( true );
                final Messenger messenger = new Messenger( btFragment );
                getSupportFragmentManager()
                        .beginTransaction()
                        .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                        .add( btFragment, BluetoothFragment.FRAGMENT_TAG )
                        .add( R.id.main_content, PlayerGameFragment.newInstance( messenger ) )
                        .add( R.id.above_content, TableGameFragment.newInstance( messenger ) )
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
        else
        {
            mTableOpen = savedInstanceState.getBoolean( KEY_TABLE_OPEN, false );
        }

        final View rootView = findViewById( R.id.rootView );
        rootView.getViewTreeObserver().addOnGlobalLayoutListener( new ViewTreeObserver.OnGlobalLayoutListener()
        {
            @Override
            public void onGlobalLayout()
            {
                rootView.getViewTreeObserver().removeOnGlobalLayoutListener( this );

                if( mTableOpen )
                {
                    openTable( false );
                }
                else
                {
                    closeTable( false );
                }
            }
        } );
    }

    @Override
    protected void onSaveInstanceState( Bundle outState )
    {
        super.onSaveInstanceState( outState );

        outState.putBoolean( KEY_TABLE_OPEN, mTableOpen );
    }

    private void translateAboveContent( float toTranslationY, boolean animate )
    {
        if( animate )
        {
            mAboveContent.animate()
                    .translationY( toTranslationY )
                    .setDuration( 300 )
                    .start();
        }
        else
        {
            mAboveContent.setTranslationY( toTranslationY );
        }
    }

    public void closeTable( boolean animate )
    {
        mTableOpen = false;
        translateAboveContent( -mAboveContent.getHeight(), animate );
    }

    public void closeTable()
    {
        closeTable( true );
    }

    public void openTable( boolean animate )
    {
        mTableOpen = true;
        translateAboveContent( 0.0f, animate );
    }

    public void openTable()
    {
        openTable( true );
    }

    public void toggleTable()
    {
        if( mTableOpen )
        {
            closeTable();
        }
        else
        {
            openTable();
        }
    }

    public boolean isTableOpen()
    {
        return mTableOpen;
    }

    public void setIndeterminateProgressVisibility( boolean visible )
    {
        mIndeterminateProgressBar.setVisibility( visible ? View.VISIBLE : View.GONE );
    }

    private boolean isGameRunning()
    {
        final Fragment fragment = getSupportFragmentManager().findFragmentById( R.id.main_content );
        return fragment instanceof PlayerGameFragment;
    }

    @Override
    public void onBackPressed()
    {
        if( isTableOpen() )
        {
            closeTable();
        }
        else
        {
            if( isGameRunning() )
            {
                Dialog.showConfirmation( this, "Leaving Game", "Are you sure you want to leave the game?", "Yes", "Cancel", new Dialog.OnConfirmationListener()
                {
                    @Override
                    public void onOK()
                    {
                        GameActivity.super.onBackPressed();
                    }

                    @Override
                    public void onCancel()
                    {
                        // Do nothing
                    }
                } );
            }
            else
            {
                super.onBackPressed();
            }
        }
    }
}
