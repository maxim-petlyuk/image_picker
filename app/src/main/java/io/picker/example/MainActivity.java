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
import android.view.View;
import android.widget.ArrayAdapter;

import com.picker.file.FilePickerDelegate;
import com.picker.file.PickerResult;
import com.picker.file.factory.FileSourceFactory;
import com.picker.file.factory.FileSourceType;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import io.picker.example.databinding.ActivityMainBinding;
import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private FilePickerDelegate mFilePickerDelegate;
    private ObservableField<String> mResultPath = new ObservableField<>("");
    private ActivityMainBinding mBinding;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mBinding = DataBindingUtil.setContentView(this, R.layout.activity_main);
        mBinding.setHandler(this);

        findViewById(R.id.btnPick).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showPickAction();
            }
        });
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
                                mFilePickerDelegate = new FilePickerDelegate(FileSourceFactory.createImageSource(FileSourceType.GALLERY));
                                doPick(mFilePickerDelegate);
                                break;

                            case 1:
                                mFilePickerDelegate = new FilePickerDelegate(FileSourceFactory.createImageSource(FileSourceType.CAMERA));
                                doPick(mFilePickerDelegate);
                                break;
                        }
                    }
                })
                .show();
    }

    private void doPick(FilePickerDelegate filePickerDelegate) {
        filePickerDelegate.pickFile(this)
                .subscribe(new Consumer<PickerResult>() {
                    @Override
                    public void accept(PickerResult pickerResult) throws Exception {
                        String path = pickerResult.getFilePath().toString();
                        Picasso.get().load(path).config(Bitmap.Config.RGB_565).fit().centerCrop().into(mBinding.ivResult);
                        mResultPath.set(pickerResult.getFilePath().toString());
                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
                        mResultPath.set(String.format("error [%s]", throwable.getCause()));
                        throwable.printStackTrace();
                    }
                });
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (mFilePickerDelegate != null) {
            mFilePickerDelegate.onActivityResult(requestCode, resultCode, data);
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (mFilePickerDelegate != null) {
            mFilePickerDelegate.onRequestPermissionsResult(requestCode, permissions, grantResults);
        }
    }

    public ObservableField<String> getResultPath() {
        return mResultPath;
    }
}
