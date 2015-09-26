package com.adamnickle.deck;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.CardPile;

import java.util.Collection;


public class CardPileView extends ImageView
{
    private final CardPile mCardPile;

    private int mTouchXOffset;
    private int mTouchYOffset;

    public CardPileView( Context context, CardPile cardPile )
    {
        super( context );

        mCardPile = cardPile;
        mCardPile.addListener( mListener );

        final int cardWidth = getResources().getDimensionPixelSize( R.dimen.card_width );
        final int cardHeight = getResources().getDimensionPixelSize( R.dimen.card_height );
        setLayoutParams( new FrameLayout.LayoutParams( cardWidth, cardHeight ) );

        update();
    }

    public CardPile getCardPile()
    {
        return mCardPile;
    }

    private void update()
    {
        setImageResource( mCardPile.getResource() );
    }

    private final CardPile.CardPileListener mListener = new CardPile.CardPileListener()
    {

        @Override
        public void onCardAdded( Card card )
        {
            update();
        }

        @Override
        public void onCardsAdded( Collection<? extends Card> cards )
        {
            update();
        }

        @Override
        public void onCardRemoved( Card card )
        {
            update();
        }

        @Override
        public void onCardsRemoved( Collection<? extends Card> cards )
        {
            update();
        }

        @Override
        public void onCardCollectionChanged()
        {
            update();
        }

        @Override
        public void onIsFaceUpChanged( boolean isFaceUp )
        {
            update();
        }
    };

    public void setPositionX( int x )
    {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.leftMargin = x;
        setLayoutParams( params );
    }

    public void setPositionY( int y )
    {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.topMargin = y;
        setLayoutParams( params );
    }

    public void shiftPositionX( int deltaX )
    {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.leftMargin += deltaX;
        setLayoutParams( params );
    }

    public void shiftPositionY( int deltaY )
    {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        params.topMargin += deltaY;
        setLayoutParams( params );
    }

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event )
    {
        switch( event.getActionMasked() )
        {
            case MotionEvent.ACTION_MOVE:
                final int[] parentLocation = new int[ 2 ];
                ( (View)getParent() ).getLocationOnScreen( parentLocation );
                setPositionX( (int)event.getRawX() - parentLocation[ 0 ] - mTouchXOffset );
                setPositionY( (int)event.getRawY() - parentLocation[ 1 ] - mTouchYOffset );
                break;

            case MotionEvent.ACTION_DOWN:
                final int[] location = new int[ 2 ];
                this.getLocationOnScreen( location );
                mTouchXOffset = (int)event.getRawX() - location[ 0 ];
                mTouchYOffset = (int)event.getRawY() - location[ 1 ];
                break;
        }
        return super.onTouchEvent( event );
    }
}
