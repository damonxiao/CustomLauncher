
package com.seuic.launcher;

import android.app.Activity;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.View;
import android.view.View.OnClickListener;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RadioGroup.OnCheckedChangeListener;

import com.seuic.launcher.data.AppInfo;
import com.seuic.launcher.data.AppInfo.AppSize;
import com.seuic.launcher.util.AppHelper;
import com.seuic.launcher.util.Const;
import com.seuic.launcher.util.Logger;
import com.seuic.launcher.widget.ColorPickerDialog;

public class AppInfoEditor extends Activity implements OnClickListener{

    private AppInfo mAppInfo;

    private ImageView mIconColor;

    private EditText mLabelEdit;

    private RadioGroup mIconSizeGroup;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_info_editor);

        mIconColor = (ImageView) findViewById(R.id.icon_color);
        mLabelEdit = (EditText) findViewById(R.id.icon_name);
        mIconSizeGroup = (RadioGroup) findViewById(R.id.icon_size_group);
        mIconSizeGroup.setOnCheckedChangeListener(new OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                RadioButton rb = (RadioButton) findViewById(checkedId);
                String txt = rb.getText().toString();
                if(txt.equals(getString(R.string.icon_size_small))){
                    mAppInfo.setAppSize(AppSize.small);
                }
                else if(txt.equals(getString(R.string.icon_size_large))){
                    mAppInfo.setAppSize(AppSize.large);
                }
            }
        });
        String packageName = getIntent().getStringExtra(Const.EXTRA_PACKAGE_NAME);
        if(packageName == null){
            finish();
            return;
        }
        mAppInfo = AppHelper.loadAppInfo(packageName, this, getPackageManager());
        if(mAppInfo == null){
            finish();
            return;
        }
        mIconColor.setImageDrawable(mAppInfo.getIcon());
        mIconColor.setBackgroundColor(mAppInfo.getIconBgColor());
        mLabelEdit.setText(mAppInfo.getTitle());
        mLabelEdit.setSelection(mLabelEdit.length());
        mLabelEdit.addTextChangedListener(new TextWatcher() {
            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                if(s != null && s.length() > 0){
                    mAppInfo.setTitle(s);
                }
            }
            
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
                
            }
            
            @Override
            public void afterTextChanged(Editable s) {
                
            }
        });
        AppSize size = mAppInfo.getAppSize();
        if(size == AppSize.small){
            ((RadioButton)(mIconSizeGroup.getChildAt(0))).setChecked(true);
        }else {
            ((RadioButton)(mIconSizeGroup.getChildAt(1))).setChecked(true);
        }
        findViewById(R.id.cancel_btn).setOnClickListener(this);
        findViewById(R.id.save_btn).setOnClickListener(this);
        mIconColor.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        switch (v.getId()) {
            case R.id.cancel_btn:
                onBackPressed();
                break;
            case R.id.save_btn:
                AppHelper.saveAppLiteInfo(mAppInfo);
                AppHelper.loadDefinedApp();
                onBackPressed();
                break;
            case R.id.icon_color:
                ColorPickerDialog colorPicker = new ColorPickerDialog(this, "fdsaf",
                        new ColorPickerDialog.OnColorChangedListener() {

                            @Override
                            public void colorChanged(int color) {
                                Logger.d("AppInfoEditor", "colorChanged()[color=" + color + "]");
                                mIconColor.setBackgroundColor(color);
                                mAppInfo.setIconBgColor(color);
                            }
                        });
                colorPicker.show();
                break;
            default:
                break;
        }
    }
}
