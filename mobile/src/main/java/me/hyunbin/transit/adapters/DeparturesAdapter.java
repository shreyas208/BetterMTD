package me.hyunbin.transit.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.NumberPicker;
import android.widget.RelativeLayout;
import android.widget.TextView;

import java.util.List;

import me.hyunbin.transit.NotificationService;
import me.hyunbin.transit.R;
import me.hyunbin.transit.activities.RouteActivity;
import me.hyunbin.transit.models.Departure;

/**
 * Created by Hyunbin on 7/3/2015.
 */
public class DeparturesAdapter extends RecyclerView.Adapter<DeparturesAdapter.ListItemViewHolder>{

    private static final String TAG = DeparturesAdapter.class.getSimpleName();
    private static final String ARG_TRIPID = "trip_id";
    private static final String ARG_HEADSIGN = "headsign";

    private List<Departure> mData;
    private String mCurrentStopId;
    private final String mCurrentStopName;
    private RecyclerView mParentRecyclerView;
    private String[] displayedValues;

    public DeparturesAdapter(List<Departure> data, String currentStopId, String currentStopName){
        if(data == null){
            throw new IllegalArgumentException("Adapter data must not be null");
        }
        this.mData = data;
        this.mCurrentStopId = currentStopId;
        this.mCurrentStopName = currentStopName;
        setHasStableIds(true);
        displayedValues = new String[15];
        for (int i = 1; i <= 15; i++) {
            displayedValues[i-1] = i + " min";
        }
    }

    public void swapData(List<Departure> data){
        mData = data;
    }

    @Override
    public long getItemId(int position){
        return mData.get(position).getUniqueId();
    }

    @Override
    public void onAttachedToRecyclerView(RecyclerView view){
        mParentRecyclerView = view;
    }

    @Override
    public ListItemViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_stop, parent, false);
        return new ListItemViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(ListItemViewHolder holder, int position) {
        final Departure departure = mData.get(position);

        holder.mHeadSignTextView.setText(departure.getHeadsign());
        holder.mExpectedMinsTextView.setText(departure.getExpectedMins() + "");

        String headSignFrag = "";
        if(departure.getTrip() != null)
            headSignFrag = "To " + departure.getTrip().getTripHeadsign();
        holder.mSubTextView.setText(headSignFrag);

        final String routeColor = departure.getRoute().getRouteColor();
        String routeTextColor = departure.getRoute().getRouteTextColor();
        // Special adjustment for ugly red
        if(routeColor.equals("ff0000") || routeColor.equals("ed1c24")){
            holder.mListItem.setBackgroundColor(Color.parseColor("#" + routeColor) - 0xD2000000);
            routeTextColor = "ffffff";
        }
        final String sRouteTextColor = routeTextColor;

        holder.mListItem.setBackgroundColor(Color.parseColor("#" + routeColor) - 0x48000000);
        holder.mHeadSignTextView.setTextColor(Color.parseColor("#" + routeTextColor));
        holder.mExpectedMinsTextView.setTextColor(Color.parseColor("#" + routeTextColor));
        holder.mSubTextView.setTextColor(Color.parseColor("#" + routeTextColor) - 0x5F000000);
        holder.mMinsTextView.setTextColor(Color.parseColor("#" + routeTextColor) - 0x5F000000);

        if(departure.isIsIstop() == true){
            if(routeTextColor.equals("ffffff"))
                holder.mIStopImageView.setImageResource(R.drawable.ic_istop_light);
            else
                holder.mIStopImageView.setImageResource(R.drawable.ic_istop_dark);
            holder.mIStopImageView.setScaleType(ImageView.ScaleType.FIT_XY);
        }
        else {
            holder.mIStopImageView.setVisibility(View.GONE);
        }

        holder.mListItem.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if(departure.getTrip() != null){
                    Intent intent = new Intent(v.getContext(), RouteActivity.class);
                    intent.putExtra(ARG_TRIPID, departure.getTrip().getTripId());
                    intent.putExtra(ARG_HEADSIGN, departure.getHeadsign());
                    intent.putExtra("current_stop", mCurrentStopId);
                    intent.putExtra("route_color", routeColor);
                    intent.putExtra("text_color", sRouteTextColor);
                    v.getContext().startActivity(intent);

                }
                else{
                    // Dismisses the Snackbar being shown, if any, and displays the new one
                    Snackbar.make(mParentRecyclerView, "This bus has no scheduled information",
                            Snackbar.LENGTH_SHORT).show();
                }
            }
        });

        holder.mListItem.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(final View v) {
                LayoutInflater inflater = (LayoutInflater) v.getContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
                View numPickView = inflater.inflate(R.layout.dialog_time_picker, null);
                final NumberPicker numberPicker = (NumberPicker) numPickView.findViewById(R.id.number_picker);
                numberPicker.setMinValue(1);
                numberPicker.setMaxValue(15);
                numberPicker.setValue(5);
                if(departure.getExpectedMins() < 5) {
                    numberPicker.setValue(2);
                }
                numberPicker.setDisplayedValues(displayedValues);
                numberPicker.setWrapSelectorWheel(false);
                AlertDialog.Builder builder = new AlertDialog.Builder(v.getContext());
                builder.setTitle("Set incoming bus alarm")
                        .setMessage("At how many minutes before arrival should the alarm ring?")
                        .setView(numPickView)
                        .setPositiveButton("OK", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog, int which) {
                                Intent intent = new Intent(v.getContext(), NotificationService.class);
                                intent.putExtra("current_stop", mCurrentStopId);
                                intent.putExtra("stop_name", mCurrentStopName);
                                intent.putExtra("unique_id", departure.getUniqueId());
                                intent.putExtra("alarm_time", numberPicker.getValue());
                                v.getContext().startService(intent);
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
                return true;
            }
        });
    }

    @Override
    public int getItemCount() {
        return mData.size();
    }

    public final static class ListItemViewHolder extends RecyclerView.ViewHolder {
        private TextView mHeadSignTextView;
        private TextView mExpectedMinsTextView;
        private TextView mSubTextView;
        private TextView mMinsTextView;
        private RelativeLayout mListItem;
        private ImageView mIStopImageView;

        public ListItemViewHolder(View itemView) {
            super(itemView);
            mListItem = (RelativeLayout) itemView.findViewById(R.id.listitem);
            mHeadSignTextView = (TextView) itemView.findViewById(R.id.headsign);
            mExpectedMinsTextView = (TextView) itemView.findViewById(R.id.expectedmins);
            mSubTextView = (TextView) itemView.findViewById(R.id.subtext);
            mMinsTextView = (TextView) itemView.findViewById(R.id.minslabel);
            mIStopImageView = (ImageView) itemView.findViewById(R.id.iStopView);
        }
    }
}
