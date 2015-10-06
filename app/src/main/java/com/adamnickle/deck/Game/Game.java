package com.adamnickle.deck.Game;

import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.Diff;
import com.adamnickle.deck.Listenable;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;


public class Game extends Listenable<Game.GameListener>
{
    public interface GameListener
    {
        void onPlayerAdded( Game game, Player player );
        void onPlayerRemoved( Game game, Player player );
    }

    private final HashMap<String, Player> mPlayers = new HashMap<>();

    public void addPlayer( Player player )
    {
        mPlayers.put( player.getAddress(), player );

        for( GameListener listener : getListeners() )
        {
            listener.onPlayerAdded( this, player );
        }
    }

    public void removePlayer( Player player )
    {
        mPlayers.remove( player.getAddress() );

        for( GameListener listener : getListeners() )
        {
            listener.onPlayerRemoved( this, player );
        }
    }

    public List<Player> getPlayers()
    {
        return new ArrayList<>( mPlayers.values() );
    }

    public void update( Game updatedGame )
    {
        final Diff<Player> diff = Diff.difference( mPlayers.values(), updatedGame.mPlayers.values() );

        for( Player removed : diff.Removed )
        {
            removePlayer( removed );
        }
        for( Player added : diff.Added )
        {
            addPlayer( added );
        }

        for( Player player : mPlayers.values() )
        {
            player.update( updatedGame.mPlayers.get( player.getAddress() ) );
        }
    }

    public boolean hasSamePlayers( Game game )
    {
        if( this.mPlayers.size() != game.mPlayers.size() )
        {
            return false;
        }

        for( Player player : this.mPlayers.values() )
        {
            if( !game.mPlayers.containsValue( player ) )
            {
                return false;
            }
        }
        return true;
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
                reader.endArray();
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
