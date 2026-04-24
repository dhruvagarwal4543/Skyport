package com.skyport.app.activities;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

import java.util.Calendar;
import java.util.Locale;

public class CompleteProfileActivity extends AppCompatActivity {

    private EditText etFirstName, etMiddleName, etLastName, etPhone;
    private TextView etDOB;
    private Button btnAction;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_complete_profile);

        etFirstName = findViewById(R.id.etFirstName);
        etMiddleName = findViewById(R.id.etMiddleName);
        etLastName = findViewById(R.id.etLastName);
        etDOB = findViewById(R.id.etDOB);
        etPhone = findViewById(R.id.etPhone);
        btnAction = findViewById(R.id.btnAction);

        // Pre-fill Name
        String prefillName = getIntent().getStringExtra("PREFILL_NAME");
        if (prefillName != null && !prefillName.isEmpty()) {
            String[] nameParts = prefillName.split(" ");
            etFirstName.setText(nameParts[0]);
            if (nameParts.length > 1) {
                // If there's multiple spaces, join the rest as Last Name
                StringBuilder lastName = new StringBuilder();
                for (int i = 1; i < nameParts.length; i++) {
                    lastName.append(nameParts[i]).append(" ");
                }
                etLastName.setText(lastName.toString().trim());
            }
        }

        // Date of Birth logic
        etDOB.setOnClickListener(v -> showDatePicker());
        
        // Also capture the icon click
        findViewById(R.id.ivCalendar).setOnClickListener(v -> showDatePicker());

        btnAction.setOnClickListener(v -> {
            if (validateFields()) {
                Intent intent = new Intent(CompleteProfileActivity.this, HomeActivity.class);
                intent.putExtra("USER_NAME", etFirstName.getText().toString());
                intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                startActivity(intent);
            }
        });
    }

    private void showDatePicker() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        int month = calendar.get(Calendar.MONTH);
        int day = calendar.get(Calendar.DAY_OF_MONTH);

        DatePickerDialog datePickerDialog = new DatePickerDialog(
                this,
                (view, year1, month1, dayOfMonth) -> {
                    String selectedDate = String.format(Locale.getDefault(), "%02d/%02d/%d", dayOfMonth, month1 + 1, year1);
                    etDOB.setText(selectedDate);
                },
                year, month, day);
        datePickerDialog.getDatePicker().setMaxDate(System.currentTimeMillis());
        datePickerDialog.show();
    }

    private boolean validateFields() {
        if (TextUtils.isEmpty(etFirstName.getText())) {
            Toast.makeText(this, "First Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etLastName.getText())) {
            Toast.makeText(this, "Last Name is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        if (TextUtils.isEmpty(etDOB.getText().toString().replace("27/03/1997", "").trim())) {
            // Because the hint is 27/03/1997, make sure it's actually set
            if(etDOB.getText().toString().equals("")) {
                Toast.makeText(this, "Date of Birth is required", Toast.LENGTH_SHORT).show();
                return false;
            }
        }
        if (TextUtils.isEmpty(etPhone.getText())) {
            Toast.makeText(this, "Phone Number is required", Toast.LENGTH_SHORT).show();
            return false;
        }
        return true;
    }
}
