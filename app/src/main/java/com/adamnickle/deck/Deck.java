package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.Toast;


public class Deck extends Application
{
    public static final String TAG = "DeckApplication";

    private static Context sContext;
    private static Handler sHandler;
    private static Toast sToast;

    @SuppressLint("ShowToast")
    @Override
    public void onCreate()
    {
        super.onCreate();

        sContext = this;
        sHandler = new Handler();
        sToast = Toast.makeText( sContext, "", Toast.LENGTH_SHORT );
    }

    public static void toast( final String message )
    {
        sHandler.post( new Runnable()
        {
            @Override
            public void run()
            {
                sToast.setText( message );
                sToast.show();
            }
        } );
    }

    public static void toast( String messageFormat, Object... args )
    {
        Deck.toast( String.format( messageFormat, args ) );
    }

    public static void debugToast( String message )
    {
        if( BuildConfig.DEBUG )
        {
            Deck.toast( message );
            Deck.log( message );
        }
    }

    public static void debugToast( String messageFormat, Object... args )
    {
        Deck.debugToast( String.format( messageFormat, args ) );
    }

    public static void debugToast( String message, Exception ex )
    {
        if( BuildConfig.DEBUG )
        {
            Deck.toast( "%s\n%s", message, ex.getMessage() );
            Deck.log( message, ex );
        }
    }

    public static void log( String message )
    {
        if( BuildConfig.DEBUG )
        {
            Log.d( TAG, message );
        }
    }

    public static void log( String messageFormat, Object... args )
    {
        Deck.log( String.format( messageFormat, args ) );
    }

    public static void log( String message, Exception ex )
    {
        if( BuildConfig.DEBUG )
        {
            Log.e( TAG, message, ex );
        }
    }
}
