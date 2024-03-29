package com.adamnickle.deck;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;

import com.adamnickle.deck.Game.Card;
import com.adamnickle.deck.Game.Player;


public class CardTableLayout extends FrameLayout
{
    public interface OnCardSendListener
    {
        void onCardInHolder( Card card );
        void onCardOutside( Card card );
    }

    private PlayingCardHolderView mCardHolder;
    private final SparseArray<PlayingCardView> mPlayingCardViews = new SparseArray<>();
    private final SparseArray<PlayingCardView> mDraggingViews = new SparseArray<>();

    private OnCardSendListener mListener;
    private Player mPlayer;

    private int mLastWidth = 0;
    private int mLastHeight = 0;

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public CardTableLayout( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );
        initialize();
    }

    public CardTableLayout( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        initialize();
    }

    public CardTableLayout( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        initialize();
    }

    public CardTableLayout( Context context )
    {
        super( context );
        initialize();
    }

    private void initialize()
    {
        setClickable( true );
        mLastWidth = getWidth();
        mLastHeight = getHeight();
    }

    @Override
    protected void onFinishInflate()
    {
        super.onFinishInflate();

        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
        {
            final View view = getChildAt( i );
            if( view instanceof PlayingCardHolderView )
            {
                mCardHolder = (PlayingCardHolderView)view;
                break;
            }
        }
    }

    @Override
    protected void onLayout( boolean changed, int left, int top, int right, int bottom )
    {
        if( changed )
        {
            final int width = right - left;
            final int height = bottom - top;
            final float widthMultiplier = (float)width / mLastWidth;
            final float heightMultiplier = (float)height / mLastHeight;

            for( int i = getChildCount() - 1; i >= 0; i-- )
            {
                final View view = getChildAt( i );
                if( view instanceof PlayingCardView )
                {
                    final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)view.getLayoutParams();
                    params.leftMargin = (int)( params.leftMargin * widthMultiplier );
                    params.topMargin = (int)( params.topMargin * heightMultiplier );
                }
            }

            mLastWidth = width;
            mLastHeight = height;

            requestLayout();
        }

        super.onLayout( changed, left, top, right, bottom );
    }

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
                    final LayoutParams params = (LayoutParams)view.getLayoutParams();
                    final int parentWidth = getWidth();
                    final int parentHeight = getHeight();
                    final int horizontalOffset = Math.round( Utilities.random( -parentWidth * 0.25f, parentWidth * 0.25f ) );
                    final int verticalOffset = Math.round( Utilities.random( -parentHeight * 0.25f, parentHeight * 0.25f ) );
                    params.leftMargin = ( parentWidth - params.width ) / 2 + horizontalOffset;
                    params.topMargin = ( parentHeight - params.height ) / 2 + verticalOffset;
                    view.setLayoutParams( params );
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

        @Override
        public void onNameChanged( Player player, String oldName )
        {
            // Do nothing
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
        if( mCardHolder == null )
        {
            return false;
        }
        final int[] location = new int[ 2 ];
        mCardHolder.getLocationOnScreen( location );
        return location[ 0 ] <= rawX && rawX <= location[ 0 ] + mCardHolder.getWidth()
            && location[ 1 ] <= rawY && rawY <= location[ 1 ] + mCardHolder.getHeight();
    }

    private boolean isOutside( PlayingCardView view )
    {
        if( view.getRight() <= 0 )
        {
            return true;
        }
        if( view.getLeft() >= getWidth() )
        {
            return true;
        }
        if( view.getBottom() <= 0 )
        {
            return true;
        }
        if( view.getTop() >= getHeight() )
        {
            return true;
        }
        return false;
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
                            mListener.onCardInHolder( v.getCard() );
                        }
                    }
                    else if( this.isOutside( v ) )
                    {
                        if( mListener != null )
                        {
                            mListener.onCardOutside( v.getCard() );
                        }
                    }
                }
                break;
            }
        }

        return false;
    }

    public void setOnCardSendListener( OnCardSendListener listener )
    {
        mListener = listener;
    }
}
