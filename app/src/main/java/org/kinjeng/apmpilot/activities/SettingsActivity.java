package org.kinjeng.apmpilot.activities;


import android.annotation.TargetApi;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.res.Configuration;
import android.os.Build;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;
import android.preference.PreferenceFragment;
import android.preference.PreferenceManager;
import android.support.v4.app.NavUtils;
import android.support.v7.app.ActionBar;
import android.view.KeyEvent;
import android.view.MenuItem;

import com.o3dr.services.android.lib.drone.property.Type;
import com.o3dr.services.android.lib.drone.property.VehicleMode;

import org.kinjeng.apmpilot.R;

import java.util.ArrayList;
import java.util.List;

/**
 * A {@link PreferenceActivity} that presents a set of application settings. On
 * handset devices, settings are presented as a single list. On tablets,
 * settings are split by category, with category headers shown to the left of
 * the list of settings.
 * <p>
 * See <a href="http://developer.android.com/design/patterns/settings.html">
 * Android Design: Settings</a> for design guidelines and the <a
 * href="http://developer.android.com/guide/topics/ui/settings.html">Settings
 * API Guide</a> for more information on developing a Settings UI.
 */
public class SettingsActivity extends AppCompatPreferenceActivity {

    public static void updatePreferenceSummary(Preference preference) {
        if (preference instanceof ListPreference) {
            preference.setSummary(((ListPreference) preference).getEntry());
        }
        if (preference instanceof EditTextPreference) {
            preference.setSummary(((EditTextPreference) preference).getText());
        }
    }

    /**
     * Helper method to determine if the device has an extra-large screen. For
     * example, 10" tablets are extra-large.
     */
    private static boolean isXLargeTablet(Context context) {
        return (context.getResources().getConfiguration().screenLayout
                & Configuration.SCREENLAYOUT_SIZE_MASK) >= Configuration.SCREENLAYOUT_SIZE_XLARGE;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setupActionBar();
    }

    /**
     * Set up the {@link android.app.ActionBar}, if the API is available.
     */
    private void setupActionBar() {
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            // Show the Up button in the action bar.
            actionBar.setDisplayHomeAsUpEnabled(true);
        }
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        int id = item.getItemId();
        if (id == android.R.id.home) {
            if (!super.onMenuItemSelected(featureId, item)) {
                NavUtils.navigateUpFromSameTask(this);
            }
            return true;
        }
        return super.onMenuItemSelected(featureId, item);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public boolean onIsMultiPane() {
        return isXLargeTablet(this);
    }

    /**
     * {@inheritDoc}
     */
    @Override
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public void onBuildHeaders(List<Header> target) {
        loadHeadersFromResource(R.xml.pref_headers, target);
    }

    /**
     * This method stops fragment injection in malicious applications.
     * Make sure to deny any unknown fragments here.
     */
    protected boolean isValidFragment(String fragmentName) {
        return PreferenceFragment.class.getName().equals(fragmentName)
                || ConnectionPreferenceFragment.class.getName().equals(fragmentName)
                || VideoPreferenceFragment.class.getName().equals(fragmentName)
                || JoystickPreferenceFragment.class.getName().equals(fragmentName);
    }

    /**
     * This fragment shows connection preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class ConnectionPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_connection);
            setHasOptionsMenu(true);
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                updatePreferenceSummary(getPreferenceScreen().getPreference(i));
            }
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            updatePreferenceSummary(preference);
        }

    }

    /**
     * This fragment shows video preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class VideoPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_video);
            setHasOptionsMenu(true);
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                updatePreferenceSummary(getPreferenceScreen().getPreference(i));
            }
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            updatePreferenceSummary(preference);
        }

    }

    /**
     * This fragment shows joystick preferences only. It is used when the
     * activity is showing a two-pane settings UI.
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    public static class JoystickPreferenceFragment extends PreferenceFragment implements SharedPreferences.OnSharedPreferenceChangeListener {
        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            addPreferencesFromResource(R.xml.pref_joystick);
            CharSequence[] lrEntries = {
                    "Button L1", "Button L2", "Button R1", "Button R2"
            };
            CharSequence[] lrValues = {
                    Integer.valueOf(KeyEvent.KEYCODE_BUTTON_L1).toString(),
                    Integer.valueOf(KeyEvent.KEYCODE_BUTTON_L2).toString(),
                    Integer.valueOf(KeyEvent.KEYCODE_BUTTON_R1).toString(),
                    Integer.valueOf(KeyEvent.KEYCODE_BUTTON_R2).toString()
            };

            ListPreference lp = (ListPreference)findPreference("pref_drone_arm");
            lp.setEntries(lrEntries);
            lp.setEntryValues(lrValues);
            lp = (ListPreference)findPreference("pref_drone_disarm");
            lp.setEntries(lrEntries);
            lp.setEntryValues(lrValues);
            lp = (ListPreference)findPreference("pref_drone_land");
            lp.setEntries(lrEntries);
            lp.setEntryValues(lrValues);

            List<CharSequence> modeEntries = new ArrayList<>();
            List<CharSequence> modeValues = new ArrayList<>();

            for (VehicleMode vehicleMode : VehicleMode.getVehicleModePerDroneType(Type.TYPE_COPTER)) {
                if (vehicleMode.getMode() != VehicleMode.COPTER_LAND.getMode()) {
                    modeEntries.add(vehicleMode.getLabel());
                    modeValues.add(Integer.valueOf(vehicleMode.getMode()).toString());
                }
            }

            CharSequence[] a = {};
            lp = (ListPreference)findPreference("pref_joystick_button_a");
            lp.setEntries(modeEntries.toArray(a));
            lp.setEntryValues(modeValues.toArray(a));

            lp = (ListPreference)findPreference("pref_joystick_button_b");
            lp.setEntries(modeEntries.toArray(a));
            lp.setEntryValues(modeValues.toArray(a));

            lp = (ListPreference)findPreference("pref_joystick_button_x");
            lp.setEntries(modeEntries.toArray(a));
            lp.setEntryValues(modeValues.toArray(a));

            lp = (ListPreference)findPreference("pref_joystick_button_y");
            lp.setEntries(modeEntries.toArray(a));
            lp.setEntryValues(modeValues.toArray(a));

            setHasOptionsMenu(true);
            for (int i = 0; i < getPreferenceScreen().getPreferenceCount(); i++) {
                updatePreferenceSummary(getPreferenceScreen().getPreference(i));
            }
            PreferenceManager.getDefaultSharedPreferences(getActivity()).registerOnSharedPreferenceChangeListener(this);
        }

        @Override
        public boolean onOptionsItemSelected(MenuItem item) {
            int id = item.getItemId();
            if (id == android.R.id.home) {
                startActivity(new Intent(getActivity(), SettingsActivity.class));
                return true;
            }
            return super.onOptionsItemSelected(item);
        }

        @Override
        public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
            Preference preference = findPreference(key);
            updatePreferenceSummary(preference);
        }

    }
}
