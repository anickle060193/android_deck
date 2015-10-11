package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;


@SuppressLint("ValidFragment")
public class TableGameFragment extends AbstractGameFragment
{
    private View mMainView;
    private CardTableLayout mCardTableLayout;

    public static TableGameFragment newInstance( Messenger messenger )
    {
        return new TableGameFragment( messenger );
    }

    public TableGameFragment( Messenger messenger )
    {
        super( messenger );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
        setHasOptionsMenu( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_table_game, container, false );

            mCardTableLayout = (CardTableLayout)mMainView.findViewById( R.id.card_table );
            mCardTableLayout.setBackgroundResource( Settings.getTableGameBackgroundResource( getActivity() ) );
            mCardTableLayout.setPlayer( getMessenger().getTable() );
            mCardTableLayout.setOnCardSendListener( mOnCardSendListener );
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
        inflater.inflate( R.menu.table_game, menu );
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case R.id.toggle_table:
                ( (GameActivity)getActivity() ).toggleTable();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    private void sendCardToPlayer( final Card card )
    {
        getMessenger().performAction( new Messenger.Action()
        {
            @Override
            public void run()
            {
                mCardTableLayout.getPlayer().removeCard( card );
                getMessenger().getMe().addCard( card );
            }
        } );
    }

    private final CardTableLayout.OnCardSendListener mOnCardSendListener = new CardTableLayout.OnCardSendListener()
    {
        @Override
        public void onCardInHolder( final Card card )
        {
            sendCardToPlayer( card );
        }

        @Override
        public void onCardOutside( final Card card )
        {
            sendCardToPlayer( card );
        }
    };
}
