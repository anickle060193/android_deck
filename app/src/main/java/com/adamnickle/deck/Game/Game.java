package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;


public class Game
{
    private final List<Player> mPlayers = new ArrayList<>();

    public void addPlayer( Player player )
    {
        mPlayers.add( player );
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();

        writer.name( "players" ).beginArray();
        for( Player player : mPlayers )
        {
            player.writeToJson( writer );
        }
        writer.endArray();

        writer.endObject();
    }

    public static Game readFromJson( JsonReader reader ) throws IOException
    {
        List<Player> players = new ArrayList<>();

        reader.beginObject();
        while( reader.hasNext() )
        {
            final String jsonName = reader.nextName();
            if( "players".equals( jsonName ) )
            {
                reader.beginArray();
                while( reader.hasNext() )
                {
                    players.add( Player.readFromJson( reader ) );
                }
                reader.endObject();
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        final Game game = new Game();
        game.mPlayers.addAll( players );
        return game;
    }
}
