package com.adamnickle.deck.Game;

import android.support.annotation.NonNull;
import android.util.JsonReader;
import android.util.JsonWriter;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Random;


public class CardCollection extends ArrayList<Card>
{
    public interface CardCollectionListener
    {
        void onCardAdded( Card card );
        void onCardRemoved( Card card );
        void onCardCollectionChanged();
    }

    protected final Random rand = new Random();
    protected final List<CardCollectionListener> mListeners = new ArrayList<>();

    public void addListener( CardCollectionListener listener )
    {
        mListeners.add( listener );
    }

    public void removeListener( CardCollectionListener listener )
    {
        mListeners.remove( listener );
    }

    public void shuffle()
    {
        Collections.shuffle( this );
        onCardCollectionChanged();
    }

    protected void onCardAdded( Card card )
    {
        for( CardCollectionListener listener : mListeners )
        {
            listener.onCardAdded( card );
        }
    }

    protected void onCardRemoved( Card card )
    {
        for( CardCollectionListener listener : mListeners )
        {
            listener.onCardRemoved( card );
        }
    }

    protected void onCardCollectionChanged()
    {
        for( CardCollectionListener listener : mListeners )
        {
            listener.onCardCollectionChanged();
        }
    }

    @Override
    public boolean add( Card card )
    {
        this.add( size(), card );
        return true;
    }

    @Override
    public void add( int index, Card card )
    {
        super.add( index, card );
        onCardAdded( card );
    }

    @Override
    public boolean addAll( Collection<? extends Card> cards )
    {
        return this.addAll( size(), cards );
    }

    @Override
    public boolean addAll( int index, Collection<? extends Card> cards )
    {
        final boolean ret = super.addAll( index, cards );
        if( ret )
        {
            for( Card card : cards )
            {
                onCardAdded( card );
            }
        }
        return ret;
    }

    @Override
    public void clear()
    {
        super.clear();
        onCardCollectionChanged();
    }

    @Override
    public Card remove( int index )
    {
        final Card card = super.remove( index );
        onCardRemoved( card );
        return card;
    }

    @Override
    public boolean remove( Object object )
    {
        final boolean ret = super.remove( object );
        if( ret )
        {
            onCardRemoved( (Card)object );
        }
        return ret;
    }

    @Override
    protected void removeRange( int fromIndex, int toIndex )
    {
        super.removeRange( fromIndex, toIndex );
        onCardCollectionChanged();
    }

    @Override
    public Card set( int index, Card card )
    {
        final Card c = super.set( index, card );
        onCardRemoved( c );
        onCardAdded( card );
        return c;
    }

    @Override
    public boolean retainAll( @NonNull Collection<?> collection )
    {
        final boolean ret = super.retainAll( collection );
        if( ret )
        {
            onCardCollectionChanged();
        }
        return ret;
    }

    @Override
    public boolean removeAll( @NonNull Collection<?> collection )
    {
        final boolean ret = super.removeAll( collection );
        if( ret )
        {
            //noinspection unchecked
            for( Card card : (Collection<Card>)collection )
            {
                onCardRemoved( card );
            }
        }
        return ret;
    }

    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginArray();
        for( Card c : this )
        {
            c.writeToJson( writer );
        }
        writer.endArray();
    }

    public static CardCollection readFromJson( JsonReader reader ) throws IOException
    {
        final CardCollection cards = new CardCollection();

        reader.beginArray();
        while( reader.hasNext() )
        {
            final Card c = Card.readFromJson( reader );
            cards.add( c );
        }
        reader.endArray();

        return cards;
    }
}
