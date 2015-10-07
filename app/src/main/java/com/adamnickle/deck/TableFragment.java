package com.adamnickle.deck;

import android.app.Fragment;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;


public class TableFragment extends Fragment
{
    private View mMainView;
    private CardTableLayout mCardTableLayout;

    public static TableFragment newInstance()
    {
        final TableFragment fragment = new TableFragment();
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {

        }
        else
        {
            Utilities.removeFromParent( mMainView );
        }
        return mMainView;
    }
}
