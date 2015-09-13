package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.util.Log;
import android.widget.Toast;


public class Deck extends Application
{
    private static Context sContext;
    private static Toast sToast;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate()
    {
        super.onCreate();

        sContext = this;
        sToast = Toast.makeText( sContext, "", Toast.LENGTH_SHORT );
    }

    public static void toast( String message )
    {
        sToast.setText( message );
        sToast.show();
    }

    public static void toast( String messageFormat, Object... args )
    {
        Deck.toast( String.format( messageFormat, args ) );
    }

    public static void log( String message )
    {
        if( BuildConfig.DEBUG )
        {
            Log.d( "Deck", message );
        }
    }

    public static void log( String messageFormat, Object... args )
    {
        Deck.log( String.format( messageFormat, args ) );
    }

    public static void log( String message, Exception ex )
    {
        ex.printStackTrace();
        if( BuildConfig.DEBUG )
        {
            Log.e( "Deck", message, ex );
        }
    }
}
