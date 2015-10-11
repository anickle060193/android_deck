package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.app.AppCompatActivity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;


public class GameActivity extends AppCompatActivity
{
    public static final String EXTRA_IS_SERVER = BuildConfig.APPLICATION_ID + ".extra.is_server";
    public static final String EXTRA_BLUETOOTH_DEVICE = BuildConfig.APPLICATION_ID + ".extra.bluetooth_device";

    private static final String KEY_TABLE_OPEN = GameActivity.class.getName() + ".table_open";

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

        mAboveContent = findViewById( R.id.above_content );

        if( savedInstanceState == null )
        {
            final Intent intent = getIntent();
            final boolean isServer = intent.getBooleanExtra( EXTRA_IS_SERVER, true );

            final BluetoothFragment bluetoothFragment = BluetoothFragment.newInstance( isServer );

            final BluetoothDevice device = intent.getParcelableExtra( EXTRA_BLUETOOTH_DEVICE );
            if( device != null )
            {
                bluetoothFragment.connectToDevice( device );
            }

            final Messenger messenger = new Messenger( bluetoothFragment );
            getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                    .add( bluetoothFragment, BluetoothFragment.FRAGMENT_TAG )
                    .add( R.id.main_content, PlayerGameFragment.newInstance( messenger ) )
                    .add( R.id.above_content, TableGameFragment.newInstance( messenger ) )
                    .commit();
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
