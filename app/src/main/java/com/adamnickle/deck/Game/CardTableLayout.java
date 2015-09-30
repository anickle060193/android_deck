package com.adamnickle.deck.Game;

import android.animation.Animator;
import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.util.SparseArray;
import android.view.MotionEvent;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.adamnickle.deck.PlayingCardView;
import com.adamnickle.deck.R;


public class CardTableLayout extends FrameLayout
{
    public interface OnCardSendListener
    {
        boolean onCardSend( Card card );
    }

    private final ImageView mCardHolder;

    private final SparseArray<PlayingCardView> mDraggingViews = new SparseArray<>();

    private OnCardSendListener mListener;

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

    private PlayingCardView getPlayingCardViewUnder( float rawX, float rawY )
    {
        final int[] location = new int[ 2 ];
        final int childCount = getChildCount();
        for( int i = 0; i < childCount; i++ )
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
                        final int[] location = new int[ 2 ];
                        v.getLocationOnScreen( location );
                        v.setPivotX( rawX - location[ 0 ] );
                        v.setPivotY( rawY - location[ 1 ] );

                        onCardSend( v );
                    }
                }
                break;
            }
        }

        return false;
    }

    public void setOnCardSendListener( OnCardSendListener listener )
    {
        if( mListener == null )
        {
            mListener = listener;
        }
    }

    public void unSetOnCardSendListener( OnCardSendListener listener )
    {
        if( listener == mListener )
        {
            mListener = null;
        }
    }

    protected void onCardSend( final PlayingCardView view )
    {
        if( mListener != null && mListener.onCardSend( view.getCard() ) )
        {
            view.setClickable( false );
            view.setFocusableInTouchMode( false );
            view.animate()
                    .setDuration( 300 )
                    .scaleX( 0.0f )
                    .scaleY( 0.0f )
                    .setListener( new Animator.AnimatorListener()
                    {
                        @Override
                        public void onAnimationEnd( Animator animation )
                        {
                            CardTableLayout.this.removeView( view );
                        }

                        @Override
                        public void onAnimationCancel( Animator animation )
                        {
                            CardTableLayout.this.removeView( view );
                        }

                        @Override
                        public void onAnimationStart( Animator animation )
                        {
                        }

                        @Override
                        public void onAnimationRepeat( Animator animation )
                        {
                        }
                    } )
                    .start();
        }
    }
}
