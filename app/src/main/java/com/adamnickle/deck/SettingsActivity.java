package com.adamnickle.deck;

import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v7.app.ActionBar;
import android.view.MenuItem;

import java.util.List;


public class SettingsActivity extends AppCompatPreferenceActivity
{
    @Override
    protected void onCreate( Bundle savedInstanceState )
    {
        super.onCreate( savedInstanceState );

        final ActionBar actionBar = getSupportActionBar();
        if( actionBar != null )
        {
            actionBar.setDisplayHomeAsUpEnabled( true );
        }
    }

    @Override
    public boolean onOptionsItemSelected( MenuItem item )
    {
        switch( item.getItemId() )
        {
            case android.R.id.home:
                onBackPressed();
                return true;

            default:
                return super.onOptionsItemSelected( item );
        }
    }

    @Override
    public boolean onIsMultiPane()
    {
        return ( getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_MASK ) >= Configuration.SCREENLAYOUT_SIZE_LARGE;
    }

    @Override
    public void onBuildHeaders( List<Header> target )
    {
        loadHeadersFromResource( R.xml.pref_headers, target );
    }

    private final static Preference.OnPreferenceChangeListener sBindPreferenceSummaryToValueListener = new Preference.OnPreferenceChangeListener()
    {
        @Override
        public boolean onPreferenceChange( Preference preference, Object value )
        {
            final String stringValue = value.toString();

            if( preference instanceof ListPreference )
            {
                final ListPreference listPreference = (ListPreference)preference;
                final int index = listPreference.findIndexOfValue( stringValue );

                if( index >= 0 )
                {
                    preference.setSummary( listPreference.getEntries()[ index ] );
                }
                else
                {
                    preference.setSummary( null );
                }
            }
            else
            {
                preference.setSummary( stringValue );
            }
            return true;
        }
    };

    private static void bindPreferenceSummaryToValue( Preference preference )
    {
        preference.setOnPreferenceChangeListener( sBindPreferenceSummaryToValueListener );

        final String value = PreferenceManager
                .getDefaultSharedPreferences( preference.getContext() )
                .getString( preference.getKey(), "" );
        sBindPreferenceSummaryToValueListener.onPreferenceChange( preference, value );
    }

    @Override
    protected boolean isValidFragment( String fragmentName )
    {
        return PreferenceFragment.class.getName().equals( fragmentName )
            || GeneralPreferenceFragment.class.getName().equals( fragmentName );
    }

    public static class GeneralPreferenceFragment extends PreferenceFragment
    {
        @Override
        public void onCreate( Bundle savedInstanceState )
        {
            super.onCreate( savedInstanceState );
            addPreferencesFromResource( R.xml.pref_general );

            bindPreferenceSummaryToValue( findPreference( Settings.PREF_NAME ) );
            bindPreferenceSummaryToValue( findPreference( Settings.PREF_PLAYER_GAME_BACKGROUND ) );
            bindPreferenceSummaryToValue( findPreference( Settings.PREF_TABLE_GAME_BACKGROUND ) );
        }
    }
}
