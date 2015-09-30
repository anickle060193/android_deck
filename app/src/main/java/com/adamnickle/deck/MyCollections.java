package com.adamnickle.deck;

import java.util.Collection;
import java.util.List;


public class MyCollections
{
    public interface ComplexComparator<S, T>
    {
        boolean similar( S s, T t );
    }

    public static <T> void diff( final Collection<T> older, final Collection<T> newer, final Collection<T> added, final Collection<T> removed )
    {
        added.clear();
        removed.clear();

        removed.addAll( older );
        removed.removeAll( newer );

        added.addAll( newer );
        added.removeAll( older );
    }

    private static <S, T> void removeAll( List<S> collection, List<T> removers, ComplexComparator<S, T> comparator )
    {
        for( T remover : removers )
        {
            for( int i = collection.size() - 1; i >= 0; i-- )
            {
                if( comparator.similar( collection.get( i ), remover ) )
                {
                    collection.remove( i );
                }
            }
        }
    }

    public static <T> void diff( )
    {

    }
}
