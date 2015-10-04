package com.adamnickle.deck;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Player;


public class CardTableLayout extends FrameLayout
{
    public interface CardSendListener
    {
        void onCardSend( Card card );
    }

    private final ImageView mCardHolder;
    private final SparseArray<PlayingCardView> mPlayingCardViews = new SparseArray<>();
    private final SparseArray<PlayingCardView> mDraggingViews = new SparseArray<>();

    private CardSendListener mListener;
    private Player mPlayer;

    //region Constructors
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardTableLayout( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );

        mCardHolder = new ImageView( context, attrs, defStyleAttr, defStyleRes );
        initCardHolder();
    }

    public CardTableLayout( Context context )
    {
        this( context, null );
    }

    public CardTableLayout( Context context, AttributeSet attrs )
    {
        this( context, attrs, 0 );
    }

    public CardTableLayout( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );

        mCardHolder = new ImageView( context, attrs, defStyleAttr );
        initCardHolder();
    }

    private void initCardHolder()
    {
        mCardHolder.setImageResource( R.drawable.card_holder );

        final LayoutParams params = generateDefaultLayoutParams();
        params.width = getResources().getDimensionPixelSize( R.dimen.card_width );
        params.height = getResources().getDimensionPixelSize( R.dimen.card_height );
        final int margin = getResources().getDimensionPixelSize( R.dimen.card_holder_margin );
        params.setMargins( margin, margin, margin, margin );
        mCardHolder.setLayoutParams( params );

        this.addView( mCardHolder );
    }
    //endregion

    public void setPlayer( Player player )
    {
        if( mPlayer != null )
        {
            mPlayer.unregisterListener( mPlayerListener );
        }
        mPlayer = player;
        if( mPlayer != null )
        {
            mPlayer.registerListener( mPlayerListener );
        }
    }

    public Player getPlayer()
    {
        return mPlayer;
    }

    private final Player.PlayerListener mPlayerListener = new Player.PlayerListener()
    {
        @Override
        public void onCardAdded( Player player, final Card card )
        {
            CardTableLayout.this.post( new Runnable()
            {
                @Override
                public void run()
                {
                    final PlayingCardView view = new PlayingCardView( getContext(), card );
                    mPlayingCardViews.put( card.getCardId(), view );
                    addView( view );
                }
            } );
        }

        @Override
        public void onCardRemoved( Player player, final Card card )
        {
            CardTableLayout.this.post( new Runnable()
            {
                @Override
                public void run()
                {
                    final PlayingCardView view = mPlayingCardViews.get( card.getCardId() );
                    mPlayingCardViews.remove( card.getCardId() );
                    removeView( view );
                }
            } );
        }
    };

    private PlayingCardView getPlayingCardViewUnder( float rawX, float rawY )
    {
        final int[] location = new int[ 2 ];
        for( int i = getChildCount() - 1; i >= 0; i-- )
        {
            final View view = getChildAt( i );
            if( view instanceof PlayingCardView )
            {
                view.getLocationOnScreen( location );
                if( location[ 0 ] <= rawX && rawX <= location[ 0 ] + view.getWidth()
                 && location[ 1 ] <= rawY && rawY <= location[ 1 ] + view.getHeight() )
                {
                    return (PlayingCardView)view;
                }
            }
        }
        return null;
    }

    private boolean isInCardHolder( float rawX, float rawY )
    {
        final int[] location = new int[ 2 ];
        mCardHolder.getLocationOnScreen( location );
        return location[ 0 ] <= rawX && rawX <= location[ 0 ] + mCardHolder.getWidth()
            && location[ 1 ] <= rawY && rawY <= location[ 1 ] + mCardHolder.getHeight();
    }

    @Override
    public boolean onInterceptTouchEvent( MotionEvent ev )
    {
        switch( ev.getActionMasked() )
        {
            case MotionEvent.ACTION_DOWN:
            case MotionEvent.ACTION_POINTER_DOWN:
            {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId( pointerIndex );
                final PlayingCardView v = getPlayingCardViewUnder( ev.getRawX(), ev.getRawY() );
                if( v != null )
                {
                    mDraggingViews.put( pointerId, v );
                }
                break;
            }

            case MotionEvent.ACTION_UP:
            case MotionEvent.ACTION_POINTER_UP:
            case MotionEvent.ACTION_CANCEL:
            {
                final int pointerIndex = ev.getActionIndex();
                final int pointerId = ev.getPointerId( pointerIndex );
                final PlayingCardView v = mDraggingViews.get( pointerId );
                if( v != null )
                {
                    mDraggingViews.remove( pointerId );
                    final float rawX = ev.getRawX();
                    final float rawY = ev.getRawY();
                    if( this.isInCardHolder( rawX, rawY ) )
                    {
                        if( mListener != null )
                        {
                            mListener.onCardSend( v.getCard() );
                        }
                    }
                }
                break;
            }
        }

        return false;
    }

    public void setOnCardSendListener( CardSendListener listener )
    {
        mListener = listener;
    }
}
