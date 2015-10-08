package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deck.Game.Card;


@SuppressLint("ValidFragment")
public class TableGameFragment extends AbstractGameFragment
{
    private View mMainView;
    private CardTableLayout mCardTableLayout;

    public static TableGameFragment newInstance( BluetoothFragment bluetoothFragment )
    {
        return new TableGameFragment( bluetoothFragment.getMessenger() );
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
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_table_game, container, false );

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
    };
}
