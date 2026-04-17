package com.skyport.app.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.TextPaint;
import android.text.method.LinkMovementMethod;
import android.text.style.ClickableSpan;
import android.text.style.StyleSpan;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Button btnSignUp = findViewById(R.id.btnSignUp);
        TextView tvFooter = findViewById(R.id.tvFooter);

        btnSignUp.setOnClickListener(v -> {
            startActivity(new Intent(SplashActivity.this, SignupActivity.class));
        });

        setupClickableFooter(tvFooter);
    }

    private void setupClickableFooter(TextView tvFooter) {
        String fullText = "Already have an account? Log in";
        SpannableString spannableString = new SpannableString(fullText);

        // Find the index of "Log in"
        int startIndex = fullText.indexOf("Log in");
        int endIndex = fullText.length();

        if (startIndex != -1) {
            // Apply bold style to "Log in"
            spannableString.setSpan(new StyleSpan(android.graphics.Typeface.BOLD), startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

            // Make "Log in" clickable
            ClickableSpan clickableSpan = new ClickableSpan() {
                @Override
                public void onClick(@NonNull View widget) {
                    startActivity(new Intent(SplashActivity.this, AuthActivity.class));
                }

                @Override
                public void updateDrawState(@NonNull TextPaint ds) {
                    super.updateDrawState(ds);
                    ds.setUnderlineText(true);
                    ds.setColor(getResources().getColor(R.color.skyport_navy));
                }
            };
            spannableString.setSpan(clickableSpan, startIndex, endIndex, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }

        tvFooter.setText(spannableString);
        tvFooter.setMovementMethod(LinkMovementMethod.getInstance());
        tvFooter.setHighlightColor(Color.TRANSPARENT);
    }
}
