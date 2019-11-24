package com.example.solar_decathlon_house_numerical;

import android.content.Context;
import android.graphics.Color;
import android.inputmethodservice.Keyboard;
import android.inputmethodservice.KeyboardView;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.text.method.PasswordTransformationMethod;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.TextView;

public class EditPassword extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = EditPassword.this;

    private NestedScrollView nestedScrollView;

    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutNewPassword;
    private TextInputLayout textInputLayoutConfirmNewPassword;

    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextNewPassword;
    private TextInputEditText textInputEditTextConfirmNewPassword;

    private Button ButtonChangePassword;

    private InputValidation inputValidation;
    private DatabaseHelper databaseHelper;
    private User user;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_password);
        getSupportActionBar().hide();
        initViews();
        initListeners();
        initObjects();
    }

    private void initViews() {
        nestedScrollView = findViewById(R.id.nestedScrollView);
        textInputLayoutEmail = findViewById(R.id.textInputLayoutEmail);
        textInputLayoutNewPassword = findViewById(R.id.textInputLayoutNewPassword);
        textInputLayoutConfirmNewPassword = findViewById(R.id.textInputLayoutConfirmNewPassword);
        textInputEditTextEmail = findViewById(R.id.textInputEditTextEmail);
        textInputEditTextNewPassword = findViewById(R.id.textInputEditTextNewPassword);
        textInputEditTextNewPassword.setTransformationMethod(new HidePassword());
        textInputEditTextConfirmNewPassword = findViewById(R.id.textInputEditTextConfirmNewPassword);
        textInputEditTextConfirmNewPassword.setTransformationMethod(new HidePassword());
        ButtonChangePassword = findViewById(R.id.password_change);
    }

    private void initListeners() {
        ButtonChangePassword.setOnClickListener(this);
    }

    private void initObjects() {
        inputValidation = new InputValidation(activity);
        databaseHelper = new DatabaseHelper(activity);
        user = new User();
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.password_change:
                closeKeyboard();
                postDataToSQLite();
                break;
        }
    }

    private void postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextNewPassword, textInputLayoutNewPassword, getString(R.string.error_message_password))) {
            return;
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextNewPassword, textInputEditTextConfirmNewPassword,
                textInputLayoutConfirmNewPassword, getString(R.string.error_password_match))) {
            return;
        }

        if (databaseHelper.checkUser(textInputEditTextEmail.getText().toString().trim())) {
            String nameOfUser = user.getName();
            databaseHelper.deleteUser(user);

            user.setName(nameOfUser);
            user.setEmail(textInputEditTextEmail.getText().toString().trim());
            user.setPassword(textInputEditTextNewPassword.getText().toString().trim());
            databaseHelper.updateUser(user);

            // Snack Bar to show success message that record saved successfully
            Snackbar snackView = Snackbar.make(nestedScrollView, "Password Change Successful", Snackbar.LENGTH_LONG);
            View snackbarView = snackView.getView();
            // get textview inside snackbar view
            TextView snackTextView = snackbarView.findViewById(android.support.design.R.id.snackbar_text);
            // set text to center
            snackTextView.setTextAlignment(View.TEXT_ALIGNMENT_CENTER);
            snackTextView.setTextColor(Color.WHITE);
            // show the snackbar
            snackView.show();
            emptyInputEditText();
        } else {
            // Snack Bar to show error message that record already exists
            Snackbar snackView = Snackbar.make(nestedScrollView, "Could not find the Email", Snackbar.LENGTH_LONG);
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
        textInputEditTextNewPassword.setText(null);
        textInputEditTextConfirmNewPassword.setText(null);
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

    private void closeKeyboard() {
        View view = this.getCurrentFocus();

        if(view != null){
            InputMethodManager inputManager =(InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
            inputManager.hideSoftInputFromWindow(view.getWindowToken(), 0);
        }
    }
}