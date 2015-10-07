package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Player;


@SuppressLint("ValidFragment")
public class TableFragment extends PlayingCardHolderFragment
{
    private View mMainView;
    private CardTableLayout mCardTableLayout;

    public static TableFragment newInstance( BluetoothFragment bluetoothFragment )
    {
        return new TableFragment( bluetoothFragment.getMessenger() );
    }

    public TableFragment( Messenger messenger )
    {
        super( messenger );
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_table, container, false );

            mCardTableLayout = (CardTableLayout)mMainView.findViewById( R.id.cardTable );
            mCardTableLayout.setPlayer( getMessenger().getTable() );
            mCardTableLayout.setOnCardSendListener( mOnCardSendListener );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }

    private final CardTableLayout.OnCardSendListener mOnCardSendListener = new CardTableLayout.OnCardSendListener()
    {
        @Override
        public void onCardSend( final Card card )
        {
            showPlayerSelector( "Send card to:", new OnPlayerSelectedListener()
            {
                @Override
                public void onPlayerSelected( final Player player )
                {
                    getMessenger().performAction( new Messenger.Action()
                    {
                        @Override
                        public void run()
                        {
                            mCardTableLayout.getPlayer().removeCard( card );
                            player.addCard( card );
                        }
                    } );
                }
            } );
        }
    };
}
