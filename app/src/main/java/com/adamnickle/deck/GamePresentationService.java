package com.adamnickle.deck;

import android.view.Display;
import android.view.WindowManager;

import com.google.android.gms.cast.CastRemoteDisplayLocalService;

public class GamePresentationService extends CastRemoteDisplayLocalService
{
    private GamePresentation mPresentation;

    @Override
    public void onCreatePresentation( Display display )
    {
        createPresentation( display );
    }

    @Override
    public void onDismissPresentation()
    {
        dismissPresentation();
    }

    private void dismissPresentation()
    {
        if( mPresentation != null )
        {
            mPresentation.dismiss();
            mPresentation = null;
        }
    }

    private void createPresentation( Display display )
    {
        dismissPresentation();

        mPresentation = new GamePresentation( this, display );
        try
        {
            mPresentation.show();
        }
        catch( WindowManager.InvalidDisplayException ex )
        {
            Deck.log( "Unable to show presentation, display was removed.", ex );
            dismissPresentation();
        }
    }
}
