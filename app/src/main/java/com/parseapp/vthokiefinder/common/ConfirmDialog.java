package com.parseapp.vthokiefinder.common;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;

/**
 * A dialog prompting the user to confirm an action
 *
 * @author Steven Briggs
 * @version 2015.10.26
 */
public class ConfirmDialog extends DialogFragment {

    public static final String TAG = ConfirmDialog.class.getSimpleName();

    private static final String TITLE_KEY = "titleKey";
    private static final String POSITIVE_KEY = "positiveKey";
    private static final String NEGATIVE_KEY = "negativeKey";

    private Callbacks mListener;

    public interface Callbacks {
        void onPositiveButtonClicked();
        void onNegativeButtonClicked();
    }

    /**
     * A factory method to return a new ConfirmDialog that has been configured
     *
     * @param title the title of the dialog
     * @param positive the text for the positive button
     * @param negative the text for the negative button
     * @return a new ConfirmDialog that has been configured
     */
    public static ConfirmDialog newInstance(String title, String positive, String negative) {
        ConfirmDialog dialog = new ConfirmDialog();

        Bundle args = new Bundle();
        args.putString(TITLE_KEY, title);
        args.putString(POSITIVE_KEY, positive);
        args.putString(NEGATIVE_KEY, negative);
        dialog.setArguments(args);

        return dialog;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        Activity activity = (Activity) context;

        try {
            mListener = (Callbacks) activity;
        }

        catch (ClassCastException e) {
            throw new ClassCastException(activity.toString() + " must implement Callbacks");
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        Bundle args = getArguments();
        String title = args.getString(TITLE_KEY);
        String positive = args.getString(POSITIVE_KEY);
        String negative = args.getString(NEGATIVE_KEY);

        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setTitle(title)
                .setPositiveButton(positive, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onPositiveButtonClicked();
                        dismiss();
                    }
                })
                .setNegativeButton(negative, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        mListener.onNegativeButtonClicked();
                        dismiss();
                    }
                })
                .setCancelable(true);

        return builder.create();
    }
}
