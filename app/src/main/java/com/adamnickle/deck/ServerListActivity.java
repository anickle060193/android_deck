package com.adamnickle.deck;

import android.bluetooth.BluetoothDevice;
import android.content.Intent;
import android.support.v4.app.Fragment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.ProgressBar;
import android.widget.TextView;

public class ServerListActivity extends AppCompatActivity
{
    private ProgressBar mIndeterminateProgressBar;

    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );
        setContentView( R.layout.activity_server_list );

        if( BuildConfig.DEBUG )
        {
            getWindow().addFlags( WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON );
        }

        setSupportActionBar( (Toolbar)findViewById( R.id.server_list_activity_toolbar ) );

        mIndeterminateProgressBar = (ProgressBar)findViewById( R.id.server_list_activity_toolbar_progress_bar );
    }

    public void setProgressVisibility( boolean visible )
    {
        mIndeterminateProgressBar.setVisibility( visible ? View.VISIBLE : View.GONE );
    }

    public static class ServerListFragment extends Fragment
    {
        private View mMainView;
        private RecyclerView mRecyclerView;
        private View mEmptyView;

        private BluetoothSearchFragment mBluetoothSearchFragment;
        private ServerArrayAdapter mAdapter;

        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            setRetainInstance( true );

            mBluetoothSearchFragment = BluetoothSearchFragment.newInstance();
            getActivity().getSupportFragmentManager()
                    .beginTransaction()
                    .add( mBluetoothSearchFragment, BluetoothSearchFragment.FRAGMENT_TAG )
                    .commit();

            mBluetoothSearchFragment.registerBluetoothSearchListener( mSearchListener );
        }

        @Override
        public View onCreateView( LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState )
        {
            if( mMainView == null )
            {
                mMainView = inflater.inflate( R.layout.fragment_server_list, container, false );

                mEmptyView = mMainView.findViewById( R.id.empty_view );

                mRecyclerView = (RecyclerView)mMainView.findViewById( R.id.server_list );
                mAdapter = new ServerArrayAdapter();
                mRecyclerView.setAdapter( mAdapter );
                updateViews();
            }
            else
            {
                Utilities.removeFromParent( mMainView );
            }
            return mMainView;
        }

        @Override
        public void onStart()
        {
            super.onStart();

            mBluetoothSearchFragment.startDiscovery();
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

            mBluetoothSearchFragment.unregisterBluetoothSearchListener( mSearchListener );

            ( (ServerListActivity)getActivity() ).setProgressVisibility( false );
        }

        private final BluetoothSearchFragment.BluetoothSearchListener mSearchListener = new BluetoothSearchFragment.BluetoothSearchListener()
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

            @Override
            public void onDiscoveryStarted()
            {
                ( (ServerListActivity)getActivity() ).setProgressVisibility( true );
            }

            @Override
            public void onDiscoveryEnded()
            {
                ( (ServerListActivity)getActivity() ).setProgressVisibility( false );
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

        private void onServerItemClicked( BluetoothDevice device )
        {
            final Intent intent = new Intent( getActivity(), GameActivity.class )
                    .putExtra( GameActivity.EXTRA_IS_SERVER, false )
                    .putExtra( GameActivity.EXTRA_BLUETOOTH_DEVICE, device );
            startActivity( intent );
        }

        private class ServerViewHolder extends RecyclerView.ViewHolder
        {
            BluetoothDevice Device;
            final View ServerItem;
            final TextView ServerName;

            public ServerViewHolder( View itemView )
            {
                super( itemView );

                ServerItem = itemView.findViewById( R.id.server_item );
                ServerName = (TextView)itemView.findViewById( R.id.server_name );

                ServerItem.setOnClickListener( new View.OnClickListener()
                {
                    @Override
                    public void onClick( View v )
                    {
                        onServerItemClicked( Device );
                    }
                } );
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
}
