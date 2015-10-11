package com.adamnickle.deck;

import android.view.View;
import android.view.ViewGroup;

import java.io.Closeable;
import java.io.IOException;
import java.util.Objects;


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

    public static float random( float min, float max )
    {
        return (float)Math.random() * ( max - min ) + min;
    }

    public static <T> int indexOf( T[] array, T obj )
    {
        if( obj == null )
        {
            for( int i = 0; i < array.length; i++ )
            {
                if( array[ i ] == null )
                {
                    return i;
                }
            }
        }
        else
        {
            for( int i = 0; i < array.length; i++ )
            {
                if( obj.equals( array[ i ] ) )
                {
                    return i;
                }
            }
        }
        return -1;
    }
}
