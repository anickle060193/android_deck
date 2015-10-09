package com.adamnickle.deckcommon.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deckcommon.Diff;
import com.adamnickle.deckcommon.Listenable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Player extends Listenable<Player.PlayerListener>
{
    public interface PlayerListener
    {
        void onCardAdded( Player player, Card card );
        void onCardRemoved( Player player, Card card );
        void onNameChanged( Player player, String oldName );
    }

    private final String mAddress;

    private String mName;
    private final List<Card> mCards;

    private Player( String name, String address, List<Card> collection )
    {
        mName = name;
        mAddress = address;
        mCards = collection;
    }

    public Player( String name, String address )
    {
        this( name, address, new ArrayList<Card>() );
    }

    public void update( Player updatedPlayer )
    {
        final Diff<Card> diff = Diff.difference( mCards, updatedPlayer.mCards );

        for( Card card : diff.Removed )
        {
            removeCard( card );
        }
        for( Card card : diff.Added )
        {
            addCard( card );
        }

        if( !mName.equals( updatedPlayer.mName ) )
        {
            setName( updatedPlayer.mName );
        }
    }

    public String getName()
    {
        return mName;
    }

    public void setName( String name )
    {
        if( name != null )
        {
            final String oldName = mName;
            mName = name;

            for( PlayerListener listener : getListeners() )
            {
                listener.onNameChanged( this, oldName );
            }
        }
    }

    public String getAddress()
    {
        return mAddress;
    }

    public void addCard( Card card )
    {
        mCards.add( card );

        for( PlayerListener listener : getListeners() )
        {
            listener.onCardAdded( this, card );
        }
    }

    public void removeCard( Card card )
    {
        mCards.remove( card );

        for( PlayerListener listener : getListeners() )
        {
            listener.onCardRemoved( this, card );
        }
    }

    @Override
    public boolean equals( Object o )
    {
        if( this == o )
        {
            return true;
        }
        if( !( o instanceof Player ) )
        {
            return false;
        }

        final Player player = (Player)o;

        return this.mAddress.equals( player.getAddress() );
    }

    @Override
    public int hashCode()
    {
        return mAddress.hashCode();
    }

    @Override
    public String toString()
    {
        return mName;
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();

        writer.name( "name" ).value( mName );
        writer.name( "address" ).value( mAddress );

        writer.name( "cards" ).beginArray();
        for( Card card : mCards )
        {
            card.writeToJson( writer );
        }
        writer.endArray();

        writer.endObject();
    }

    public static Player readFromJson( JsonReader reader ) throws IOException
    {
        String name = null;
        String address = null;
        final List<Card> cards = new ArrayList<>();

        reader.beginObject();
        while( reader.hasNext() )
        {
            final String jsonName = reader.nextName();
            if( "name".equals( jsonName ) )
            {
                name = reader.nextString();
            }
            else if( "address".equals( jsonName ) )
            {
                address = reader.nextString();
            }
            else if( "cards".equals( jsonName ) )
            {
                reader.beginArray();
                while( reader.hasNext() )
                {
                    final Card card = Card.readFromJson( reader );
                    cards.add( card );
                }
                reader.endArray();
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        return new Player( name, address, cards );
    }
}
