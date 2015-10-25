package com.adamnickle.deck;

import android.content.Context;
import android.content.res.TypedArray;
import android.support.annotation.ArrayRes;
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

    public static int[] getResourceArray( Context context, @ArrayRes int id )
    {
        final TypedArray array = context.getResources().obtainTypedArray( id );

        final int length = array.length();
        final int[] resArray = new int[ length ];
        for( int i = 0; i < length; i++ )
        {
            resArray[ i ] = array.getResourceId( i, 0 );
        }

        array.recycle();
        return resArray;
    }
}
