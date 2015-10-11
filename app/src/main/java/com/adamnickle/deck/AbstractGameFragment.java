package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.content.DialogInterface;
import android.support.v4.app.Fragment;

import com.adamnickle.deck.Game.Game;
import com.adamnickle.deck.Game.Player;

import java.util.List;


@SuppressLint("ValidFragment")
public abstract class AbstractGameFragment extends Fragment
{
    protected interface OnPlayerSelectedListener
    {
        void onPlayerSelected( Player player );
    }

    private final Messenger mMessenger;

    public AbstractGameFragment( Messenger messenger )
    {
        mMessenger = messenger;
    }

    protected Messenger getMessenger()
    {
        return mMessenger;
    }

    protected Game getGame()
    {
        return mMessenger.getGame();
    }

    protected void showPlayerSelector( String title, final OnPlayerSelectedListener listener )
    {
        final List<Player> players = getGame().getPlayers();
        Dialog.showSingleChoiceDialog( getActivity(), title, true, players.toArray( new Player[ players.size() ] ), new Dialog.OnSingleChoiceDialogClickListener<Player>()
        {
            @Override
            public void onClick( DialogInterface dialog, Player player, int which )
            {
                listener.onPlayerSelected( player );
            }
        } );
    }
}
