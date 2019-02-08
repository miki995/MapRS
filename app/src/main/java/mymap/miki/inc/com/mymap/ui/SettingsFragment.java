package mymap.miki.inc.com.mymap.ui;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.support.annotation.RequiresApi;
import android.support.v7.preference.PreferenceFragmentCompat;
import android.support.v7.preference.ListPreference;
import android.support.v7.preference.Preference;
import android.util.Log;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Objects;

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

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    @Override
    public void onStart() {
        super.onStart();
        // register the preference change listener
        getPreferenceScreen().getSharedPreferences()
                .registerOnSharedPreferenceChangeListener(this);

        loadCountries();
    }

    private void loadCities(String countryCode) {
        ArrayList<String> entries = new ArrayList<>();
        ArrayList<String> entryValues = new ArrayList<>();

        try {
            JSONArray jsonArray = new JSONArray(loadJSONFromAsset("countryCities.json"));
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject cityObject = (JSONObject) jsonArray.get(i);
                if (cityObject.getString("country").equals(countryCode)) {
                    entryValues.add(cityObject.getString("lat") + "," + cityObject.getString("lng"));
                    entries.add(cityObject.getString("name"));
                }
            }
        } catch (JSONException e) {
            Log.i("TAG", "Improper JSON string");
        }

        ListPreference listPreference = (ListPreference) findPreference(getString(R.string.city));
        listPreference.setEntries(entries.toArray(new String[entries.size()]));
        listPreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
        listPreference.setValueIndex(0);
    }

    private void loadCountries() {
        ArrayList<String> entries = new ArrayList<>();
        ArrayList<String> entryValues = new ArrayList<>();

        try {
            JSONObject json = new JSONObject(loadJSONFromAsset("countries.json"));
            JSONArray jsonArray = json.getJSONArray("countries");
            for (int i = 0; i < jsonArray.length(); ++i) {
                JSONObject countryObject = (JSONObject) jsonArray.get(i);
                entryValues.add(countryObject.getString("code"));
                entries.add(countryObject.getString("name"));
            }
        } catch (JSONException e) {
            Log.i("TAG", "Improper JSON string");
        }

        ListPreference listPreference = (ListPreference) findPreference(getString(R.string.country));
        listPreference.setEntries(entries.toArray(new String[entries.size()]));
        listPreference.setEntryValues(entryValues.toArray(new String[entryValues.size()]));
        listPreference.setValueIndex(0);
        loadCities(entryValues.get(0));
    }

    private String loadJSONFromAsset(String filename) {
        String json = null;

        try {
            InputStream is = this.getResources().getAssets().open(filename);
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");
        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }

        return json;
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
        if (key.equals(getString(R.string.country))) {

            SharedPreferences.Editor editor = getActivity().getSharedPreferences(key, Context.MODE_PRIVATE).edit();
            Preference preference = findPreference(key);
            String countrySummary = preference.getKey();
            ListPreference listPreference = (ListPreference) preference;
            String value = listPreference.getValue();
            editor.putString(key, value);
            editor.apply();

            int prefIndex = listPreference.findIndexOfValue(countrySummary);

            if (prefIndex >= 0) {
                listPreference.setSummary(listPreference.getEntries()[prefIndex]);
            }

            loadCities(value);
        }
    }
}