package com.kingtous.remotefingerunlock;

import android.Manifest;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.Toast;
import com.google.android.material.navigation.NavigationView;
import com.kingtous.remotefingerunlock.FileTransferTool.FileTransferActivity;
import com.kingtous.remotefingerunlock.ToolFragment.AboutFragment;
import com.kingtous.remotefingerunlock.ToolFragment.DataManagementFragment;
import com.kingtous.remotefingerunlock.ToolFragment.ModeFragment;
import com.kingtous.remotefingerunlock.ToolFragment.ScanFragment;
import com.kingtous.remotefingerunlock.ToolFragment.UnlockFragment;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import java.util.List;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import pub.devrel.easypermissions.EasyPermissions;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;


    FragmentManager fragmentManager;
    ResideMenu menu;
    Fragment unlock, scan, dataManagement, about,mode;
    Fragment currentFragment;
    //request code
    int FINGER_REQUEST_CODE = 1;
    // 双击退出计时
    long firstTime=0;

    void initSideMenu() {
        String titles[] = {"解锁设备", "添加设备", "管理设备", "查看文件","连接模式","关于", "退出"};
        int icon[] = {R.drawable.back2, R.drawable.back2, R.drawable.back2, R.drawable.back2,R.drawable.back2,R.drawable.back2, R.drawable.back2};
        menu = new ResideMenu(this);
        menu.setBackground(R.drawable.background);
        menu.attachToActivity(this);
        for (int i = 0; i < titles.length; i++) {
            ResideMenuItem item = new ResideMenuItem(this, icon[i], titles[i]);
            final int finalI = i;
            item.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    switch (finalI) {
                        case 0:
                            //解锁
                            switchFragment(unlock)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 1:
                            // 搜索
                            switchFragment(scan)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 2:
                            // 退出登录，跳转到登录页面
                            switchFragment(dataManagement)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 3:
                            // 文件传输
                            startActivity(new Intent(MainActivity.this, FileTransferActivity.class));
                            switchFragment(dataManagement)
                                    .commit();
                            break;
                        case 4:
                            // 设置
//                            startActivity(new Intent(MainActivity.this, SettingActivity.class));
                            switchFragment(mode)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 5:
                            // 关于
                            switchFragment(about)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 6:
                            //退出
                            menu.closeMenu();
                            finish();
                            break;
                    }
                }
            });
            menu.addMenuItem(item, ResideMenu.DIRECTION_LEFT); // or  ResideMenu.DIRECTION_RIGHT
        }
        menu.setSwipeDirectionDisable(ResideMenu.DIRECTION_RIGHT);
    }

    @RequiresApi(api = Build.VERSION_CODES.LOLLIPOP)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        final DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        toolbar.setNavigationIcon(R.drawable.back2);
        toolbar.setNavigationOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                menu.openMenu(ResideMenu.DIRECTION_LEFT);
            }
        });
        initSideMenu();

//        drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
//        toggle = new ActionBarDrawerToggle(
//                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
//        drawer.addDrawerListener(toggle);
//        toggle.syncState();
//
//        navigationView = (NavigationView) findViewById(R.id.nav);
//        navigationView.setNavigationItemSelectedListener(this);

        checkPermission();

        //fragment
        fragmentManager = getSupportFragmentManager();
        unlock = new UnlockFragment();
        scan = new ScanFragment();
        mode = new ModeFragment();
//        settings=new SettingsFragment();
        dataManagement = new DataManagementFragment();
        about = new AboutFragment();
        switchFragment(unlock).commit();
        Window w = getWindow();
        w.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
        w.setStatusBarColor(getResources().getColor(R.color.deepskyblue));
    }


    private void checkPermission() {
        if (!EasyPermissions.hasPermissions(this, Manifest.permission.USE_FINGERPRINT)) {
            String[] permissions = new String[]{Manifest.permission.USE_FINGERPRINT, Manifest.permission.BLUETOOTH,
                    Manifest.permission.BLUETOOTH_ADMIN};
            EasyPermissions.requestPermissions(this, "需要申请指纹权限", FINGER_REQUEST_CODE, permissions);
        }
    }

    @Override
    public void onPermissionsGranted(int requestCode, @androidx.annotation.NonNull List<String> perms) {
        return;
    }

    @Override
    public void onPermissionsDenied(int requestCode, @androidx.annotation.NonNull List<String> perms) {

        new androidx.appcompat.app.AlertDialog.Builder(this)
                .setTitle("权限获取")
                .setMessage("权限获取失败，请允许指纹权限")
                .setPositiveButton("好", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                        finish();
                    }
                })
                .show();

    }


    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            long secondTime = System.currentTimeMillis();
            if (secondTime - firstTime > 2000) {
                Toast.makeText(MainActivity.this, "再按一次退出程序", Toast.LENGTH_SHORT).show();
                firstTime = secondTime;
            } else {
                finish();
            }
        }
    }

//    @Override
//    public boolean onCreateOptionsMenu(Menu menu) {
//        // Inflate the menu; this adds items to the action bar if it is present.
//        getMenuInflater().inflate(R.menu.main, menu);
//        return true;
//    }
//
//    @Override
//    public boolean onOptionsItemSelected(MenuItem item) {
//        // Handle action bar item clicks here. The action bar will
//        // automatically handle clicks on the Home/Up button, so long
//        // as you specify a parent activity in AndroidManifest.xml.
//        int id = item.getItemId();
//
//        //noinspection SimplifiableIfStatement
//        if (id == R.id.action_settings) {
//            return true;
//        }
//
//        return super.onOptionsItemSelected(item);
//    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();
        if (id == currentFragment.getId())
            return false;

        if (id == R.id.Unlock) {
            switchFragment(unlock)
                    .commit();
        } else if (id == R.id.Scan) {
            switchFragment(scan)
                    .commit();
        } else if (id == R.id.DataManagement) {
            switchFragment(dataManagement)
                    .commit();
        }
//        else if (id == R.id.Settings) {
//            switchFragment(settings)
//                    .commit();
//        } else if (id == R.id.Share) {
//
//        }
        else if (id == R.id.About) {
            switchFragment(about)
                    .commit();
        }
        return true;
    }

    private FragmentTransaction switchFragment(Fragment targetFragment) {
        FragmentTransaction transaction = getSupportFragmentManager().beginTransaction();

        transaction.setTransitionStyle(FragmentTransaction.TRANSIT_FRAGMENT_FADE);

        if (!targetFragment.isAdded()) {
            transaction.add(R.id.fragmentShow, targetFragment, targetFragment.getClass().getName());
        }
        transaction.replace(R.id.fragmentShow, targetFragment);
        currentFragment = targetFragment;
        return transaction;
    }

}
