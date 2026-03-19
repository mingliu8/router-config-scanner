package com.example.oltmanager;

import android.os.AsyncTask;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity implements OntAdapter.OnRegisterClickListener {
    private EditText etIp, etPort, etUsername, etPassword;
    private Button btnConnect, btnRefresh;
    private RecyclerView recyclerOnTs;
    private OltManager oltManager;
    private OntAdapter ontAdapter;
    private List<Ont> ontList;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 初始化UI组件
        etIp = findViewById(R.id.et_ip);
        etPort = findViewById(R.id.et_port);
        etUsername = findViewById(R.id.et_username);
        etPassword = findViewById(R.id.et_password);
        btnConnect = findViewById(R.id.btn_connect);
        btnRefresh = findViewById(R.id.btn_refresh);
        recyclerOnTs = findViewById(R.id.recycler_onts);

        // 设置RecyclerView
        recyclerOnTs.setLayoutManager(new LinearLayoutManager(this));

        // 设置按钮点击事件
        btnConnect.setOnClickListener(v -> connectToOlt());
        btnRefresh.setOnClickListener(v -> refreshOnTs());
    }

    private void connectToOlt() {
        String ip = etIp.getText().toString().trim();
        String portStr = etPort.getText().toString().trim();
        String username = etUsername.getText().toString().trim();
        String password = etPassword.getText().toString().trim();

        if (ip.isEmpty() || portStr.isEmpty()) {
            Toast.makeText(this, "请输入IP地址和端口", Toast.LENGTH_SHORT).show();
            return;
        }

        int port = Integer.parseInt(portStr);

        // 创建OLT管理器
        oltManager = new OltManager(ip, port, username, password);

        // 异步连接OLT
        new AsyncTask<Void, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Void... voids) {
                return oltManager.connect();
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, R.string.message_connect_success, Toast.LENGTH_SHORT).show();
                    btnConnect.setText(R.string.disconnect);
                    // 连接成功后刷新光猫列表
                    refreshOnTs();
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_connect_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    private void refreshOnTs() {
        if (oltManager == null) {
            Toast.makeText(this, "请先连接OLT", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步获取未注册光猫列表
        new AsyncTask<Void, Void, List<Ont>>() {
            @Override
            protected List<Ont> doInBackground(Void... voids) {
                return oltManager.getUnregisteredOnTs();
            }

            @Override
            protected void onPostExecute(List<Ont> onts) {
                if (onts != null && !onts.isEmpty()) {
                    ontList = onts;
                    ontAdapter = new OntAdapter(ontList, MainActivity.this);
                    recyclerOnTs.setAdapter(ontAdapter);
                    Toast.makeText(MainActivity.this, R.string.message_refresh_success, Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_refresh_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute();
    }

    @Override
    public void onRegisterClick(Ont ont) {
        if (oltManager == null) {
            Toast.makeText(this, "请先连接OLT", Toast.LENGTH_SHORT).show();
            return;
        }

        // 异步注册光猫
        new AsyncTask<Ont, Void, Boolean>() {
            @Override
            protected Boolean doInBackground(Ont... onts) {
                return oltManager.registerOnt(onts[0]);
            }

            @Override
            protected void onPostExecute(Boolean success) {
                if (success) {
                    Toast.makeText(MainActivity.this, R.string.message_register_success, Toast.LENGTH_SHORT).show();
                    // 注册成功后刷新光猫列表
                    refreshOnTs();
                } else {
                    Toast.makeText(MainActivity.this, R.string.message_register_failed, Toast.LENGTH_SHORT).show();
                }
            }
        }.execute(ont);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (oltManager != null) {
            oltManager.disconnect();
        }
    }
}