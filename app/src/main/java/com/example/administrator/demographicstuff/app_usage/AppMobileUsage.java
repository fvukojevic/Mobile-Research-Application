package com.example.administrator.demographicstuff.app_usage;

import android.Manifest;
import android.annotation.TargetApi;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.example.administrator.demographicstuff.R;

import java.util.concurrent.TimeUnit;

import butterknife.BindView;
import butterknife.ButterKnife;

public class AppMobileUsage extends AppCompatActivity {
    private RecyclerView list;
    private static Button mobile_btn,wifi_btn,combine_btn;
    private String android_id;
    private Handler handler = new Handler();
    private final int REQUEST_READ_PHONE_STATE_PERMISSION = 1231;
    @BindView(R.id.progressBarMobileUsage)
    ProgressBar progressBar;
    @BindView(R.id.topMenu)
    LinearLayout linearLayout;
    @BindView(R.id.list)
    RecyclerView recyclerView;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        android_id = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
        setContentView(R.layout.activity_app_mobile_usage);
        ButterKnife.bind(this);
        list = findViewById(R.id.list);
        showWifiUsage();
        showCombinedUsage();
        initialize();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case REQUEST_READ_PHONE_STATE_PERMISSION: {
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    initialize();
                }
            }

        }
    }

    /*
     * Prelazak na Wifi promet
     * Prilikom prelaska finish osigurava da kada kliknemo 'nazad' odlazimo u FirstPageActivity
     * a ne ponovno u mobilni
     */
    public void showWifiUsage()
    {
        wifi_btn = findViewById(R.id.wifi_btn);
        wifi_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Intent intent = new Intent(".AppWifiUsage");
                startActivity(intent);
                finish();
            }
        });
    }

    /*
     * Prelazak na ukupni promet
     * Prilikom prelaska finish osigurava da kada kliknemo 'nazad' odlazimo u FirstPageActivity
     * a ne ponovno u mobilni
     */
    public void showCombinedUsage()
    {
        combine_btn = findViewById(R.id.combined_btn);
        combine_btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                progressBar.setVisibility(View.VISIBLE);
                recyclerView.setVisibility(View.GONE);
                Intent intent = new Intent(".AppCombinedUsage");
                startActivity(intent);
                finish();
            }
        });
    }

    /*
     * Inicijalizacija i testiranje permisija
     */
    private void initialize() {
        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_PHONE_STATE)
                != PackageManager.PERMISSION_GRANTED) {
            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    Manifest.permission.READ_PHONE_STATE)) {
                Toast.makeText(getApplicationContext(), "Can't obtain permission", Toast.LENGTH_SHORT).show();
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.READ_PHONE_STATE},
                        REQUEST_READ_PHONE_STATE_PERMISSION);
            }
            return;
        }
        if (hasPermissionToReadNetworkHistory()) {
            initializeRecyclerViewProperties();
        }
    }

    /*
     * Inicijalizacija uz pomoć App usage adaptera
     * Tu se nalazi holder koji prikazuje podatke
     * Povezan je sa item layout-om
     */
    private void initializeRecyclerViewProperties() {
        list.setLayoutManager(new LinearLayoutManager(getApplicationContext()));
        final AppUsageAdapter adapter = new AppUsageAdapter(getApplicationContext(), android_id);
        adapter.usageList = new NetworkUsageHelper(getApplicationContext()).getMobileUsageList();
        list.setAdapter(adapter);
        handler.postDelayed(new Runnable() {
            @Override
            public void run() {
                adapter.usageList = new NetworkUsageHelper(getApplicationContext()).getMobileUsageList();
                adapter.notifyDataSetChanged();
                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(getApplicationContext(), "Data updated", Toast.LENGTH_SHORT).show();
                    }
                });
            }
        }, TimeUnit.MILLISECONDS.convert(5, TimeUnit.MINUTES));
    }


    private boolean hasPermissionToReadNetworkHistory() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        final AppOpsManager appOps = (AppOpsManager) getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());
        if (mode == AppOpsManager.MODE_ALLOWED) {
            return true;
        }
        appOps.startWatchingMode(AppOpsManager.OPSTR_GET_USAGE_STATS,
                getApplicationContext().getPackageName(),
                new AppOpsManager.OnOpChangedListener() {
                    @Override
                    @TargetApi(Build.VERSION_CODES.M)
                    public void onOpChanged(String op, String packageName) {
                        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                                android.os.Process.myUid(), getPackageName());
                        if (mode != AppOpsManager.MODE_ALLOWED) {
                            return;
                        }
                        appOps.stopWatchingMode(this);
                        Intent intent = new Intent(AppMobileUsage.this, AppMobileUsage.class);
                        if (getIntent().getExtras() != null) {
                            intent.putExtras(getIntent().getExtras());
                        }
                        intent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        getApplicationContext().startActivity(intent);
                    }
                });
        requestReadNetworkHistoryAccess();
        return false;
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    private void requestReadNetworkHistoryAccess() {
        Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
        startActivity(intent);
    }
}

