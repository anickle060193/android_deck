package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentTransaction;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import butterknife.Bind;
import butterknife.ButterKnife;
import butterknife.OnClick;


public class ServerListFragment extends Fragment
{
    private View mMainView;
    @Bind( R.id.serverList ) RecyclerView mRecyclerView;
    @Bind( R.id.emptyView ) TextView mEmptyView;

    private BluetoothFragment mBluetoothFragment;
    private ServerArrayAdapter mAdapter;

    public static ServerListFragment newInstance( BluetoothFragment btFragment )
    {
        final ServerListFragment fragment = new ServerListFragment();
        fragment.mBluetoothFragment = btFragment;
        return fragment;
    }

    @Override
    public void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setRetainInstance( true );

        mBluetoothFragment.registerBluetoothSearchListener( mSearchListener );
    }

    @Override
    public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
    {
        if( mMainView == null )
        {
            mMainView = inflater.inflate( R.layout.fragment_device_list, container, false );
            ButterKnife.bind( this, mMainView );

            mRecyclerView.setLayoutManager( new LinearLayoutManager( getActivity() ) );
            mAdapter = new ServerArrayAdapter();
            mRecyclerView.setAdapter( mAdapter );
            updateViews();
        }
        else
        {
            final ViewGroup parent = (ViewGroup)mMainView.getParent();
            if( parent != null )
            {
                parent.removeView( mMainView );
            }
        }
        return mMainView;
    }

    @Override
    public void onStart()
    {
        super.onStart();

        mBluetoothFragment.findDevices();
    }

    @Override
    public void onResume()
    {
        super.onResume();

        getActivity().setTitle( "Select server:" );
    }

    @Override
    public void onDestroy()
    {
        super.onDestroy();

        mBluetoothFragment.unregisterBluetoothSearchListener( mSearchListener );
    }

    private final BluetoothSearchListener mSearchListener = new BluetoothSearchListener()
    {
        @Override
        public void onDeviceFound( BluetoothDevice device )
        {
            if( !mAdapter.contains( device ) )
            {
                mAdapter.add( device );
                updateViews();
            }
        }
    };

    private void updateViews()
    {
        if( mAdapter.getItemCount() == 0 )
        {
            mEmptyView.setVisibility( View.VISIBLE );
        }
        else
        {
            mEmptyView.setVisibility( View.GONE );
        }
    }

    class ServerViewHolder extends RecyclerView.ViewHolder
    {
        BluetoothDevice Device;
        @Bind( R.id.serverName ) TextView ServerName;

        public ServerViewHolder( View itemView )
        {
            super( itemView );

            ButterKnife.bind( this, itemView );
        }

        @OnClick( R.id.serverItem )
        void onServerItemClick()
        {
            mBluetoothFragment.connectToDevice( Device );
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .setTransition( FragmentTransaction.TRANSIT_FRAGMENT_OPEN )
                    .addToBackStack( null )
                    .replace( R.id.main_content, GameFragment.newInstance( mBluetoothFragment ) )
                    .commit();
        }
    }

    private class ServerArrayAdapter extends ArrayRecyclerAdapter<BluetoothDevice, ServerViewHolder>
    {
        @Override
        public ServerViewHolder onCreateViewHolder( ViewGroup parent, int viewType )
        {
            final View view = LayoutInflater.from( getActivity() ).inflate( R.layout.server_item_view, parent, false );
            return new ServerViewHolder( view );
        }

        @Override
        public void onBindViewHolder( ServerViewHolder holder, int position )
        {
            final BluetoothDevice device = get( position );
            holder.Device = device;
            holder.ServerName.setText( device.getName() );
        }
    }
}
