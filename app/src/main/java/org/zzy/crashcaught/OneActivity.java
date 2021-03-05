package org.zzy.crashcaught;

import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

/**
 * ================================================
 * 作    者：ZhouZhengyi
 * 创建日期：2021/3/5 9:36
 * 描    述：
 * 修订历史：
 * ================================================
 */
public class OneActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_one);
        int a = 2/0;
    }
}
