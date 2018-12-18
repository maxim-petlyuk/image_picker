package io.picker.example;

import android.content.DialogInterface;
import android.content.Intent;
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

import java.util.Arrays;

import io.reactivex.functions.Consumer;

public class MainActivity extends AppCompatActivity {

    private FilePickerDelegate mFilePickerDelegate;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_main);

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

                    }
                }, new Consumer<Throwable>() {
                    @Override
                    public void accept(Throwable throwable) throws Exception {
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
}
