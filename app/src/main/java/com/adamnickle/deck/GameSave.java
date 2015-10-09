package com.adamnickle.deck;

import android.content.Context;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deckcommon.Game.Game;
import com.adamnickle.deckcommon.Utilities;

import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.IOException;
import java.io.OutputStreamWriter;


public class GameSave
{
    private static File getSaveDirectory( Context context )
    {
        return new File( context.getFilesDir(), "deck_saves" );
    }

    private static File getSaveFile( Context context, String filename )
    {
        return new File( GameSave.getSaveDirectory( context ), filename );
    }

    private static boolean makeSaveDirectory( Context context )
    {
        final File saveDirectory = getSaveDirectory( context );
        return saveDirectory.mkdirs() || saveDirectory.isDirectory();
    }

    public static String[] getGameSaveNames( Context context )
    {
        final File saveDirectory = GameSave.getSaveDirectory( context );
        final File[] saves = saveDirectory.listFiles();
        final String[] saveNames = new String[ saves.length ];
        for( int i = 0; i < saves.length; i++ )
        {
            saveNames[ i ] = saves[ i ].getName();
        }
        return saveNames;
    }

    public static boolean saveGame( Context context, Game game, String filename )
    {
        if( !GameSave.makeSaveDirectory( context ) )
        {
            return false;
        }
        boolean success = true;
        FileOutputStream output = null;
        OutputStreamWriter outputWriter = null;
        JsonWriter writer = null;
        try
        {
            final File saveFile = GameSave.getSaveFile( context, filename );
            output = new FileOutputStream( saveFile );
            outputWriter = new OutputStreamWriter( output );
            writer = new JsonWriter( outputWriter );
            game.writeToJson( writer );
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred saving a game.", ex );
            success = false;
        }
        finally
        {
            if( !Utilities.close( writer ) )
            {
                success = false;
            }
            if( !Utilities.close( outputWriter ) )
            {
                success = false;
            }
            if( !Utilities.close( output ) )
            {
                success = false;
            }
        }
        return success;
    }

    public static Game openGame( Context context, String filename )
    {
        FileReader inputReader = null;
        JsonReader reader = null;
        try
        {
            inputReader = new FileReader( GameSave.getSaveFile( context, filename ) );
            reader = new JsonReader( inputReader );
            return Game.readFromJson( reader );
        }
        catch( IOException ex )
        {
            Deck.log( "An error occurred while opening a game.", ex );
        }
        finally
        {
            Utilities.close( reader );
            Utilities.close( inputReader );
        }
        return null;
    }
}
