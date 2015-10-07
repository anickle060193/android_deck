package com.adamnickle.deck;

import android.view.View;
import android.view.ViewGroup;

import java.io.Closeable;
import java.io.IOException;


public abstract class Utilities
{
    private Utilities() { }

    public static boolean close( Closeable closeable )
    {
        if( closeable != null )
        {
            try
            {
                closeable.close();
            }
            catch( IOException ex )
            {
                Deck.log( "An error occurred closing " + closeable, ex );
                return false;
            }
        }
        return true;
    }

    public static void removeFromParent( View view )
    {
        final ViewGroup parent = (ViewGroup)view.getParent();
        if( parent != null )
        {
            parent.removeView( view );
        }
    }
}
