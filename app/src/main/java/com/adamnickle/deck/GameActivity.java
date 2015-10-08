package com.adamnickle.deck;

import android.app.PendingIntent;
import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.FragmentTransaction;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.app.MediaRouteActionProvider;
import android.support.v7.media.MediaRouteSelector;
import android.support.v7.media.MediaRouter;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;
import android.widget.ProgressBar;

import com.google.android.gms.cast.CastDevice;
import com.google.android.gms.cast.CastMediaControlIntent;
import com.google.android.gms.cast.CastRemoteDisplayLocalService;
import com.google.android.gms.common.api.Status;

import butterknife.Bind;
import butterknife.ButterKnife;


public class GameActivity extends AppCompatActivity
{
    public static final String EXTRA_IS_SERVER = BuildConfig.APPLICATION_ID + ".extra.is_server";

    @Bind( R.id.progress_bar ) ProgressBar mIndeterminateProgressBar;
    @Bind( R.id.above_content ) View mAboveContent;

    private MediaRouter mMediaRouter;
    private MediaRouteSelector mMediaRouteSelector;
    private CastDevice mSelectedDevice;

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

        mMediaRouter = MediaRouter.getInstance( getApplicationContext() );

        mMediaRouteSelector = new MediaRouteSelector.Builder()
                .addControlCategory( CastMediaControlIntent.categoryForCast( BuildConfig.APPLICATION_ID ) )
                .build();

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

    private void toggleTable()
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

    @Override
    public boolean onCreateOptionsMenu( Menu menu )
    {
        getMenuInflater().inflate( R.menu.game, menu );

        final MenuItem mediaRouteMenuItem = menu.findItem( R.id.media_route_menu_item );
        final MediaRouteActionProvider mediaRouteActionProvider = (MediaRouteActionProvider)MenuItemCompat.getActionProvider( mediaRouteMenuItem );
        mediaRouteActionProvider.setRouteSelector( mMediaRouteSelector );

        return true;
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.toggleTable:
                toggleTable();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    protected void onStart()
    {
        super.onStart();

        mMediaRouter.addCallback( mMediaRouteSelector, mMediaRouterCallback, MediaRouter.CALLBACK_FLAG_REQUEST_DISCOVERY );
    }

    @Override
    protected void onStop()
    {
        super.onStop();

        mMediaRouter.removeCallback( mMediaRouterCallback );
    }

    private final MediaRouter.Callback mMediaRouterCallback = new MediaRouter.Callback()
    {
        @Override
        public void onRouteSelected( MediaRouter router, MediaRouter.RouteInfo route )
        {
            mSelectedDevice = CastDevice.getFromBundle( route.getExtras() );

            final Intent intent = new Intent( GameActivity.this, GameActivity.class );
            intent.setFlags( Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_SINGLE_TOP );
            final PendingIntent notificationPendingIntent = PendingIntent.getActivity( GameActivity.this, 0, intent, 0 );

            final CastRemoteDisplayLocalService.NotificationSettings settings = new CastRemoteDisplayLocalService.NotificationSettings.Builder()
                    .setNotificationPendingIntent( notificationPendingIntent )
                    .build();

            CastRemoteDisplayLocalService.startService(
                    getApplicationContext(),
                    GamePresentationService.class,
                    getString( R.string.cast_application_id ),
                    mSelectedDevice,
                    settings,
                    new CastRemoteDisplayLocalService.Callbacks()
                    {
                        @Override
                        public void onRemoteDisplaySessionStarted( CastRemoteDisplayLocalService castRemoteDisplayLocalService )
                        {
                            // Initialize sender UI
                        }

                        @Override
                        public void onRemoteDisplaySessionError( Status status )
                        {
                            Deck.toast( "An error occurred during initialization:" + status.getStatusMessage() );
                        }
                    } );
        }

        @Override
        public void onRouteUnselected( MediaRouter router, MediaRouter.RouteInfo route )
        {
            CastRemoteDisplayLocalService.stopService();
        }
    };
}
