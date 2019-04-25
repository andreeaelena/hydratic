package com.hydratic.app.adapter;

import android.content.Context;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.firebase.ui.database.FirebaseRecyclerAdapter;
import com.firebase.ui.database.FirebaseRecyclerOptions;
import com.hydratic.app.R;
import com.hydratic.app.model.DrinkLog;
import com.hydratic.app.model.User;
import com.hydratic.app.storage.MemoryStore;
import com.hydratic.app.util.Utils;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import static com.hydratic.app.util.Constants.IMPERIAL_UNITS;
import static com.hydratic.app.util.Constants.IMPERIAL_VOLUME_UNIT;
import static com.hydratic.app.util.Constants.METRIC_VOLUME_UNIT;

public class LogHistoryAdapter extends FirebaseRecyclerAdapter<DrinkLog, RecyclerView.ViewHolder> {

    private static final String DRINK_DESCRIPTION_FORMAT = "%1$.0f %2$s %3$s of %4$s";

    private SimpleDateFormat mSimpleDateFormat;

    /**
     * Initialize a {@link RecyclerView.Adapter} that listens to a Firebase query. See
     * {@link FirebaseRecyclerOptions} for configuration options.
     *
     * @param options
     */
    public LogHistoryAdapter(Context context, @NonNull FirebaseRecyclerOptions<DrinkLog> options) {
        super(options);

        final Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = context.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = context.getResources().getConfiguration().locale;
        }

        mSimpleDateFormat = new SimpleDateFormat("EEE, d MMM yyyy - h:mm a", locale);
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        final LayoutInflater inflater = LayoutInflater.from(parent.getContext());
        final View view = inflater.inflate(R.layout.log_history_item, parent, false);
        return new LogViewHolder(view);
    }

    @Override
    protected void onBindViewHolder(@NonNull RecyclerView.ViewHolder viewHolder, int i, @NonNull DrinkLog drinkLog) {
        final Calendar cal = Calendar.getInstance();
        cal.setTimeInMillis(drinkLog.timestamp);

        final String formattedDateTime = mSimpleDateFormat.format(cal.getTime());
        final String formattedDescription = getFormattedDrinkDescription(drinkLog);

        final LogViewHolder logViewHolder = (LogViewHolder)viewHolder;
        logViewHolder.mLogDateTimeView.setText(formattedDateTime);
        logViewHolder.mDrinkDescriptionView.setText(formattedDescription);
    }

    private String getFormattedDrinkDescription(DrinkLog drinkLog) {
        final User user = MemoryStore.getInstance().getLoggedInUser();

        final double amount = user.preferredUnits.equalsIgnoreCase(IMPERIAL_UNITS)
                ? drinkLog.amount
                : Utils.convertFlOzToMl(drinkLog.amount);

        final String volumeUnits = user.preferredUnits.equalsIgnoreCase(IMPERIAL_UNITS)
                ? IMPERIAL_VOLUME_UNIT
                : METRIC_VOLUME_UNIT;

        return String.format(DRINK_DESCRIPTION_FORMAT,
                amount, volumeUnits, drinkLog.container, drinkLog.type);
    }

    /**
     * ViewHolder class that holds a reference to the log view.
     */
    class LogViewHolder extends RecyclerView.ViewHolder {
        final TextView mLogDateTimeView;
        final TextView mDrinkDescriptionView;

        LogViewHolder(View view) {
            super(view);
            mLogDateTimeView = view.findViewById(R.id.log_date_time);
            mDrinkDescriptionView = view.findViewById(R.id.drink_description);
        }
    }
}
