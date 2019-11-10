package com.example.solar_decathlon_house_numerical;

import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.widget.NestedScrollView;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatTextView;
import android.view.Gravity;
import android.widget.Button;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity implements View.OnClickListener {
    private final AppCompatActivity activity = LoginActivity.this;
    private NestedScrollView nestedScrollView;
    private TextInputLayout textInputLayoutEmail;
    private TextInputLayout textInputLayoutPassword;
    private TextInputEditText textInputEditTextEmail;
    private TextInputEditText textInputEditTextPassword;
    private Button button1;
    private AppCompatTextView textViewLinkRegister;
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
        textInputEditTextPassword = findViewById(R.id.textInputEditTextPassword);
        button1 = findViewById(R.id.button1);
        textViewLinkRegister = findViewById(R.id.textViewLinkRegister);
    }

    private void initListeners() {
        button1.setOnClickListener(this);
        textViewLinkRegister.setOnClickListener(this);
    }

    private void initObjects() {
        databaseHelper = new DatabaseHelper(activity);
        inputValidation = new InputValidation(activity);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.button1:
                verifyFromSQLite();
                break;
            case R.id.textViewLinkRegister:
                Intent intentRegister = new Intent(getApplicationContext(), RegisterActivity.class);
                startActivity(intentRegister);
                break;
        }
    }

    private void verifyFromSQLite() {
        if (!inputValidation.isInputEditTextFilled(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextEmail(textInputEditTextEmail, textInputLayoutEmail, getString(R.string.error_message_email))) {
            return;
        }
        if (!inputValidation.isInputEditTextFilled(textInputEditTextPassword, textInputLayoutPassword, getString(R.string.error_message_email))) {
            return;
        }

        if (databaseHelper.checkUser(textInputEditTextEmail.getText().toString().trim()
                , textInputEditTextPassword.getText().toString().trim())) {
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
        textInputEditTextPassword.setText(null);
    }
}