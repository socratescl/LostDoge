package com.socratescl.lostdoge;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.Window;

import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.parse.FindCallback;
import com.parse.ParseException;
import com.parse.ParseObject;
import com.parse.ParseQuery;
import com.parse.ParseUser;

import java.util.ArrayList;
import java.util.List;


public class MainActivity extends Activity {
    public static final String TAG = MainActivity.class.getSimpleName();
    protected GoogleMap mMap;
    protected List<ParseObject> mMarkerInfo;
    protected ArrayList<Marker> mMarkers;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_main);
        ParseUser currentUser = ParseUser.getCurrentUser();
        if(currentUser == null) {
            navigateToLogin();
        }
        else{
            Log.i(TAG, currentUser.getUsername());
        }
        //set action bar
        final ActionBar actionBar = getActionBar();
        actionBar.setNavigationMode(ActionBar.NAVIGATION_MODE_STANDARD);
        //set everything else
        setUpMapIfNeeded();
    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch(id) {
            case R.id.action_settings:
                return true;
            case R.id.action_sign_out:
                ParseUser.logOut();
                navigateToLogin();
                break;
            case R.id.add:
                Intent intent = new Intent(this, PositionActivity.class);
                startActivity(intent);
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    private void navigateToLogin() {
        Intent intent = new Intent(this, SignInActivity.class);
        //clear previous task so we cant go back to main from login w/o logging in
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
        startActivity(intent);
    }

    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            mMap = ((MapFragment) getFragmentManager().findFragmentById(R.id.map))
                    .getMap();
        }
        // Check if we were successful in obtaining the map.
        if (mMap != null) {
            // The Map is verified. It is now safe to manipulate the map.
            retrieveMarkers(mMap);
        }
    }
    private void retrieveMarkers(final GoogleMap map) {
        ParseQuery<ParseObject> query = new ParseQuery<ParseObject>(ParseConstants.CLASS_MARKER_INFO);
        //query.whereEqualTo(ParseConstants.KEY_RECIPIENTS_ID, ParseUser.getCurrentUser().getObjectId());
        setProgressBarIndeterminateVisibility(true);
        query.findInBackground(new FindCallback<ParseObject>() {
            @Override
            public void done(List<ParseObject> markers, ParseException e) {
                setProgressBarIndeterminateVisibility(false);
                if(e == null){
                    //markers found
                    mMarkerInfo = markers;
                    mMarkers = new ArrayList<Marker>();
                    int i = 0;
                    for (ParseObject markerInfo : mMarkerInfo) {
                        //create marker
                        //String posterId = markerInfo.getString(ParseConstants.KEY_USER_ID);
                        String petName = markerInfo.getString(ParseConstants.KEY_PET_NAME);
                        String petDescription = markerInfo.getString(ParseConstants.KEY_PET_DESCRIPTION);
                        double petLat = markerInfo.getDouble(ParseConstants.KEY_PET_LATITUDE);
                        double petLng = markerInfo.getDouble(ParseConstants.KEY_PET_LONGITUDE);
                        LatLng petPosition = new LatLng(petLat, petLng);
                        //ParseFile file = markerInfo.getParseFile(ParseConstants.KEY_PET_IMAGE);
                        //Uri fileUri = Uri.parse(file.getUrl());
                        Marker marker = map.addMarker(new MarkerOptions().title(petName).position(petPosition).snippet(petDescription));
                        //add marker
                        mMarkers.add(i, marker);
                        i++;
                    }
                }
            }
        });
    }
}
