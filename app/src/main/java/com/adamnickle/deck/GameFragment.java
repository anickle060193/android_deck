package com.adamnickle.deck;

import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.Player;

import java.util.ArrayList;
import java.util.List;


public class GameFragment extends Fragment
{
    private View mMainView;
    private CardTableLayout mCardTable;

    private Messenger mMessenger;
    private final List<PlayingCardView> mPlayingCardViews = new ArrayList<>();

    private int mOrientation;

    private final Game mGame = new Game();

    public static GameFragment newInstance( BluetoothFragment bluetoothFragment )
    {
        final GameFragment gameFragment = new GameFragment();
        gameFragment.mMessenger = bluetoothFragment.getMessenger();
        return gameFragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        mOrientation = getResources().getConfiguration().orientation;

        mMessenger.getGame().registerListener( mGameListener );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_game, container, false );
            mMainView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    final PlayingCardView playingCardView = new PlayingCardView( getActivity(), new Card() );
                    ( (ViewGroup)v ).addView( playingCardView );
                    mPlayingCardViews.add( playingCardView );
                }
            } );

            mCardTable = (CardTableLayout)mMainView.findViewById( R.id.cardTable );
            mCardTable.setOnCardSendListener( new CardTableLayout.CardSendListener()
            {
                @Override
                public void onCardSend( final Card card )
                {
                    showPlayerSelector( "Send card to:", new OnPlayerSelectedListener()
                    {
                        @Override
                        public void onPlayerSelected( Player player )
                        {
                            mCardTable.getPlayer().removeCard( card );
                            player.addCard( card );
                        }
                    } );
                }
            } );
        }
        else
        {
            final ViewGroup parent = (ViewGroup)mMainView.getParent();
            if( parent != null )
            {
                parent.removeView( mMainView );
            }
        }
        return mMainView;
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );

        final int newOrientation = getResources().getConfiguration().orientation;
        if( newOrientation != mOrientation )
        {
            for( PlayingCardView view : mPlayingCardViews )
            {
                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
                final int temp = params.leftMargin;
                //noinspection SuspiciousNameCombination
                params.leftMargin = params.topMargin;
                params.topMargin = temp;
                view.setLayoutParams( params );
            }
            mOrientation = newOrientation;
        }
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mMessenger.getGame().unregisterListener( mGameListener );
    }

    private final Game.GameListener mGameListener = new Game.GameListener()
    {
        @Override
        public void onPlayerAdded( Game game, Player player )
        {
            if( player.getAddress().equals( mMessenger.getThisAddress() ) )
            {
                mCardTable.setPlayer( player );
            }

            Deck.toast( player.getName() + " has connected." );
        }

        @Override
        public void onPlayerRemoved( Game game, Player player )
        {
            if( player.getAddress().equals( mMessenger.getThisAddress() ) )
            {
                mCardTable.setPlayer( null );
            }

            Deck.toast( player.getName() + " has disconnected." );
        }
    };

    private interface OnPlayerSelectedListener
    {
        void onPlayerSelected( Player player );
    }

    private void showPlayerSelector( String title, final OnPlayerSelectedListener listener )
    {
        final List<Player> players = mGame.getPlayers();
        final String[] playerNames = new String[ players.size() ];
        for( int i = 0; i < playerNames.length; i++ )
        {
            playerNames[ i ] = players.get( i ).getName();
        }

        new AlertDialog.Builder( getActivity() )
                .setTitle( title )
                .setSingleChoiceItems( playerNames, -1, new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.dismiss();
                        final Player player = players.get( which );
                        listener.onPlayerSelected( player );
                    }
                } )
                .setNegativeButton( "Cancel", new DialogInterface.OnClickListener()
                {
                    @Override
                    public void onClick( DialogInterface dialog, int which )
                    {
                        dialog.cancel();
                    }
                } )
                .show();
    }
}
