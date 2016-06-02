package com.vaporwarecorp.mirror.feature.main;

import android.app.*;
import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.IdRes;
import android.support.annotation.NonNull;
import android.view.View;
import android.view.WindowManager;
import com.robopupu.api.feature.FeatureContainer;
import com.robopupu.api.feature.FeatureView;
import com.robopupu.api.mvp.PluginActivity;
import com.robopupu.api.plugin.Plug;
import com.robopupu.api.plugin.Plugin;
import com.vaporwarecorp.mirror.R;
import com.vaporwarecorp.mirror.app.MirrorAppScope;
import com.vaporwarecorp.mirror.component.AppManager;
import com.vaporwarecorp.mirror.component.PluginFeatureManager;
import com.vaporwarecorp.mirror.component.dottedgrid.DottedGridView;
import com.vaporwarecorp.mirror.component.forecast.ForecastView;
import com.vaporwarecorp.mirror.component.forecast.model.Forecast;
import com.vaporwarecorp.mirror.feature.MainFeature;
import com.vaporwarecorp.mirror.feature.MainScope;
import com.vaporwarecorp.mirror.feature.common.view.MirrorView;
import com.vaporwarecorp.mirror.util.FullScreenActivityUtil;

@Plugin
public class MirrorActivity extends PluginActivity<MainPresenter> implements MainView {
// ------------------------------ FIELDS ------------------------------

    @Plug
    AppManager mAppManager;
    @Plug(MirrorAppScope.class)
    MainFeature mFeature;
    @Plug
    PluginFeatureManager mFeatureManager;
    @Plug(MainScope.class)
    MainPresenter mPresenter;

    private DottedGridView mContentContainer;
    private ForecastView mForecastView;
    private View mFullscreenContainer;
    private View mHeaderContainer;

// ------------------------ INTERFACE METHODS ------------------------


// --------------------- Interface FeatureContainer ---------------------

    @Override
    @IdRes
    public int getContainerViewId() {
        return R.id.fragment_container;
    }

// --------------------- Interface FeatureTransitionManager ---------------------

    @Override
    public void showView(final FeatureView featureView, final boolean addToBackStack, final String fragmentTag) {
        String tag = fragmentTag;
        if (fragmentTag == null) {
            tag = featureView.getViewTag();
        }

        final FragmentManager manager = getFragmentManager();
        if (manager.findFragmentByTag(tag) == null) {
            if (featureView instanceof DialogFragment) {
                showDialogFragment(manager, (DialogFragment) featureView, addToBackStack, tag);
            } else if (featureView instanceof MirrorView) {
                showMirrorView(manager, (MirrorView) featureView, addToBackStack, tag);
            } else if (featureView instanceof Fragment) {
                showFragment(manager, (Fragment) featureView, addToBackStack, tag);
            }
        }
    }

    @Override
    public void removeView(final FeatureView featureView, final boolean addedToBackStack, final String fragmentTag) {
        if (featureView instanceof DialogFragment) {
            hideDialogFragment((DialogFragment) featureView);
        } else if (featureView instanceof MirrorView) {
            String tag = (fragmentTag != null) ? fragmentTag : featureView.getViewTag();
            hideMirrorView((MirrorView) featureView, addedToBackStack, tag);
        } else if (featureView instanceof Fragment) {
            String tag = (fragmentTag != null) ? fragmentTag : featureView.getViewTag();
            hideFragment((Fragment) featureView, true, addedToBackStack, tag);
        }
    }

// --------------------- Interface MainView ---------------------

    @Override
    public Activity activity() {
        return this;
    }

    @Override
    public void displayView() {
        mHeaderContainer.setVisibility(View.VISIBLE);
        mContentContainer.setVisibility(View.VISIBLE);
        mFullscreenContainer.setVisibility(View.GONE);
    }

    @Override
    public FeatureContainer getMainFeatureContainer() {
        return this;
    }

    @Override
    public void hideView() {
        mHeaderContainer.setVisibility(View.INVISIBLE);
        mContentContainer.setVisibility(View.INVISIBLE);
    }

    @Override
    public void setForecast(Forecast forecast) {
        mForecastView.setForecast(forecast);
    }

// --------------------- Interface PresentedView ---------------------

    @Override
    public MainPresenter getPresenter() {
        return mPresenter;
    }

// -------------------------- OTHER METHODS --------------------------

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        mPresenter.verifyPermissions();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        mPresenter.onViewResult(requestCode, resultCode, data);
    }

    @Override
    protected void onCreate(final Bundle inState) {
        super.onCreate(inState);
        setContentView(R.layout.activity_mirror);

        mHeaderContainer = findViewById(R.id.header_container);
        mContentContainer = (DottedGridView) findViewById(R.id.content_container);
        mFullscreenContainer = findViewById(R.id.fullscreen_container);
        mForecastView = (ForecastView) findViewById(R.id.forecast_view);
        findViewById(R.id.test_button).setOnClickListener(v -> mPresenter.startSpotify());
        findViewById(R.id.test_another_button).setOnClickListener(v -> mPresenter.startSpotify2());
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        mAppManager.refWatcher().watch(this);
    }

    @Override
    protected void onResume() {
        super.onResume();
        onResumeFullScreen();
    }

    @Override
    protected void onStart() {
        super.onStart();

        if (!mFeature.isStarted()) {
            mFeatureManager.startFeature(this, mFeature);
        }
    }

    private void hideDialogFragment(final DialogFragment dialogFragment) {
        dialogFragment.dismiss();
    }

    private void hideFragment(final Fragment fragment,
                              final boolean removeParentView,
                              final boolean addedToBackStack,
                              final String tag) {
        final FragmentManager manager = getFragmentManager();
        if (manager.findFragmentByTag(tag) != null) {
            Integer viewId = null;
            if (fragment.getView() != null && fragment.getView().getParent() != null) {
                viewId = ((View) fragment.getView().getParent()).getId();
            }

            final FragmentTransaction transaction = manager.beginTransaction();
            transaction.remove(fragment);
            transaction.commit();

            if (addedToBackStack) {
                manager.popBackStack();
            }

            if (removeParentView && viewId != null) {
                mContentContainer.removeBorderView(viewId);
            }
        }
    }

    private void hideMirrorView(final MirrorView mirrorView, final boolean addedToBackStack, final String tag) {
        hideFragment((Fragment) mirrorView, !mirrorView.isFullscreen(), addedToBackStack, tag);
    }

    private void onResumeFullScreen() {
        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);
        FullScreenActivityUtil.onResume(this);
    }

    private void showDialogFragment(final FragmentManager fragmentManager,
                                    final DialogFragment dialogFragment,
                                    final boolean addToBackStack,
                                    final String tag) {
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.add(dialogFragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commitAllowingStateLoss();
    }

    private void showFragment(final FragmentManager fragmentManager,
                              final Fragment fragment,
                              final boolean addToBackStack,
                              final String tag) {
        mFullscreenContainer.setVisibility(View.GONE);

        final int viewId = mContentContainer.addBorderView(this);
        final FragmentTransaction transaction = fragmentManager.beginTransaction();
        transaction.replace(viewId, fragment, tag);
        if (addToBackStack) {
            transaction.addToBackStack(tag);
        }
        transaction.commitAllowingStateLoss();
    }

    private void showMirrorView(final FragmentManager fragmentManager,
                                final MirrorView mirrorView,
                                final boolean addToBackStack,
                                final String tag) {
        if (mirrorView.isFullscreen()) {
            mFullscreenContainer.setVisibility(View.VISIBLE);

            final FragmentTransaction transaction = fragmentManager.beginTransaction();
            transaction.replace(R.id.fullscreen_container, (Fragment) mirrorView, tag);
            if (addToBackStack) {
                transaction.addToBackStack(tag);
            }
            transaction.commitAllowingStateLoss();
        } else {
            showFragment(fragmentManager, (Fragment) mirrorView, addToBackStack, tag);
        }
    }
}
