package com.adamnickle.deck.Game;

import android.support.annotation.DrawableRes;
import android.util.JsonReader;
import android.util.JsonWriter;

import com.adamnickle.deck.R;

import java.io.IOException;


public class CardPile extends CardCollection
{
    public static abstract class CardPileListener implements CardCollectionListener
    {
        public abstract void onIsFaceUpChanged( boolean isFaceUp );
    }

    private boolean mIsFaceUp = false;

    protected void onIsFaceUpChanged( boolean isFaceUp )
    {
        for( CardCollectionListener listener : mListeners )
        {
            if( listener instanceof CardPileListener )
            {
                ( (CardPileListener)listener ).onIsFaceUpChanged( isFaceUp );
            }
        }
    }

    public boolean isFaceUp()
    {
        return mIsFaceUp;
    }

    public void setIsFaceUp( boolean isFaceUp )
    {
        mIsFaceUp = isFaceUp;
        onIsFaceUpChanged( mIsFaceUp );
    }

    private int getTopIndex( boolean adding )
    {
        if( isFaceUp() )
        {
            return 0;
        }
        else
        {
            if( adding )
            {
                return size();
            }
            else
            {
                return size() - 1;
            }
        }
    }

    public Card peek()
    {
        return this.get( getTopIndex( false ) );
    }

    public Card draw()
    {
        return this.remove( getTopIndex( false ) );
    }

    public void discard( Card card )
    {
        this.add( getTopIndex( true ), card );
    }

    public @DrawableRes int getResource()
    {
        return isFaceUp() ? this.peek().getResource() : R.drawable.card_pile;
    }

    @Override
    public void writeToJson( JsonWriter writer ) throws IOException
    {
        writer.beginObject();

        writer.name( "faceUp" ).value( mIsFaceUp );
        writer.name( "cards" );
        super.writeToJson( writer );

        writer.endObject();
    }

    public static CardPile readFromJson( JsonReader reader ) throws IOException
    {
        final CardPile pile = new CardPile();

        reader.beginObject();
        while( reader.hasNext() )
        {
            final String jsonName = reader.nextName();
            if( "faceUp".equals( jsonName ) )
            {
                final boolean isFaceUp = reader.nextBoolean();
                pile.setIsFaceUp( isFaceUp );
            }
            else if( "cards".equals( jsonName ) )
            {
                final CardCollection collection = CardCollection.readFromJson( reader );
                pile.addAll( collection );
            }
            else
            {
                throw new IllegalStateException( "Invalid JSON name: " + jsonName );
            }
        }
        reader.endObject();

        return pile;
    }
}
