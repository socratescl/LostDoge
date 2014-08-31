package com.socratescl.lostdoge;

import android.app.ActionBar;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import com.parse.LogInCallback;
import com.parse.ParseException;
import com.parse.ParseUser;

public class SignInActivity extends Activity {
    private TextView mSignUpTextView;
    private EditText mUsername;
    private EditText mPassword;
    private Button mLoginButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        setContentView(R.layout.activity_login);
        //hide action bar
        ActionBar actionBar = getActionBar();
        actionBar.hide();
        //handle everything
        mSignUpTextView = (TextView)findViewById(R.id.signUpText);
        mSignUpTextView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(SignInActivity.this, SignUpActivity.class);
                startActivity(intent);
            }
        });
        //
        mUsername = (EditText)findViewById(R.id.userEditText);
        mPassword = (EditText)findViewById(R.id.passwordEditText);
        mLoginButton = (Button)findViewById(R.id.signInButton);
        mLoginButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String username = mUsername.getText().toString();
                String password = mPassword.getText().toString();
                //delete spaces
                username = username.trim();
                password = password.trim();
                //check if any fields are empty
                if(username.isEmpty() || password.isEmpty()){
                    AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                    builder.setMessage(getString(R.string.dialog_message_no_user_pass_error))
                            .setTitle(getString(R.string.dialog_title_error))
                            .setPositiveButton(android.R.string.ok, null);
                    AlertDialog dialog = builder.create();
                    dialog.show();
                }
                else{
                    //login
                    setProgressBarIndeterminateVisibility(true);
                    ParseUser.logInInBackground(username, password, new LogInCallback() {
                        @Override
                        public void done(ParseUser parseUser, ParseException e) {
                            setProgressBarIndeterminateVisibility(false);
                            if (e == null) {
                                //Success
                                LostDogeApplication.updateParseInstallation(parseUser);
                                Intent intent = new Intent(SignInActivity.this, MainActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                                intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                startActivity(intent);
                            } else {
                                AlertDialog.Builder builder = new AlertDialog.Builder(SignInActivity.this);
                                builder.setMessage(e.getMessage())
                                        .setTitle(getString(R.string.dialog_title_error))
                                        .setPositiveButton(android.R.string.ok, null);
                                AlertDialog dialog = builder.create();
                                dialog.show();
                            }
                        }
                    });
                }

            }
        });
    }
}
