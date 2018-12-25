package io.picker.example;

import android.content.DialogInterface;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.databinding.ObservableField;
import android.graphics.Bitmap;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.picker.file.PickerResult;
import com.picker.file.RxFilePicker;
import com.picker.file.factory.FileSourceType;
import com.squareup.picasso.Picasso;

import java.util.Arrays;

import io.picker.example.databinding.FragmentPickerBinding;
import io.reactivex.disposables.CompositeDisposable;
import io.reactivex.functions.Consumer;

public class PickerFragmentActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_fragment_picker);

        if (savedInstanceState == null) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .replace(R.id.frameContent, PickerFragment.newInstance())
                    .commitAllowingStateLoss();
        }
    }

    public static class PickerFragment extends Fragment {

        private final String TAG = "RxFragmentPicker";
        private RxFilePicker mRxFilePicker = new RxFilePicker();
        private CompositeDisposable mCompositeDisposable;
        private ObservableField<String> mResultPath = new ObservableField<>("");
        private FragmentPickerBinding mBinding;

        public static PickerFragment newInstance() {
            PickerFragment fragment = new PickerFragment();

            Bundle args = new Bundle();
            fragment.setArguments(args);

            return fragment;
        }

        @Nullable
        @Override
        public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
            return inflater.inflate(R.layout.fragment_picker, container, false);
        }

        @Override
        public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
            super.onViewCreated(view, savedInstanceState);

            Log.d(TAG, "onViewCreated");

            view.findViewById(R.id.btnPick).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    showPickAction();
                }
            });

            view.findViewById(R.id.btnNewAct).setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(getContext(), EmptyActivity.class));
                }
            });

            mBinding = DataBindingUtil.bind(view);
            mBinding.setHandler(this);
        }

        @Override
        public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
            super.onViewStateRestored(savedInstanceState);

            Log.d(TAG, "onViewStateRestored");

            if (savedInstanceState != null) {
                mRxFilePicker.onRestoreInstanceState(savedInstanceState);
            }
        }

        @Override
        public void onResume() {
            super.onResume();

            Log.d(TAG, "onResume");

            mCompositeDisposable = new CompositeDisposable();
            mCompositeDisposable.add(mRxFilePicker.getPickerFileReady()
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
        public void onPause() {
            super.onPause();

            Log.d(TAG, "onPause");

            mCompositeDisposable.dispose();
        }

        @Override
        public void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);

            Log.d(TAG, "onSaveInstanceState");

            mRxFilePicker.onSaveInstanceState(outState);
        }

        @Override
        public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
            super.onActivityResult(requestCode, resultCode, data);

            Log.d(TAG, "onActivityResult");

            mRxFilePicker.onActivityResult(requestCode, resultCode, data);
        }

        @Override
        public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
            super.onRequestPermissionsResult(requestCode, permissions, grantResults);

            Log.d(TAG, "onRequestPermissionsResult");

            mRxFilePicker.onRequestPermissionsResult(this, requestCode, permissions, grantResults);
        }

        @Override
        public void onDestroy() {
            super.onDestroy();

            Log.d(TAG, "onDestroy");
        }

        private void showPickAction() {
            ArrayAdapter<String> arrayAdapter = new ArrayAdapter<>(getContext(), android.R.layout.simple_spinner_dropdown_item, Arrays.asList("Gallery", "Camera"));
            new AlertDialog.Builder(getContext())
                    .setTitle("Pick")
                    .setAdapter(arrayAdapter, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialog, int which) {
                            dialog.dismiss();
                            switch (which) {
                                case 0:
                                    mRxFilePicker.fromSource(FileSourceType.GALLERY).pickFile(PickerFragment.this);
                                    break;

                                case 1:
                                    mRxFilePicker.fromSource(FileSourceType.CAMERA).pickFile(PickerFragment.this);
                                    break;
                            }
                        }
                    })
                    .show();
        }

        public ObservableField<String> getResultPath() {
            return mResultPath;
        }
    }
}
