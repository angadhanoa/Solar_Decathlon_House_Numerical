package com.example.solar_decathlon_house_numerical;

import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.View;
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
    private AppCompatTextView appCompatTextViewLoginLink;

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
        textInputEditTextConfirmNewPassword = findViewById(R.id.textInputEditTextConfirmNewPassword);
        ButtonChangePassword = findViewById(R.id.password_change);
        appCompatTextViewLoginLink = findViewById(R.id.appCompatTextViewLoginLink);
    }

    private void initListeners() {
        ButtonChangePassword.setOnClickListener(this);
        appCompatTextViewLoginLink.setOnClickListener(this);
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
                postDataToSQLite();
                break;
            case R.id.appCompatTextViewLoginLink:
                finish();
                break;
        }
    }

    private void postDataToSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextNewPassword, textInputLayoutNewPassword, getString(R.string.error_message_password))) {
            return;
        }
        if (!inputValidation.isInputEditTextMatches(textInputEditTextNewPassword, textInputEditTextConfirmNewPassword,
                textInputLayoutConfirmNewPassword, getString(R.string.error_password_match))) {
            return;
        }

        if (!databaseHelper.checkUser(textInputEditTextEmail.getText().toString().trim())) {
            user.setEmail(textInputEditTextEmail.getText().toString().trim());
            user.setPassword(textInputEditTextNewPassword.getText().toString().trim());

            databaseHelper.addUser(user);

            // Snack Bar to show success message that record saved successfully
            Snackbar snackView = Snackbar.make(nestedScrollView, getString(R.string.success_message), Snackbar.LENGTH_LONG);
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
            Snackbar snackView = Snackbar.make(nestedScrollView, getString(R.string.error_email_exists), Snackbar.LENGTH_LONG);
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
}