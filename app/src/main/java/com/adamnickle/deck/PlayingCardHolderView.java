package com.adamnickle.deck;

import android.annotation.TargetApi;
import android.content.Context;
import android.os.Build;
import android.util.AttributeSet;
import android.widget.ImageView;


public class PlayingCardHolderView extends ImageView
{
    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public PlayingCardHolderView( Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes )
    {
        super( context, attrs, defStyleAttr, defStyleRes );
        initialize();
    }

    public PlayingCardHolderView( Context context, AttributeSet attrs, int defStyleAttr )
    {
        super( context, attrs, defStyleAttr );
        initialize();
    }

    public PlayingCardHolderView( Context context, AttributeSet attrs )
    {
        super( context, attrs );
        initialize();
    }

    public PlayingCardHolderView( Context context )
    {
        super( context );
        initialize();
    }

    private void initialize()
    {
        setImageResource( R.drawable.card_holder );
    }
}
