package com.example.kyle.reminder;

import android.content.res.Configuration;
import android.os.Bundle;
import android.support.design.widget.NavigationView;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

public class MainActivity extends AppCompatActivity {

  private TextView mNavTitle;
  private DrawerLayout mDrawerLayout;
  private ActionBarDrawerToggle mDrawerToggle;
  private String mActivityTitle;
  private NavigationView mNavigationView;
  private View mNavHeader;
  private Toolbar mToolbar;
  private FragmentManager mFragmentManager;

  @Override
  public void onCreate(Bundle savedInstanceState) {

    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);
    mToolbar = findViewById(R.id.tool_bar);
    this.setSupportActionBar(mToolbar);

    if (getSupportActionBar() != null) {
      getSupportActionBar().setDisplayHomeAsUpEnabled(true);
    }

    mFragmentManager = getSupportFragmentManager();
    Bundle args = new Bundle();
    args.putSerializable(ReminderParams.TYPE, ReminderType.ALL);
    Fragment reminderFragment = new ReminderFragment();
    reminderFragment.setArguments(args);
    mFragmentManager.beginTransaction().add(R.id.content_frame, reminderFragment).commit();
    setupDrawer();
  }

  @Override
  protected void onPostCreate(Bundle savedInstanceState) {
    super.onPostCreate(savedInstanceState);
    mDrawerToggle.syncState();
  }

  @Override
  public void onConfigurationChanged(Configuration newConfig) {
    super.onConfigurationChanged(newConfig);
    mDrawerToggle.onConfigurationChanged(newConfig);
  }

  public boolean onOptionsItemSelected(MenuItem item) {
    return mDrawerToggle.onOptionsItemSelected(item) || super.onOptionsItemSelected(item);
  }

  private void setupDrawer() {
    mNavigationView = findViewById(R.id.navigation_view);
    mDrawerLayout = findViewById(R.id.drawer_layout);
    mActivityTitle = getTitle().toString();

    mNavHeader = mNavigationView.getHeaderView(0);
    mNavTitle = mNavHeader.findViewById(R.id.name);

    mNavTitle.setText(mActivityTitle);

    mNavigationView.setNavigationItemSelectedListener(new NavigationView.OnNavigationItemSelectedListener() {

      // This method will trigger on item Click of navigation menu
      @Override
      public boolean onNavigationItemSelected(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
          case R.id.nav_home:
            mDrawerLayout.closeDrawers();
            Toast.makeText(getApplicationContext(), R.string.show_all_toast,
                Toast.LENGTH_SHORT).show();
            reloadReminders(ReminderType.ALL);
            break;
          case R.id.nav_alerts:
            mDrawerLayout.closeDrawers();
            Toast.makeText(getApplicationContext(), R.string.show_alerts_toast,
                Toast.LENGTH_SHORT).show();
            reloadReminders(ReminderType.ALERT);
            break;
          case R.id.nav_notes:
            mDrawerLayout.closeDrawers();
            Toast.makeText(getApplicationContext(), R.string.show_notes_toast,
                Toast.LENGTH_SHORT).show();
            reloadReminders(ReminderType.NOTE);
            break;
          case R.id.nav_settings:
            mDrawerLayout.closeDrawers();
            Toast.makeText(getApplicationContext(), "Coming soon!", Toast.LENGTH_SHORT).show();
            break;
          default:
            break;
        }
        return true;
      }

      ;
    });
    mDrawerToggle = new ActionBarDrawerToggle(this, mDrawerLayout,
            R.string.drawer_open, R.string.drawer_close) {

      /** Called when a drawer has settled in a completely open state. */
      public void onDrawerOpened(View drawerView) {
        super.onDrawerOpened(drawerView);

      }

      /** Called when a drawer has settled in a completely closed state. */
      public void onDrawerClosed(View view) {
        super.onDrawerClosed(view);
      }

    };
    mDrawerToggle.setDrawerIndicatorEnabled(true);
    mDrawerLayout.addDrawerListener(mDrawerToggle);
  }

  public void reloadReminders(ReminderType type) {
    Bundle args = new Bundle();
    args.putSerializable(ReminderParams.TYPE, type);
    Fragment reminderFragment = new ReminderFragment();
    reminderFragment.setArguments(args);
    mFragmentManager.beginTransaction().replace(R.id.content_frame, reminderFragment).commit();
  }

}

