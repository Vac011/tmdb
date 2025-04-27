package edu.whu.tmdb;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Build;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import net.sf.jsqlparser.JSQLParserException;
import net.sf.jsqlparser.parser.CCJSqlParserUtil;
import net.sf.jsqlparser.statement.Statement;

import java.io.ByteArrayInputStream;
import java.io.IOException;

import au.edu.rmit.bdm.Test;
import edu.whu.tmdb.Main;
import edu.whu.tmdb.R;
import edu.whu.tmdb.query.Transaction;
import edu.whu.tmdb.query.operations.utils.SelectResult;
import edu.whu.tmdb.util.DbOperation;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_CODE = 1;
    private EditText etCmd;
    private TextView tvResult;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);//界面文件
        etCmd = findViewById(R.id.etCmd);
        tvResult = findViewById(R.id.tvResult);

        // 检查并请求存储权限
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.WRITE_EXTERNAL_STORAGE) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
            }
        }

        findViewById(R.id.btExecute).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // 清除之前的结果
                tvResult.setText("");
                String s = Main.function1(etCmd.getText().toString());
                tvResult.append(s+"\n");
            }
        });

        findViewById(R.id.btClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                etCmd.setText("");
            }
        });
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_CODE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // 权限授予
                Log.d("Permissions", "Write external storage permission granted.");
            } else {
                // 权限被拒绝
                tvResult.setText("Permission denied. Cannot reset database.");
                Log.d("Permissions", "Write external storage permission denied.");
            }
        }
    }
}