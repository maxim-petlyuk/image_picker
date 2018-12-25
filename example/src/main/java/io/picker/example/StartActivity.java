package io.picker.example;

import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.view.View;

import io.picker.example.databinding.ActivityStartBinding;

public class StartActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        ActivityStartBinding binding = DataBindingUtil.setContentView(this, R.layout.activity_start);
        binding.setHandler(this);
    }

    public void onClick(View view) {
        switch (view.getId()) {
            case R.id.btnFromAct:
                startActivity(new Intent(this, PickerActivity.class));
                break;

            case R.id.btnFromFragment:
                startActivity(new Intent(this, PickerFragmentActivity.class));
                break;
        }
    }
}
