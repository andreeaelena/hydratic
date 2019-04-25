package com.hydratic.app.fragment;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import com.hydratic.app.R;
import com.hydratic.app.util.Constants.Extras;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

public class SettingsEditTextDialogFragment extends DialogFragment {

    public interface OnSettingsDialogPositiveButtonClickListener {
        void onClick(DialogInterface dialogInterface, String newValue);
    }

    private OnSettingsDialogPositiveButtonClickListener mOnPositiveButtonClickListener;

    public static SettingsEditTextDialogFragment createInstance(Bundle args, OnSettingsDialogPositiveButtonClickListener onPositiveButtonClickListener) {
        final SettingsEditTextDialogFragment settingsEditTextDialogFragment = new SettingsEditTextDialogFragment();
        settingsEditTextDialogFragment.setArguments(args);
        settingsEditTextDialogFragment.mOnPositiveButtonClickListener = onPositiveButtonClickListener;
        return settingsEditTextDialogFragment;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        String hint = "";
        String value = "";

        final Bundle args = getArguments();
        if (args != null) {
            hint = args.getString(Extras.EXTRA_HINT);
            value = args.getString(Extras.EXTRA_TEXT);
        }

        final AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        final LayoutInflater inflater = requireActivity().getLayoutInflater();
        final View view = inflater.inflate(R.layout.settings_custom_dialog, null);
        final EditText editText = view.findViewById(R.id.dialog_edit_text);
        editText.setHint(hint);
        editText.setText(value);

        builder.setView(view)
                .setPositiveButton(R.string.save, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        mOnPositiveButtonClickListener.onClick(dialogInterface, editText.getText().toString());
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialogInterface, int i) {
                        dialogInterface.dismiss();
                    }
                });

        return builder.create();
    }
}
