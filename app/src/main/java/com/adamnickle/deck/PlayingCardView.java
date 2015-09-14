package com.adamnickle.deck;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;


public class PlayingCardView extends ImageView
{
    private int mTouchXOffset;
    private int mTouchYOffset;

    public PlayingCardView( Context context )
    {
        super( context );

        final FrameLayout.LayoutParams params = new FrameLayout.LayoutParams( ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT );
        setLayoutParams( params );
        setImageResource( R.drawable.ten_of_clubs );
    }

    @Override
    public boolean onTouchEvent( @NonNull MotionEvent event )
    {
        final FrameLayout.LayoutParams params = (FrameLayout.LayoutParams)getLayoutParams();
        switch( event.getActionMasked() )
        {
            case MotionEvent.ACTION_MOVE:
                final int[] parentLocation = new int[ 2 ];
                ( (View)getParent() ).getLocationOnScreen( parentLocation );
                params.leftMargin = (int)event.getRawX() - parentLocation[ 0 ] - mTouchXOffset;
                params.topMargin = (int)event.getRawY() - parentLocation[ 1 ] - mTouchYOffset;
                this.setLayoutParams( params );
                return true;

            case MotionEvent.ACTION_DOWN:
                final int[] location = new int[ 2 ];
                this.getLocationOnScreen( location );
                mTouchXOffset = (int)event.getRawX() - location[ 0 ];
                mTouchYOffset = (int)event.getRawY() - location[ 1 ];
                return true;

            case MotionEvent.ACTION_UP:
                return true;
        }
        return false;
    }
}
