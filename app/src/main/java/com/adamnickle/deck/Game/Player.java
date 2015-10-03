package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.Listenable;
import com.adamnickle.deck.MyCollections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Player extends Listenable<Player.PlayerListener>
{
    public interface PlayerListener
    {
        void onCardAdded( Card card );
        void onCardRemoved( Card card );
    }

    private final String mName;
    private final String mAddress;
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
        final List<Card> addedCards = new ArrayList<>();
        final List<Card> removedCards = new ArrayList<>();
        MyCollections.diff( mCards, updatedPlayer.mCards, addedCards, removedCards );

        for( Card card : removedCards )
        {
            removeCard( card );
        }
        for( Card card : addedCards )
        {
            addCard( card );
        }
    }

    public String getName()
    {
        return mName;
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
            listener.onCardAdded( card );
        }
    }

    public void removeCard( Card card )
    {
        mCards.remove( card );

        for( PlayerListener listener : getListeners() )
        {
            listener.onCardRemoved( card );
        }
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
