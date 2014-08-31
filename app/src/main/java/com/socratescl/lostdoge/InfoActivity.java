package com.socratescl.lostdoge;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.parse.ParseException;
import com.parse.ParseFile;
import com.parse.ParseObject;
import com.parse.ParseUser;
import com.parse.SaveCallback;
import com.socratescl.lostdoge.utils.FileHelper;

public class InfoActivity extends Activity {
    protected final int SELECT_PHOTO_REQUEST = 0;
    protected LatLng mPosition;
    protected EditText mNameEditText;
    protected EditText mDescEditText;
    protected TextView mCharsLeftTextView;
    protected ImageView mPreviewImageView;
    protected Button mLoadPictureButton;
    protected Spinner mPetTypeSpinner;
    protected Spinner mPetStatusSpinner;
    protected Uri mPhotoUri;
    private final TextWatcher mTextWatcher = new TextWatcher() {
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {
        }

        public void onTextChanged(CharSequence s, int start, int before, int count) {
            int charsLeft = 200-s.length();
            if(charsLeft == 200){
                mCharsLeftTextView.setText("");
            }
            else{
                mCharsLeftTextView.setText(String.valueOf(charsLeft));
            }
        }

        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_info);
        //get extras
        Bundle extras = getIntent().getExtras();
        if(extras != null){
            mPosition = (LatLng)extras.get("POSITION");
        }
        else{
            Toast.makeText(this, getString(R.string.toast_get_position_error), Toast.LENGTH_LONG).show();
            finish();
        }
        //assign stuff
        mCharsLeftTextView = (TextView)findViewById(R.id.charsLeftTextView);
        mNameEditText = (EditText)findViewById(R.id.petNameEditText);
        mDescEditText = (EditText)findViewById(R.id.descEditText);
        mPreviewImageView = (ImageView)findViewById(R.id.previewImageView);
        mLoadPictureButton = (Button)findViewById(R.id.loadPictureButton);
        mPetTypeSpinner = (Spinner)findViewById(R.id.petTypeSpinner);
        mPetStatusSpinner = (Spinner)findViewById(R.id.petStatusSpinner);
        //manipulate stuff
        mDescEditText.addTextChangedListener(mTextWatcher);
        ArrayAdapter<CharSequence> petStatusAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_pet_status, android.R.layout.simple_spinner_item);
        petStatusAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPetStatusSpinner.setAdapter(petStatusAdapter);
        ArrayAdapter<CharSequence> petTypeAdapter = ArrayAdapter.createFromResource(this,
                R.array.array_pet_type, android.R.layout.simple_spinner_item);
        petTypeAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        mPetTypeSpinner.setAdapter(petTypeAdapter);
        mLoadPictureButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent choosePhotoIntent = new Intent(Intent.ACTION_GET_CONTENT);
                choosePhotoIntent.setType("image/*");
                startActivityForResult(choosePhotoIntent, SELECT_PHOTO_REQUEST);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.info, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (id) {
            case R.id.action_settings:
                return true;
            case R.id.action_ok:
                if(allFieldsOk()){
                    //create parseobject and go back to main activity.
                    ParseObject marker = createMarker();
                    uploadMarker(marker);
                }
                else{
                    Toast.makeText(this, getString(R.string.toast_missing_fields),
                            Toast.LENGTH_LONG).show();
                }
        }
        return super.onOptionsItemSelected(item);
    }

    private void uploadMarker(ParseObject marker) {
        setProgressBarIndeterminateVisibility(true);
        marker.saveInBackground(new SaveCallback() {
            @Override
            public void done(ParseException e) {
                if(e == null) {
                    setProgressBarIndeterminateVisibility(false);
                    Toast.makeText(InfoActivity.this, getString(R.string.toast_marker_sent), Toast.LENGTH_LONG).show();
                    Intent intent = new Intent(InfoActivity.this, MainActivity.class);
                    intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                    intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                    startActivity(intent);
                }
                else{
                    setProgressBarIndeterminateVisibility(false);
                    AlertDialog.Builder builder = new AlertDialog.Builder(InfoActivity.this);
                    builder.setMessage(getString(R.string.dialog_message_upload_error))
                            .setTitle(getString(R.string.dialog_title_error))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
            }
        });
    }

    private ParseObject createMarker() {
        ParseObject object = new ParseObject(ParseConstants.CLASS_MARKER_INFO);
        object.put(ParseConstants.KEY_USER_ID, ParseUser.getCurrentUser().getUsername());
        object.put(ParseConstants.KEY_PET_NAME, mNameEditText.getText().toString());
        object.put(ParseConstants.KEY_PET_DESCRIPTION, mDescEditText.getText().toString());
        object.put(ParseConstants.KEY_PET_LATITUDE, mPosition.latitude);
        object.put(ParseConstants.KEY_PET_LONGITUDE, mPosition.longitude);
        byte[] fileBytes = FileHelper.getByteArrayFromFile(this, mPhotoUri);
        fileBytes = FileHelper.reduceImageForUpload(fileBytes);
        String fileName = FileHelper.getFileName(this, mPhotoUri, ParseConstants.TYPE_IMAGE);
        ParseFile file = new ParseFile(fileName, fileBytes);
        object.put(ParseConstants.KEY_PET_IMAGE, file);
        return object;
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if(resultCode == RESULT_OK){
            if(data == null){
                Toast.makeText(this, getString(R.string.toast_general_error), Toast.LENGTH_LONG).show();
            }
            else{
                mPhotoUri = data.getData();
                mPreviewImageView.setImageURI(mPhotoUri);
                mPreviewImageView.setVisibility(View.VISIBLE);
            }
        }
        else{
            Toast.makeText(this, getString(R.string.toast_general_error), Toast.LENGTH_LONG).show();
        }
    }

    private boolean allFieldsOk() {
        if(isEmpty(mNameEditText) || isEmpty(mDescEditText) || mPhotoUri == null){
            return false;
        }
        else{
            return true;
        }
    }

    private boolean isEmpty(EditText etText) {
        if(etText.getText().toString().trim().length() > 0){
            return false;
        }
        else {
            return true;
        }
    }
}
