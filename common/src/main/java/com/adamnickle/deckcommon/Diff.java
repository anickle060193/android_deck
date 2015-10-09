package com.adamnickle.deckcommon;

import java.util.ArrayList;
import java.util.Collection;


public class Diff<T>
{
    public final Collection<T> Added = new ArrayList<>();
    public final Collection<T> Removed = new ArrayList<>();

    private Diff( Collection<T> older, Collection<T> newer )
    {
        Removed.addAll( older );
        Removed.removeAll( newer );

        Added.addAll( newer );
        Added.removeAll( older );
    }

    public static <T> Diff<T> difference( final Collection<T> older, final Collection<T> newer )
    {
        return new Diff<>( older, newer );
    }
}
