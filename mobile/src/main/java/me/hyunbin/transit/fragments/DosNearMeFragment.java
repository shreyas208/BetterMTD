package me.hyunbin.transit.fragments;

import android.app.Fragment;
import android.content.SharedPreferences;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.view.animation.FastOutSlowInInterpolator;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.StaggeredGridLayoutManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

import me.hyunbin.transit.ApiClient;
import me.hyunbin.transit.R;
import me.hyunbin.transit.SpacesItemDecoration;
import me.hyunbin.transit.adapters.NearMeAdapter;
import me.hyunbin.transit.helpers.LayoutUtil;
import me.hyunbin.transit.models.Stop;
import me.hyunbin.transit.models.StopsByLatLonResponse;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

/**
 * This fragment contains both the favorites and near me stops listed out as chips, placed at the
 * bottom of DosMainActivity.
 */

public class DosNearMeFragment extends Fragment {
  public interface Listener {
    void onViewCollapsed(boolean isCollapsed);
  }

  private static final String TAG = DosNearMeFragment.class.getSimpleName();
  private static final String PREFERENCE_KEY = "collapse_near_me_fragment";

  private static final int ANIMATION_DURATION_MS = 400;
  private static final int NEAR_ME_SPAN_COUNT = 1;

  private NearMeAdapter mNearMeAdapter;
  private LinearLayout mBottomSheet;
  private RecyclerView mNearMeList;
  private TextView mNearMeLabel;
  private TextView mToggle;

  private Listener mListener;
  private SharedPreferences mSharedPreferences;

  private boolean mIsCollapsed;

  @Override
  public View onCreateView(
      LayoutInflater inflater,
      ViewGroup container,
      Bundle savedInstanceState) {
    View v = inflater.inflate(R.layout.fragment_dos_near_me, container, false);

    mBottomSheet = (LinearLayout) v.findViewById(R.id.bottom_sheet);
    mNearMeList = (RecyclerView) v.findViewById(R.id.near_me_list);
    setupRecyclerView(mNearMeList);

    mNearMeLabel = (TextView) v.findViewById(R.id.near_me_label);

    mToggle = (TextView) v.findViewById(R.id.toggle_text);
    mSharedPreferences = getActivity().getSharedPreferences(PREFERENCE_KEY, 0);

    mIsCollapsed = mSharedPreferences.getBoolean(PREFERENCE_KEY, false);
    if (mIsCollapsed) {
      mBottomSheet.setTranslationY(LayoutUtil.dpToPx(80) + 2);
      mToggle.setText("\u25B2");
    }

    return v;
  }

  @Override
  public void onViewCreated(final View view, Bundle savedInstanceState) {
    mToggle.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View innerView) {
        if (mIsCollapsed) {
          view.animate()
                .setInterpolator(new FastOutSlowInInterpolator())
                .translationYBy(-DosNearMeFragment.this.getView().getHeight()
                    //+ LayoutUtil.dpToPx(16)
                    + mToggle.getHeight());
          mToggle.setText("\u25BC");
        } else {
          view.animate()
              .setInterpolator(new FastOutSlowInInterpolator())
              .translationYBy(DosNearMeFragment.this.getView().getHeight()
                  //- LayoutUtil.dpToPx(16)
                  - mToggle.getHeight());
          mToggle.setText("\u25B2");
        }
        mIsCollapsed = !mIsCollapsed;

        if (mListener != null) {
          mListener.onViewCollapsed(mIsCollapsed);
        }
      }
    });
  }

  @Override
  public void onStop() {
    super.onStop();
    mSharedPreferences.edit().putBoolean(PREFERENCE_KEY, mIsCollapsed).apply();
  }

  public boolean getIsCollapsed() {
    return mIsCollapsed;
  }

  public void setListener(Listener listener) {
    mListener = listener;
  }

  public void onStopsByLatLonResponse(StopsByLatLonResponse response) {
    List<Stop> stopList = response.getStops();
    updateNearMeData(stopList);
  }

  public void onStopsByLatLonResponseFailure(String errorMsg) {
    // TODO: error handling here
  }

  private void setupRecyclerView(RecyclerView view) {
    view.getItemAnimator().setAddDuration(ANIMATION_DURATION_MS);
    view.getItemAnimator().setRemoveDuration(ANIMATION_DURATION_MS);

    StaggeredGridLayoutManager layoutManager;
    view.addItemDecoration(new SpacesItemDecoration(2, NEAR_ME_SPAN_COUNT));
    layoutManager = new StaggeredGridLayoutManager(
        NEAR_ME_SPAN_COUNT,
        StaggeredGridLayoutManager.HORIZONTAL);

    view.setLayoutManager(layoutManager);
  }

  private void updateNearMeData(List<Stop> data) {
    if (mNearMeAdapter == null) {
      mNearMeAdapter = new NearMeAdapter(data, R.layout.item_dos_near_me);
      mNearMeList.setAdapter(mNearMeAdapter);
      mNearMeAdapter.notifyItemRangeInserted(0, data.size() - 1);
    } else {
      mNearMeAdapter.swapData(data);
      mNearMeAdapter.notifyDataSetChanged();
    }
  }
}
