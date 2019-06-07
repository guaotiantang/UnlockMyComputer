package com.kingtous.remotefingerunlock;

import android.Manifest;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;

import com.gitonway.lee.niftymodaldialogeffects.lib.Effectstype;
import com.gitonway.lee.niftymodaldialogeffects.lib.NiftyDialogBuilder;
import com.google.android.material.navigation.NavigationView;
import com.kingtous.remotefingerunlock.FileTransferTool.FileTransferActivity;
import com.kingtous.remotefingerunlock.ToolFragment.AboutFragment;
import com.kingtous.remotefingerunlock.ToolFragment.DataManagementFragment;
import com.kingtous.remotefingerunlock.ToolFragment.ScanFragment;
import com.kingtous.remotefingerunlock.ToolFragment.UnlockFragment;
import com.special.ResideMenu.ResideMenu;
import com.special.ResideMenu.ResideMenuItem;

import androidx.annotation.RequiresApi;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.appcompat.app.ActionBarDrawerToggle;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import pub.devrel.easypermissions.EasyPermissions;

import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.util.List;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, EasyPermissions.PermissionCallbacks {

    Toolbar toolbar;
    DrawerLayout drawer;
    ActionBarDrawerToggle toggle;
    NavigationView navigationView;


    FragmentManager fragmentManager;

    Fragment unlock, scan, dataManagement, about;
    Fragment currentFragment;
    //request code
    int FINGER_REQUEST_CODE = 1;

    void initSideMenu() {
        String titles[] = {"解锁", "搜索设备", "数据管理", "文件传输","关于", "退出"};
        int icon[] = {R.drawable.back2, R.drawable.back2, R.drawable.back2, R.drawable.back2,R.drawable.back2, R.drawable.back2};
        final ResideMenu menu = new ResideMenu(this);
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
                            menu.closeMenu();
                            startActivity(new Intent(MainActivity.this, FileTransferActivity.class));
                            switchFragment(dataManagement)
                                    .commit();
                            break;
                        case 4:
                            // 关于
                            switchFragment(about)
                                    .commit();
                            menu.closeMenu();
                            break;
                        case 5:
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
        toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

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

        final NiftyDialogBuilder builder = NiftyDialogBuilder.getInstance(MainActivity.this);
        builder.withTitle("权限获取")
                .withEffect(Effectstype.Shake)
                .withMessage("权限获取失败，请允许指纹权限")
                .withButton1Text("好")
                .setButton1Click(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        finish();
                        builder.dismiss();
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
            super.onBackPressed();
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

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
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
