package io.picker.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Base64;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;

import com.bumptech.glide.Glide;
import com.picker.file.PickerResult;
import com.picker.file.RxFilePicker;
import com.picker.file.converters.Base64Converter;
import com.picker.file.factory.FileSourceType;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import io.picker.example.databinding.ActivityBase64Binding;
import io.picker.example.databinding.ActivityPickerBinding;
import io.reactivex.Scheduler;
import io.reactivex.SingleSource;
import io.reactivex.android.schedulers.AndroidSchedulers;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;
import io.reactivex.functions.Function;
import io.reactivex.schedulers.Schedulers;

public class Base64ConverterActivity extends AppCompatActivity {

    private final String TAG = "RxPicker";
    private ObservableField<String> mResultPath = new ObservableField<>("");
    private ActivityBase64Binding mBinding;
    private CompositeDisposable mCompositeDisposable;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        Log.d(TAG, "onCreate");

        mCompositeDisposable = new CompositeDisposable();

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_base64);
        mBinding.setHandler(this);

        findViewById(R.id.btnPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickAction();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();

        Log.d(TAG, "onStart");

        if (RxFilePicker.getInstance().hasActiveSubscription()) {
            mCompositeDisposable.add(RxFilePicker.getInstance()
                    .getActiveSubscription(this)
                    .flatMap(new Function<PickerResult, SingleSource<? extends String>>() {
                        @Override
                        public SingleSource<? extends String> apply(PickerResult pickerResult) throws Exception {
                            return new Base64Converter().transform(pickerResult.getFilePath());
                        }
                    })
                    .subscribe(mPickerSuccessConsumer, mPickerErrorConsumer));
        }
    }

    @Override
    protected void onStop() {
        super.onStop();

        Log.d(TAG, "onStop");

        mCompositeDisposable.dispose();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);

        Log.d(TAG, "onSaveInstanceState");
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        super.onRestoreInstanceState(savedInstanceState);

        Log.d(TAG, "onRestoreInstanceState");
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        Log.d(TAG, "onActivityResult");

        RxFilePicker.getInstance().onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        Log.d(TAG, "onRequestPermissionsResult");

        RxFilePicker.getInstance().onRequestPermissionsResult(this, requestCode, permissions, grantResults);
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
                                mCompositeDisposable.add(RxFilePicker.getInstance()
                                        .fromSource(FileSourceType.GALLERY)
                                        .pickFile(Base64ConverterActivity.this)
                                        .flatMap(new Function<PickerResult, SingleSource<? extends String>>() {
                                            @Override
                                            public SingleSource<? extends String> apply(PickerResult pickerResult) throws Exception {
                                                return new Base64Converter().transform(pickerResult.getFilePath());
                                            }
                                        })
                                        .subscribe(mPickerSuccessConsumer, mPickerErrorConsumer));
                                break;

                            case 1:
                                mCompositeDisposable.add(RxFilePicker.getInstance()
                                        .fromSource(FileSourceType.CAMERA)
                                        .pickFile(Base64ConverterActivity.this)
                                        .flatMap(new Function<PickerResult, SingleSource<? extends String>>() {
                                            @Override
                                            public SingleSource<? extends String> apply(PickerResult pickerResult) throws Exception {
                                                return new Base64Converter().transform(pickerResult.getFilePath());
                                            }
                                        })
                                        .subscribe(mPickerSuccessConsumer, mPickerErrorConsumer));
                                break;
                        }
                    }
                })
                .show();
    }

    public ObservableField<String> getResultPath() {
        return mResultPath;
    }

    private Consumer<String> mPickerSuccessConsumer = new Consumer<String>() {
        @Override
        public void accept(String base64Image) throws Exception {
            Log.d(TAG, "onNext");

            byte[] bytes = Base64.decode(base64Image, Base64.DEFAULT);
            Glide.with(getApplicationContext())
                    .load(bytes)
                    .into(mBinding.ivResult);
        }
    };

    private Consumer<Throwable> mPickerErrorConsumer = new Consumer<Throwable>() {
        @Override
        public void accept(Throwable throwable) throws Exception {
            Log.d(TAG, "onError");

            mResultPath.set(String.format("error [%s]", throwable.getClass().getName()));
            throwable.printStackTrace();
        }
    };
}
