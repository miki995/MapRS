package mymap.miki.inc.com.mymap.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.support.v7.preference.PreferenceManager;

import mymap.miki.inc.com.mymap.R;

public class SettingsFragment extends PreferenceFragmentCompat
        implements SharedPreferences.OnSharedPreferenceChangeListener {


    @Override
    public void onCreatePreferences(Bundle bundle, String s) {
        // Add 'general' preferences, defined in the XML file
        addPreferencesFromResource(R.xml.pref_general);
    }

    @Override
    public void onStop() {
        super.onStop();
        // unregister the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .unregisterOnSharedPreferenceChangeListener(this);
    }

    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);
    }


    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

        if (key.equals(getString(R.string.city))) {

            SharedPreferences.Editor editor = getActivity().getSharedPreferences(key, Context.MODE_PRIVATE).edit();
            Preference preference = findPreference(key);
            String citySummary = preference.getKey();
            ListPreference listPreference = (ListPreference) preference;
            String value = listPreference.getValue();

            editor.putString(key, value);
            editor.apply();


            int prefIndex = listPreference.findIndexOfValue(citySummary);
            if (prefIndex >= 0) {

                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }
        }
    }
}