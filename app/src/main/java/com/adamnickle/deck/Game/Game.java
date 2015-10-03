package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.Deck;
import com.adamnickle.deck.Listenable;
import com.adamnickle.deck.MyCollections;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;


public class Game extends Listenable<Game.GameListener>
{
    public interface GameListener
    {
        void onPlayerAdded( Player player );
        void onPlayerRemoved( Player player );
    }

    private final HashMap<String, Player> mPlayers = new HashMap<>();

    public void addPlayer( Player player )
    {
        mPlayers.put( player.getAddress(), player );
    }

    public void removePlayer( Player player )
    {
        mPlayers.remove( player.getAddress() );
    }

    public int getPlayerCount()
    {
        return mPlayers.size();
    }

    public List<Player> getPlayers()
    {
        return new ArrayList<>( mPlayers.values() );
    }

    public void update( Game updatedGame )
    {
        final HashSet<Player> addedPlayers = new HashSet<>();
        final HashSet<Player> removedPlayers = new HashSet<>();
        MyCollections.diff( mPlayers.values(), updatedGame.mPlayers.values(), addedPlayers, removedPlayers );

        for( Player removed : removedPlayers )
        {
            removePlayer( removed );
        }
        for( Player added : addedPlayers )
        {
            addPlayer( added );
        }

        for( Player player : mPlayers.values() )
        {
            player.update( updatedGame.mPlayers.get( player.getAddress() ) );
        }
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();

        writer.name( "players" ).beginArray();
        for( Player player : mPlayers.values() )
        {
            player.writeToJson( writer );
        }
        writer.endArray();

        writer.endObject();
    }

    public static Game readFromJson( JsonReader reader ) throws IOException
    {
        final Game game = new Game();

        reader.beginObject();
        while( reader.hasNext() )
        {
            final String jsonName = reader.nextName();
            if( "players".equals( jsonName ) )
            {
                reader.beginArray();
                while( reader.hasNext() )
                {
                    final Player player = Player.readFromJson( reader );
                    game.addPlayer( player );
                }
                reader.endObject();
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        return game;
    }
}
