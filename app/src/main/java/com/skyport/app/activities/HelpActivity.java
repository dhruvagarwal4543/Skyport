package com.skyport.app.activities;

import android.content.Intent;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.skyport.app.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class HelpActivity extends AppCompatActivity {

    private RecyclerView rvFaq;
    private FaqAdapter adapter;
    private List<String> allFaqs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_help);

        findViewById(R.id.btnBack).setOnClickListener(v -> finish());

        allFaqs = Arrays.asList(
                "How early should I arrive at the airport?",
                "Can I take my pet through security?",
                "I forgot my ID, what should I do?",
                "How to claim missing baggage?",
                "Lost and found process?",
                "Carry-on bag size rules?",
                "Service animal screening?"
        );

        rvFaq = findViewById(R.id.rvFaq);
        rvFaq.setLayoutManager(new LinearLayoutManager(this));
        adapter = new FaqAdapter(allFaqs);
        rvFaq.setAdapter(adapter);

        EditText etSearch = findViewById(R.id.etSearch);
        etSearch.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                filterFaqs(s.toString());
            }

            @Override
            public void afterTextChanged(Editable s) {}
        });
    }

    private void filterFaqs(String query) {
        List<String> filteredList = new ArrayList<>();
        for (String faq : allFaqs) {
            if (faq.toLowerCase().contains(query.toLowerCase())) {
                filteredList.add(faq);
            }
        }
        adapter.updateData(filteredList);
    }

    private class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.FaqViewHolder> {

        private List<String> faqs;

        FaqAdapter(List<String> faqs) {
            this.faqs = new ArrayList<>(faqs);
        }

        void updateData(List<String> newFaqs) {
            this.faqs = new ArrayList<>(newFaqs);
            notifyDataSetChanged();
        }

        @NonNull
        @Override
        public FaqViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_faq, parent, false);
            return new FaqViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull FaqViewHolder holder, int position) {
            String question = faqs.get(position);
            holder.tvQuestion.setText(question);
            holder.itemView.setOnClickListener(v -> {
                Intent intent = new Intent(HelpActivity.this, HelpDetailActivity.class);
                intent.putExtra("QUESTION", question);
                startActivity(intent);
            });
        }

        @Override
        public int getItemCount() {
            return faqs.size();
        }

        class FaqViewHolder extends RecyclerView.ViewHolder {
            TextView tvQuestion;

            FaqViewHolder(@NonNull View itemView) {
                super(itemView);
                tvQuestion = itemView.findViewById(R.id.tvQuestion);
            }
        }
    }
}
