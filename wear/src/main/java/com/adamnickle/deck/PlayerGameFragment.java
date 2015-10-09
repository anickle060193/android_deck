package com.adamnickle.deck;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.adamnickle.deckcommon.CardTableLayout;
import com.adamnickle.deckcommon.Game.Card;
import com.adamnickle.deckcommon.Game.Player;
import com.adamnickle.deckcommon.Utilities;


public class PlayerGameFragment extends Fragment
{
    private View mMainView;
    private CardTableLayout mCardTable;

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Nullable
    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_player_game, container, false );

            mCardTable = (CardTableLayout)mMainView.findViewById( R.id.card_table );
            mCardTable.setPlayer( new Player( "Test", "address" ) );
            mCardTable.setOnClickListener( new View.OnClickListener()
            {
                @Override
                public void onClick( View v )
                {
                    mCardTable.getPlayer().addCard( new Card() );
                }
            } );
        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }
}
