package com.adamnickle.deck;

import android.content.Context;
import android.os.Bundle;
import android.view.Display;

import com.google.android.gms.cast.CastPresentation;


public class GamePresentation extends CastPresentation
{
    public GamePresentation( Context serviceContext, Display display )
    {
        super( serviceContext, display );
    }

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        if( savedInstanceState == null )
        {

        }
    }
}
