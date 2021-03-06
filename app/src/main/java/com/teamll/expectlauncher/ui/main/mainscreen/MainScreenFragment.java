package com.teamll.expectlauncher.ui.main.mainscreen;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.widget.NestedScrollView;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.resource.drawable.DrawableTransitionOptions;
import com.bumptech.glide.request.RequestOptions;
import com.makeramen.roundedimageview.RoundedImageView;
import com.teamll.expectlauncher.ExpectLauncher;
import com.teamll.expectlauncher.R;
import com.teamll.expectlauncher.model.App;
import com.teamll.expectlauncher.model.Rectangle;
import com.teamll.expectlauncher.ui.main.AppLoaderActivity;
import com.teamll.expectlauncher.ui.main.LayoutSwitcher;
import com.teamll.expectlauncher.ui.main.MainActivity;
import com.teamll.expectlauncher.ui.main.bottomsheet.CommonSettingBottomSheet;
import com.teamll.expectlauncher.ui.widgets.MotionRoundedBitmapFrameLayout;
import com.teamll.expectlauncher.ui.widgets.itemtouchhelper.CustomItemTouchHelper;
import com.teamll.expectlauncher.util.PreferencesUtility;
import com.teamll.expectlauncher.util.Tool;
import com.teamll.expectlauncher.util.Util;

import java.util.ArrayList;

import butterknife.BindView;
import butterknife.ButterKnife;

public class MainScreenFragment extends AppWidgetHostFragment implements View.OnTouchListener, LayoutSwitcher.EventSender,AppLoaderActivity.AppsReceiver, CommonSettingBottomSheet.BottomSheetListener{
    private static final String TAG="MainScreenFragment";

    private long savedTime;
    private float xDown;
    private float yDown;
    private View adaptiveDock;
    private CustomItemTouchHelper mItemTouchHelper;

    @Override
    public boolean onTouch(View v, MotionEvent event) {
        Log.d(TAG, "onTouch: ");
        switch (event.getAction()) {
            case MotionEvent.ACTION_DOWN:
                adaptiveDock = v;
                Log.d(TAG, "onTouch: action down");
                savedTime = System.currentTimeMillis();
                xDown = event.getRawX();
                yDown = event.getRawY();
        }
        return false;
    }
    public static MainScreenFragment newInstance() {

        MainScreenFragment fragment = new MainScreenFragment();
        return fragment;
    }

    private View mRootView;
    private TextView appDrawerButton;
    @BindView(R.id.dock) public MotionRoundedBitmapFrameLayout dock;

    private MainActivity activity;
    float statusBarHeight  = 0;
    float navigationHeight = 0;
    float oneDp = 0;
    Rectangle rect;
    public FrameLayout.LayoutParams dockParams;
    RoundedImageView dockApp[] = new RoundedImageView[4];
    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "onCreate");
        activity = (MainActivity) getActivity();
        statusBarHeight= Tool.getStatusHeight(activity.getResources());
        navigationHeight = Tool.getNavigationHeight(activity);
        rect = new Rectangle();
        int[] size = Tool.getScreenSize(activity);
        rect.setSize(size[0],size[1]);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.main_screen_fragment,container,false);
    }
   @BindView(R.id.scrollview) NestedScrollView scrollView;

    @SuppressLint("ClickableViewAccessibility")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
    super.onViewCreated(view,savedInstanceState);
    ButterKnife.bind(this,view);
        Tool.getInstance().AddWallpaperChangedNotifier(dock);
        mRootView = view;
    widgetContainer.setOnLongClickListener(new View.OnLongClickListener() {
        @Override
        public boolean onLongClick(View v) {
            CommonSettingBottomSheet fragment =  CommonSettingBottomSheet.newInstance(LayoutSwitcher.MODE.IN_MAIN_SCREEN);
            fragment.setListener(MainScreenFragment.this);
            fragment.show(getActivity().getSupportFragmentManager(),
                    "song_popup_menu");

            return true;
        }
    });
    dockApp[0] =dock.findViewById(R.id.dockApp1);
    dockApp[1] =dock.findViewById(R.id.dockApp2);
    dockApp[2] =dock.findViewById(R.id.dockApp3);
    dockApp[3] =dock.findViewById(R.id.dockApp4);

    dock.setBackGroundColor(Color.WHITE);
    dock.setAlphaBackground(0.35f);
    dock.setRoundNumber(1.75f,true);
    dockParams = (FrameLayout.LayoutParams) dock.getLayoutParams();
    dockParams.topMargin = (int) (rect.Height - navigationHeight - dockParams.bottomMargin - dockParams.height);
    dockParams.bottomMargin = 0;
    dock.requestLayout();

        for (int i = 0; i < dockApp.length; i++) {
            View v = dockApp[i];
            if (v.getLayoutParams() instanceof LinearLayout.LayoutParams) {
                LinearLayout.LayoutParams p = (LinearLayout.LayoutParams) v.getLayoutParams();
                float dockWidth = Tool.getScreenSize(getContext())[0] - getResources().getDimension(R.dimen.dock_margin) * 2;
                float dockHeight = getResources().getDimension(R.dimen.dock_height);
                float appSize = dockHeight - p.topMargin - p.bottomMargin;
                float margin = (dockWidth - 4 * appSize) / 5f;
                Log.d(TAG, "onViewCreated: " + margin);
                p.setMarginStart((int) margin);
                if(i==dockApp.length-1)
                p.setMarginEnd((int) margin);
                v.requestLayout();
            }
            v.setOnTouchListener(this);
        }

    toggle = view.findViewById(R.id.toggleButton);
    toggleParams = (FrameLayout.LayoutParams) toggle.getLayoutParams();
    toggleParams.topMargin = dockParams.topMargin - toggleParams.height;
    toggle.requestLayout();

        FrameLayout.LayoutParams params = (FrameLayout.LayoutParams) scrollView.getLayoutParams();
        params.topMargin = (int) (statusBarHeight + params.topMargin);
        params.height =  toggleParams.topMargin - params.topMargin - params.bottomMargin;
        params.bottomMargin = 0;
        widgetContainer.setMinimumHeight(params.height);
        scrollView.requestLayout();

        AppLoaderActivity ac = (AppLoaderActivity)getActivity();
        if(ac!=null)
            ac.addAppsReceiver(this);
        else throw new NullPointerException("Fragment was created from an activity class that doesn't inherit from AppLoaderActivity class");
        bindDock();
    }
    private void dockClick(View v) {
        for (int i=0;i<appInDock.size();i++) {
            if (dockApp[i].getId() == v.getId()) {
                openApp(v, appInDock.get(i));
            }
        }
    }
   public FrameLayout.LayoutParams toggleParams ;
    public ImageView toggle;
    public void onUp(MotionEvent event) {
        if(System.currentTimeMillis() - savedTime <=300&& Math.sqrt(
                (xDown-event.getRawX())*(xDown - event.getRawX())+
                        (yDown-event.getRawY())*(yDown-event.getRawY()))<=100) {
           if(adaptiveDock!=null) {
               dockClick(adaptiveDock);
               adaptiveDock = null;
           }
        }
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

    }


    @Override
    public View getRoot() {
        return null;
    }

    private ArrayList<App> appInDock = new ArrayList<>();
    private String[] packageDock = {
            "com.android.dialer",
            "com.google.android.dialer",
            "com.android.messaging",
            "com.google.android.apps.messaging",
            "com.android.chrome",
            "com.android.browser",
            "com.google.android.music",
            "com.android.music",
            "com.android.contacts",
            "com.google.android.apps.photos",
            "com.android.settings",
            "com.android.camera2",
            "com.google.android.apps.docs",
            "com.android.documentsui",
            "com.google.android.videos",
            "com.google.android.apps.maps",
            "com.google.android.apps.photos",
            "org.chromium.webview_shell",
            "com.google.android.youtube",
            "com.google.android.googlequicksearchbox"};

    private boolean isPackageDock(App app) {
        for (String p :
                packageDock) {
            if(p.equals(app.getApplicationPackageName())) return true;
        }
        return false;
    }

    private void setDockAppForFirst() {
        App app;
        for (String packageName : packageDock) {
            app = Util.findApp(apps, packageName);

            if (app != null) {
                if (appInDock.size() < 4) {
                    appInDock.add(app);
                }
                else {
                    break;
                }
            }
        }
    }
    private ArrayList<App> apps = new ArrayList<>();
    @Override
    public void onLoadComplete(ArrayList<App> data) {
        String dockAppPackageNames[];
        apps.clear();
        apps.addAll(data);

        appInDock.clear();

        dockAppPackageNames = Util.getDockAppPackageNames(getActivity());
        if (dockAppPackageNames == null) {
//            for (App a :
//                    apps) {
//                if(isPackageDock(a)&&appInDock.size()<4) appInDock.add(a);
//            }
            setDockAppForFirst();

            dockAppPackageNames = new String[4];
            for (int index = 0; index < 4; index++) {
                dockAppPackageNames[index] = appInDock.get(index).getApplicationPackageName();
            }

            Util.saveDockAppPackageNames(getActivity(), dockAppPackageNames);
        }
        else {
            for (String packageName : dockAppPackageNames) {
                appInDock.add(Util.findApp(apps, packageName));
            }
        }

        for(int i=0;i<appInDock.size();i++) {
            RequestOptions requestOptions = new RequestOptions();
            Glide.with(getContext())
                    .load(appInDock.get(i).getIcon())
                    .apply(requestOptions)
                    .transition(DrawableTransitionOptions.withCrossFade(650))
                    .into(dockApp[i]);
           // dockApp[i].setImageDrawable(appInDock.get(i).getIcon());
        }
        bindDock();
    }
    void openApp(View v, App app) {
        ((AppLoaderActivity)getActivity()).openApp(v, app);
    }

    @Override
    public void onLoadReset() {

    }
    public void bindDock() {
        if(dockApp!=null&&dockApp.length==4&&appInDock!=null&&appInDock.size()>=4) {
            PreferencesUtility.IconEditorConfig iec = ExpectLauncher.getInstance().getPreferencesUtility().getIconConfig();
            float appSize = dockApp[0].getWidth();
            for (int i = 0; i < dockApp.length; i++) {
                switch (iec.getShapedType()) {
                    case 0:
                        dockApp[i].setBackgroundColor(0);
                        dockApp[i].setCornerRadius(0);
                        dockApp[i].setPadding(0, 0, 0, 0);
                        break;
                    case 1:
                        dockApp[i].setBackgroundColor(Color.WHITE);
                        //    Log.d(TAG, "bindAppIconType: "+iec.getCornerRadius());
                        dockApp[i].setCornerRadius(iec.getCornerRadius() * appSize);
                        int pd = (int) (appInDock.get(i).getAppSavedInstance().getPadding() * appSize);
                        dockApp[i].setPadding(pd, pd, pd, pd);
                        break;
                    case 2:
                        dockApp[i].setBackgroundColor(appInDock.get(i).getAppSavedInstance().getCustomBackground());
                        dockApp[i].setCornerRadius(iec.getCornerRadius() * appSize);
                        int pd2 = (int) (appInDock.get(i).getAppSavedInstance().getPadding() * appSize);
                        dockApp[i].setPadding(pd2, pd2, pd2, pd2);
                        break;
                }
                dockApp[i].requestLayout();

            }
        }
    }

    @Override
    public void onClickButtonInsideBottomSheet(View view) {
        switch (view.getId()) {
            case R.id.add_widget:selectWidget(); break;
            case R.id.wallpaper:
                //TODO: call replace wallpaper function;
                Intent wallpagerIntent = new Intent(Intent.ACTION_SET_WALLPAPER);
                startActivity(Intent.createChooser(wallpagerIntent, "Select Wallpaper"));
                break;
            case R.id.wall_editor:
                Log.d(TAG, "onClickButtonInsideBottomSheet: app_icon_editor");
                removeWidgetMenuSelected();
                break;
        }
    }

    public void setAlpha(float value) {
        Log.d(TAG, "setAlpha: "+value);
        widgetContainer.setAlpha(value);
        dock.setAlpha(value);
    }



    public void updateDock() {
        String dockAppPackageNames[] = Util.getDockAppPackageNames(getActivity());

        if (dockAppPackageNames != null) {
            appInDock.clear();
            for (String packageName : dockAppPackageNames) {
                appInDock.add(Util.findApp(apps, packageName));
            }

            for(int i=0;i<appInDock.size();i++) {
                dockApp[i].setImageDrawable(appInDock.get(i).getIcon());
            }
        }
    }
}
