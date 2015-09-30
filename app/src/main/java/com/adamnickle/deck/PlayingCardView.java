package com.adamnickle.deck;

import android.annotation.SuppressLint;
import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.adamnickle.deck.Game.Card;


@SuppressLint("ViewConstructor")
public class PlayingCardView extends ImageView
{
    private static final long ANIMATION_DELAY = 20L;
    private static final float DECELERATION = 8000.0f / 10.0f; // px/s/s

    private final Card mCard;

    private boolean mBounceOnWalls = true;

    private final GestureDetectorCompat mDetector;
    private float mVelX;
    private float mVelY;
    private long mLastUpdate;
    private int mTouchXOffset;
    private int mTouchYOffset;

    public PlayingCardView( Context context, Card card )
    {
        super( context );

        mCard = card;

        final int cardWidth = getResources().getDimensionPixelSize( R.dimen.card_width );
        final int cardHeight = getResources().getDimensionPixelSize( R.dimen.card_height );
        setLayoutParams( new FrameLayout.LayoutParams( cardWidth, cardHeight ) );

        setImageResource( mCard.getResource() );

        mDetector = new GestureDetectorCompat( getContext(), mDetectorListener );
    }

    public Card getCard()
    {
        return mCard;
    }

    public void setBounceOnWalls( boolean bounceOnWalls )
    {
        mBounceOnWalls = bounceOnWalls;
    }

    public boolean getBounceOnWalls()
    {
        return mBounceOnWalls;
    }

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
        final boolean detectorResult = mDetector.onTouchEvent( event );

        switch( event.getActionMasked() )
        {
            case MotionEvent.ACTION_MOVE:
                final int[] parentLocation = new int[ 2 ];
                ( (View)getParent() ).getLocationOnScreen( parentLocation );
                setPositionX( (int)event.getRawX() - parentLocation[ 0 ] - mTouchXOffset );
                setPositionY( (int)event.getRawY() - parentLocation[ 1 ] - mTouchYOffset );
                return true;

            case MotionEvent.ACTION_DOWN:
                stop();
                final int[] location = new int[ 2 ];
                this.getLocationOnScreen( location );
                mTouchXOffset = (int)event.getRawX() - location[ 0 ];
                mTouchYOffset = (int)event.getRawY() - location[ 1 ];
                return true;
        }
        return detectorResult;
    }

    private final GestureDetector.OnGestureListener mDetectorListener = new GestureDetector.SimpleOnGestureListener()
    {
        @Override
        public boolean onFling( MotionEvent e1, MotionEvent e2, final float velocityX, final float velocityY )
        {
            PlayingCardView.this.fling( velocityX, velocityY );
            return true;
        }
    };

    private void fling( float velX, float velY )
    {
        stop();
        mVelX = velX;
        mVelY = velY;
        mLastUpdate = System.currentTimeMillis();
        PlayingCardView.this.post( mUpdater );
    }

    private void stop()
    {
        mVelX = mVelY = 0.0f;
        PlayingCardView.this.removeCallbacks( mUpdater );
    }

    private void checkForSlidPast()
    {
        if( getBounceOnWalls() )
        {
            return;
        }
        final ViewGroup parent = (ViewGroup)getParent();
        if( parent == null )
        {
            return;
        }
        if( getRight() < 0 )
        {
            stop();
        }
        else if( getLeft() >= parent.getWidth() )
        {
            stop();
        }

        if( getBottom() < 0 )
        {
            stop();
        }
        else if( getTop() >= parent.getHeight() )
        {
            stop();
        }
    }

    private void checkForBounce()
    {
        if( !getBounceOnWalls() )
        {
            return;
        }
        final ViewGroup parent = (ViewGroup)getParent();
        if( parent == null )
        {
            return;
        }
        if( getLeft() < 0 )
        {
            mVelX = -mVelX;
            setPositionX( 0 );
        }
        else if( getRight() > parent.getWidth() )
        {
            mVelX = -mVelX;
            setPositionX( parent.getWidth() - getWidth() );
        }

        if( getTop() < 0 )
        {
            mVelY = -mVelY;
            setPositionY( 0 );
        }
        else if( getBottom() > parent.getHeight() )
        {
            mVelY = -mVelY;
            setPositionY( parent.getHeight() - getHeight() );
        }
    }

    private final Runnable mUpdater = new Runnable()
    {
        @Override
        public void run()
        {
            final long now = System.currentTimeMillis();
            final float elapsedTime = ( now - mLastUpdate ) / 1000.0f;
            mLastUpdate = now;

            final float deltaX = mVelX * elapsedTime;
            final float deltaY = mVelY * elapsedTime;

            shiftPositionX( (int)deltaX );
            shiftPositionY( (int)deltaY );


            final float signX = Math.signum( mVelX );
            final float signY = Math.signum( mVelY );

            mVelX -= DECELERATION * signX * elapsedTime;
            mVelY -= DECELERATION * signY * elapsedTime;

            if( signX != Math.signum( mVelX ) )
            {
                mVelX = 0.0f;
            }
            if( signY != Math.signum( mVelY ) )
            {
                mVelY = 0.0f;
            }

            checkForSlidPast();
            checkForBounce();

            if( mVelX == 0 && mVelY == 0 )
            {
                PlayingCardView.this.stop();
            }
            else
            {
                PlayingCardView.this.postDelayed( this, ANIMATION_DELAY );
            }
        }
    };
}
