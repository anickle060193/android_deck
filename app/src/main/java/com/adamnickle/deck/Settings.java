package com.adamnickle.deck;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.preference.PreferenceManager;
import android.support.annotation.DrawableRes;


public abstract class Settings
{
    public static final String PREF_NAME = "pref_name";
    public static final String PREF_PLAYER_GAME_BACKGROUND = "pref_player_game_background";
    public static final String PREF_TABLE_GAME_BACKGROUND = "pref_table_game_background";

    private Settings() { }

    public static String getName( Context context )
    {
        final String defaultName = BluetoothAdapter.getDefaultAdapter().getName();
        return PreferenceManager.getDefaultSharedPreferences( context )
                .getString( PREF_NAME, defaultName );
    }

    private static @DrawableRes int getBackgroundResource( Context context, String pref, @DrawableRes int defaultBackground )
    {
        final String value = PreferenceManager.getDefaultSharedPreferences( context )
                .getString( pref, null );
        if( value == null )
        {
            return defaultBackground;
        }
        else
        {
            final String[] backgroundNames = context.getResources().getStringArray( R.array.background_names );
            final int index = Utilities.indexOf( backgroundNames, value );
            if( index == -1 )
            {
                return defaultBackground;
            }
            else
            {
                final int[] backgrounds = Utilities.getResourceArray( context, R.array.background_drawables );
                return backgrounds[ index ];
            }
        }
    }

    public static @DrawableRes int getPlayerGameBackgroundResource( Context context )
    {
        return Settings.getBackgroundResource( context, Settings.PREF_PLAYER_GAME_BACKGROUND, R.color.player_game_background );
    }

    public static @DrawableRes int getTableGameBackgroundResource( Context context )
    {
        return Settings.getBackgroundResource( context, Settings.PREF_TABLE_GAME_BACKGROUND, R.color.table_game_background );
    }
}
