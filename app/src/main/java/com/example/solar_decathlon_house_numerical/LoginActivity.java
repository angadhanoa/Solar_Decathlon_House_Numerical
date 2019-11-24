package com.example.solar_decathlon_house_numerical;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.PasswordTransformationMethod;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = LoginActivity.this;
    private NestedScrollView nestedScrollView;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText password;
    private Button button1;
    private AppCompatTextView textViewLinkRegister;
    private AppCompatTextView textViewLinkForgotPassword;
    private InputValidation inputValidation;
    private DatabaseHelper databaseHelper;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        getSupportActionBar().hide();
        initViews();
        initListeners();
        initObjects();
    }

    private void initViews() {
        nestedScrollView = findViewById(R.id.nestedScrollView);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutPassword = findViewById(R.id.textInputLayoutPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        password = findViewById(R.id.textInputEditTextPassword);
        password.setTransformationMethod(new HidePassword());
        button1 = findViewById(R.id.button1);
        textViewLinkRegister = findViewById(R.id.textViewLinkRegister);
        textViewLinkForgotPassword = findViewById(R.id.textViewLinkForgotPassword);
    }

    private void initListeners() {
        button1.setOnClickListener(this);
        textViewLinkRegister.setOnClickListener(this);
        textViewLinkForgotPassword.setOnClickListener(this);
    }

    private void initObjects() {
        databaseHelper = new DatabaseHelper(activity);
        inputValidation = new InputValidation(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                closeKeyboard();
                verifyFromSQLite();
                break;
            case R.id.textViewLinkRegister:
                Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentRegister);
                break;
            case R.id.textViewLinkForgotPassword:
                Intent intentEditPassword = new Intent(getApplicationContext(), EditPassword.class);
                startActivity(intentEditPassword);
                break;
        }
    }

    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if(view != null){
            InputMethodManager inputManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }

    private void verifyFromSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(password, textInputLayoutPassword, getString(R.string.error_message_email))) {
            return;
        }

        if (databaseHelper.checkUser(textInputEditTextEmail.getText().toString().trim()
                , password.getText().toString().trim())) {
            Intent intentLogin = new Intent(getApplicationContext(), FeatureSelection.class);
            intentLogin.putExtra("EMAIL", textInputEditTextEmail.getText().toString().trim());
            emptyInputEditText();
            startActivity(intentLogin);
        } else {
            // Snack Bar to show success message that record is wrong
            Snackbar snackView = Snackbar.make(nestedScrollView, getString(R.string.error_valid_email_password), Snackbar.LENGTH_LONG);
            View snackbarView = snackView.getView();
            // get textview inside snackbar view
            TextView snackTextView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            // set text to center
            snackTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackTextView.setTextColor(Color.WHITE);
            // show the snackbar
            snackView.show();
        }
    }

    private void emptyInputEditText() {
        textInputEditTextEmail.setText(null);
        password.setText(null);
    }

    public class HidePassword extends PasswordTransformationMethod
    {
        @Override
        public CharSequence getTransformation(CharSequence source, View view) {
            return new PasswordCharSequence(source);
        }

        private class PasswordCharSequence implements CharSequence {
            private CharSequence sourceOfPassword;
            public PasswordCharSequence(CharSequence source) {
                sourceOfPassword = source;
            }
            public char charAt(int index) {
                return '*';
            }
            public int length() {
                return sourceOfPassword.length();
            }
            public CharSequence subSequence(int start, int end) {
                return sourceOfPassword.subSequence(start, end);
            }
        }
    }
}