package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;


public class Player
{
    private final String mName;
    private final String mAddress;
    private final CardCollection mCards;

    private Player( String name, String address, CardCollection collection )
    {
        mName = name;
        mAddress = address;
        mCards = collection;
    }

    public Player( String name, String address )
    {
        this( name, address, new CardCollection() );
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

    public void removeCard( Card card )
    {
        mCards.remove( card );
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();

        writer.name( "name" ).value( mName );
        writer.name( "address" ).value( mAddress );

        writer.name( "cards" );
        mCards.writeToJson( writer );

        writer.endObject();
    }

    public static Player readFromJson( JsonReader reader ) throws IOException
    {
        String name = null;
        String address = null;
        CardCollection collection = null;

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
                collection = CardCollection.readFromJson( reader );
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        return new Player( name, address, collection );
    }
}
