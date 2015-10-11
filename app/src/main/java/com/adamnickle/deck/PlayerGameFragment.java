package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.Player;


@SuppressLint("ValidFragment")
public class PlayerGameFragment extends AbstractGameFragment
{
    private View mMainView;
    private CardTableLayout mCardTable;

    public static PlayerGameFragment newInstance( Messenger messenger )
    {
        return new PlayerGameFragment( messenger );
    }

    public PlayerGameFragment( Messenger messenger )
    {
        super( messenger );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );

        getGame().registerListener( mGameListener );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_player_game, container, false );
            mMainView.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    final GameActivity gameActivity = (GameActivity)getActivity();
                    if( gameActivity.isTableOpen() )
                    {
                        gameActivity.closeTable();
                    }
                    else
                    {
                        mCardTable.getPlayer().addCard( new Card() );
                    }
                }
            } );

            mCardTable = (CardTableLayout)mMainView.findViewById( R.id.card_table );
            mCardTable.setOnCardSendListener( new CardTableLayout.OnCardSendListener()
            {
                @Override
                public void onCardInHolder( final Card card )
                {
                    showPlayerSelector( "Send card to:", false, new OnPlayerSelectedListener()
                    {
                        @Override
                        public void onPlayerSelected( final Player player )
                        {
                            getMessenger().performAction( new Messenger.Action()
                            {
                                @Override
                                public void run()
                                {
                                    mCardTable.getPlayer().removeCard( card );
                                    player.addCard( card );
                                }
                            } );
                        }
                    } );
                }

                @Override
                public void onCardOutside( Card card )
                {
                    // Do nothing
                }
            } );
            mCardTable.setPlayer( getMessenger().getMe() );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    @Override
    public void onCreateOptionsMenu( Menu menu, MenuInflater inflater )
    {
        inflater.inflate( R.menu.player_game, menu );
        if( getMessenger().isServer() )
        {
            inflater.inflate( R.menu.player_game_server, menu );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.set_name:
                showPlayerNameDialog();
                return true;

            case R.id.save_game:
                saveGame();
                return true;

            case R.id.open_game:
                openGame();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( R.string.app_name );
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        getGame().unregisterListener( mGameListener );
    }

    private final Game.GameListener mGameListener = new Game.GameListener()
    {
        @Override
        public void onPlayerAdded( Game game, Player player )
        {
            Deck.toast( player.getName() + " has connected." );
        }

        @Override
        public void onPlayerRemoved( Game game, Player player )
        {
            Deck.toast( player.getName() + " has disconnected." );
        }
    };

    private void showPlayerNameDialog()
    {
        Dialog.showTextDialog( getActivity(), "Set name:", true, "OK", new Dialog.OnTextDialogClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, String text )
            {
                mCardTable.getPlayer().setName( text );
            }
        } );
    }

    private void saveGame()
    {
        Dialog.showTextDialog( getActivity(), "Enter save name:", true, "Save", new Dialog.OnTextDialogClickListener()
        {
            @Override
            public void onClick( DialogInterface dialog, String saveName )
            {
                if( GameSave.saveGame( getActivity(), getGame(), saveName ) )
                {
                    Deck.toast( "Game save was successful!" );
                }
                else
                {
                    Deck.toast( "Game save was unsuccessful. Try again." );
                }
            }
        } );
    }

    private void openGame()
    {
        final String[] gameSaveNames = GameSave.getGameSaveNames( getActivity() );
        Dialog.showSingleChoiceDialog( getActivity(), "Select game save:", true, gameSaveNames, new Dialog.OnSingleChoiceDialogClickListener<String>()
        {
            @Override
            public void onClick( DialogInterface dialog, String saveName, int which )
            {
                final Game game = GameSave.openGame( getActivity(), saveName );
                if( game != null )
                {
                    if( getGame().hasSamePlayers( game ) )
                    {
                        getMessenger().openGame( game );
                    }
                    else
                    {
                        Deck.toast( "The same players must be playing as when the game was saved." );
                    }
                }
                else
                {
                    Deck.toast( "The game save could not be opened. Try again." );
                }
            }
        } );
    }
}
