package com.adamnickle.deck;


import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Set;

public abstract class Listenable<T>
{
    private final Set<T> mListeners = new HashSet<>();

    protected Collection<T> getListeners()
    {
        return Collections.unmodifiableCollection( mListeners );
    }

    public void registerListener( T listener )
    {
        mListeners.add( listener );
    }

    public void unregisterListener( T listener )
    {
        mListeners.remove( listener );
    }
}
