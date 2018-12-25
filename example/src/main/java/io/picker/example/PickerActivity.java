package io.picker.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.picker.file.RxFilePicker;
import com.picker.file.PickerResult;
import com.picker.file.factory.FileSourceType;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import io.picker.example.databinding.ActivityPickerBinding;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class PickerActivity extends AppCompatActivity {

    private final String TAG = "RxPicker";
    private RxFilePicker mRxFilePicker = new RxFilePicker();
    private ObservableField<String> mResultPath = new ObservableField<>("");
    private ActivityPickerBinding mBinding;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mCompositeDisposable = new CompositeDisposable();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_picker);
        mBinding.setHandler(this);

        findViewById(R.id.btnPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickAction();
            }
        });
        findViewById(R.id.btnNewAct).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(PickerActivity.this, EmptyActivity.class));
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        mCompositeDisposable.add(mRxFilePicker.getPickerFileReady(this)
                .subscribe(new Consumer<PickerResult>() {
                    @Override
                    public void accept(PickerResult pickerResult) throws Exception {
                        Log.d(TAG, "onNext");

                        String path = pickerResult.getFilePath().toString();
                        Picasso.get().load(path).config(Bitmap.Config.RGB_565).fit().centerCrop().into(mBinding.ivResult);
                        mResultPath.set(pickerResult.getFilePath().toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        Log.d(TAG, "onError");

                        mResultPath.set(String.format("error [%s]", throwable.getClass().getName()));
                        throwable.printStackTrace();
                    }
                }));
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        mCompositeDisposable.dispose();
    }

    private void showPickAction() {
        ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, Arrays.asList("Gallery", "Camera"));
        new AlertDialog.Builder(this)
                .setTitle("Pick")
                .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        switch (which) {
                            case 0:
                                mRxFilePicker.fromSource(FileSourceType.GALLERY).pickFile(PickerActivity.this);
                                break;

                            case 1:
                                mRxFilePicker.fromSource(FileSourceType.CAMERA).pickFile(PickerActivity.this);
                                break;
                        }
                    }
                })
                .show();
    }

    @Override
    protected void onResume() {
        super.onResume();

        Log.d(TAG, "onResume");
    }

    @Override
    protected void onPause() {
        super.onPause();

        Log.d(TAG, "onPause");
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        mRxFilePicker.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        mRxFilePicker.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        mRxFilePicker.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        mRxFilePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();

        Log.d(TAG, "onDestroy");
    }

    public ObservableField<String> getResultPath() {
        return mResultPath;
    }
}
