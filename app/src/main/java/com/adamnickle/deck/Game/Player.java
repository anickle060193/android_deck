package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Player
{
    private final String mName;
    private final String mAddress;
    private final List<Card> mCards = new ArrayList<>();

    public Player( String name, String address )
    {
        this.mName = name;
        this.mAddress = address;
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
        List<Card> cards = new ArrayList<>();

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
                    cards.add( Card.readFromJson( reader ) );
                }
                reader.endArray();
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        final Player player = new Player( name, address );
        player.mCards.addAll( cards );
        return player;
    }
}
