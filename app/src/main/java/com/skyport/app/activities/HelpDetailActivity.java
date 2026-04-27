package com.skyport.app.activities;

import android.os.Bundle;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import com.skyport.app.R;

public class HelpDetailActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help_detail);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        TextView tvQuestion = findViewById(R.id.tvQuestionTitle);
        TextView tvAnswer = findViewById(R.id.tvAnswerText);
        ImageView ivIllustration = findViewById(R.id.ivDynamicIllustration);

        String question = getIntent().getStringExtra("QUESTION");
        if (question == null) question = "Unknown Question";

        tvQuestion.setText(question);

        // Generate dynamic answer and illustration based on keywords
        String lowerQ = question.toLowerCase();

        if (lowerQ.contains("arrive") || lowerQ.contains("early")) {
            tvAnswer.setText("We recommend you spare at least an hour before your boarding time. In the hour you'll need to check your luggage in, go through screening, check yourself in (if you haven't done it online yet), and go through security clearance. If you have extra time, you get to explore our tenants and go on a splurge!");
            ivIllustration.setImageResource(R.drawable.ill_arrival);
        } else if (lowerQ.contains("pet") || lowerQ.contains("animal")) {
            tvAnswer.setText("Yes, your furry friends are welcome! Small pets can often be carried through the security checkpoint. They will need to be removed from their carrying case so the case can be scanned. Please check with your airline for specific in-cabin pet requirements.");
            ivIllustration.setImageResource(R.drawable.ill_pet);
        } else if (lowerQ.contains("lost") || lowerQ.contains("forgot") || lowerQ.contains("missing") || lowerQ.contains("recover")) {
            tvAnswer.setText("If you lose an item at the airport, don't panic! Head over to our main Lost & Found desk located near the baggage claim area. Alternatively, you can file a report through our live chat system. Our staff will help track your belongings down as quickly as possible.");
            ivIllustration.setImageResource(R.drawable.ill_lost);
        } else if (lowerQ.contains("security") || lowerQ.contains("size") || lowerQ.contains("carry-on")) {
            tvAnswer.setText("Security is our top priority. Ensure liquids are under 100ml and placed in a clear bag. Standard carry-on dimensions apply to all major airlines. Electronics larger than a phone should be removed from your bag and placed in a separate bin during scanning.");
            ivIllustration.setImageResource(R.drawable.ill_security);
        } else {
            tvAnswer.setText("For any other inquiries, please reach out to our dedicated support staff through the live chat feature below. We are available 24/7 to assist you with your travel needs.");
            ivIllustration.setImageResource(R.drawable.ill_arrival); // Default
        }
    }
}
