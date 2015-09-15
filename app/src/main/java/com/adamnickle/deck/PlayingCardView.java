package com.adamnickle.deck;

import android.content.Context;
import android.support.annotation.NonNull;
import android.support.v4.view.GestureDetectorCompat;
import android.view.GestureDetector;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class PlayingCardView extends ImageView
{
    private static final int[][] CARD_RESOURCES = new int[][]
    {
            { R.drawable.ace_of_spades, R.drawable.two_of_spades, R.drawable.three_of_spades, R.drawable.four_of_spades, R.drawable.five_of_spades, R.drawable.six_of_spades, R.drawable.seven_of_spades, R.drawable.eight_of_spades, R.drawable.nine_of_spades, R.drawable.ten_of_spades, R.drawable.jack_of_spades, R.drawable.queen_of_spades, R.drawable.king_of_spades },
            { R.drawable.ace_of_hearts, R.drawable.two_of_hearts, R.drawable.three_of_hearts, R.drawable.four_of_hearts, R.drawable.five_of_hearts, R.drawable.six_of_hearts, R.drawable.seven_of_hearts, R.drawable.eight_of_hearts, R.drawable.nine_of_hearts, R.drawable.ten_of_hearts, R.drawable.jack_of_hearts, R.drawable.queen_of_hearts, R.drawable.king_of_hearts },
            { R.drawable.ace_of_clubs, R.drawable.two_of_clubs, R.drawable.three_of_clubs, R.drawable.four_of_clubs, R.drawable.five_of_clubs, R.drawable.six_of_clubs, R.drawable.seven_of_clubs, R.drawable.eight_of_clubs, R.drawable.nine_of_clubs, R.drawable.ten_of_clubs, R.drawable.jack_of_clubs, R.drawable.queen_of_clubs, R.drawable.king_of_clubs },
            { R.drawable.ace_of_diamonds, R.drawable.two_of_diamonds, R.drawable.three_of_diamonds, R.drawable.four_of_diamonds, R.drawable.five_of_diamonds, R.drawable.six_of_diamonds, R.drawable.seven_of_diamonds, R.drawable.eight_of_diamonds, R.drawable.nine_of_diamonds, R.drawable.ten_of_diamonds, R.drawable.jack_of_diamonds, R.drawable.queen_of_diamonds, R.drawable.king_of_diamonds }
    };

    private static final float DECELERATION = 0.05f; // m/s/s
    private static final long ANIMATION_DELAY = 20L; // milliseconds

    private final float PIXEL_TO_METER;
    private final float METER_TO_PIXEL;

    private float mVelX;
    private float mVelY;
    private long mLastUpdate;

    private final GestureDetectorCompat mDetector;

    private int mTouchXOffset;
    private int mTouchYOffset;

    public PlayingCardView( Context context )
    {
        super( context );

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        setLayoutParams( params );
        final int suit = (int)( Math.random() * CARD_RESOURCES.length );
        final int rank = (int)( Math.random() * CARD_RESOURCES[ suit ].length );
        setImageResource( CARD_RESOURCES[ suit ][ rank ] );

        mDetector = new GestureDetectorCompat( getContext(), mDetectorListener );
        mDetector.setIsLongpressEnabled( false );

        METER_TO_PIXEL = getResources().getDimension( R.dimen.one_meter );
        PIXEL_TO_METER = 1.0f / METER_TO_PIXEL;
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
                final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
                params.leftMargin = (int)event.getRawX() - parentLocation[ 0 ] - mTouchXOffset;
                params.topMargin = (int)event.getRawY() - parentLocation[ 1 ] - mTouchYOffset;
                this.setLayoutParams( params );
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
        mVelX = velX * PIXEL_TO_METER;
        mVelY = velY * PIXEL_TO_METER;
        mLastUpdate = System.currentTimeMillis();
        PlayingCardView.this.post( mUpdater );
    }

    private void stop()
    {
        mVelX = mVelY = 0.0f;
        PlayingCardView.this.removeCallbacks( mUpdater );
    }

    private void updateVels( float elapsedTime )
    {
    }

    private final Runnable mUpdater = new Runnable()
    {
        @Override
        public void run()
        {
            final long now = System.currentTimeMillis();
            final float elapsedTime = ( now - mLastUpdate ) / 1000.0f;
            mLastUpdate = now;

            final float xSign = Math.signum( mVelX );
            final float ySign = Math.signum( mVelY );

            mVelX -= DECELERATION * xSign * elapsedTime;
            mVelY -= DECELERATION * ySign * elapsedTime;

            final float deltaX = mVelX * METER_TO_PIXEL * elapsedTime;
            final float deltaY = mVelY * METER_TO_PIXEL * elapsedTime;

            final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
            params.leftMargin += deltaX;
            params.topMargin += deltaY;
            setLayoutParams( params );

            final boolean xDone = Math.signum( mVelX ) != xSign || mVelX == 0.0f;
            final boolean yDone = Math.signum( mVelY ) != ySign || mVelY == 0.0f;

            if( xDone )
            {
                mVelX = 0.0f;
            }
            if( yDone )
            {
                mVelY = 0.0f;
            }
            if( xDone && yDone )
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
