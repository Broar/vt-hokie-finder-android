package com.parseapp.vthokiefinder;

import android.app.Activity;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentSender;
import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.support.v4.app.Fragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderApi;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.internal.FusedLocationProviderResult;
import com.google.maps.model.LatLng;

/**
 * A retained fragment that manages a connection to the GoogleApiClient
 *
 * @author Steven Briggs
 * @version 2015.10.23
 */
public class GoogleApiManagerFragment extends Fragment implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener {

    public static final String TAG = GoogleApiManagerFragment.class.getSimpleName();

    private static final int REQUEST_RESOLVE_ERROR = 1001;
    private static final String DIALOG_ERROR = "dialog_error";

    private Callbacks mListener;

    private GoogleApiClient mGoogleApiClient;
    private boolean mIsResolvingError;

    public interface Callbacks {
        void onClientConnected();
    }

    /**
     * A factory method to return a new GoogleApiManagerFragment that has been configured
     *
     * @return a new GoogleApiManagerFragment that has been configured
     */
    public static GoogleApiManagerFragment newInstance() {
        return new GoogleApiManagerFragment();
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

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
        mIsResolvingError = false;
        buildGoogleApiClient();
        mGoogleApiClient.connect();
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_RESOLVE_ERROR) {
            mIsResolvingError = false;

            // Make sure the app is not already connected or attempting to connect
            if (resultCode == Activity.RESULT_OK) {
                if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
                    mGoogleApiClient.connect();
                }
            }
        }
    }

    @Override
    public void onResume() {
        super.onResume();
        mGoogleApiClient.connect();
    }

    @Override
    public void onPause() {
        super.onPause();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onStop() {
        super.onStop();
        mGoogleApiClient.disconnect();
    }

    @Override
    public void onConnected(Bundle bundle) {
        // Inform anyone waiting on a connection to the GoogleApiClient that it's ready
        mListener.onClientConnected();
    }

    @Override
    public void onConnectionSuspended(int i) {
        // Space purposefully left empty
    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        // Already handling the error
        if (mIsResolvingError) {
            return;
        }

        // Attempt to resolve the connection error
        else if (connectionResult.hasResolution()) {
            try {
                mIsResolvingError = true;
                connectionResult.startResolutionForResult(getActivity(), REQUEST_RESOLVE_ERROR);
            }

            // There was an error. Try again
            catch (IntentSender.SendIntentException e) {
                mGoogleApiClient.connect();
            }
        }

        // Show dialog using GoogleApiAvailability.getErrorDialog()
        else {
            showErrorDialog(connectionResult.getErrorCode());
            mIsResolvingError = true;
        }
    }

    public GoogleApiClient getClient() {
        return mGoogleApiClient;
    }

    /**
     * Determine if the GoogleApiClient is connected
     * @return
     */
    public boolean isClientConnected() {
        return mGoogleApiClient.isConnected();
    }

    /**
     * Connect the GoogleApiClient
     */
    public void connectClient() {
        if (!mGoogleApiClient.isConnecting() && !mGoogleApiClient.isConnected()) {
            mGoogleApiClient.connect();
        }
    }

    /**
     * Disconnect the GoogleApiClient
     */
    public void disconnectClient() {
        if (mGoogleApiClient.isConnecting() || mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }
    }

    /**
     * Retrieve the last known location of the user
     *
     * @return the last known location of the user if it exists; otherwise, null
     */
    public Location getCurrentLocation() {
        if (isClientConnected())  {
            Location location = LocationServices.FusedLocationApi.getLastLocation(mGoogleApiClient);
            return location;
        }

        return null;
    }

    /**
     * Determine if the GoogleApiClient is resolving an error
     *
     * @return true if the GoogleApiClietn is resolving an error, false if not
     */
    public boolean isResolvingError() {
        return mIsResolvingError;
    }

    /**
     * Build a new instance of the GoogleApiClient.
     */
    private synchronized void buildGoogleApiClient() {
        mGoogleApiClient = new GoogleApiClient.Builder(getContext())
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();
    }

    /**
     * Display an ErrorDialogFragment
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     *
     * @param errorCode the error code that prompted the dialog
     */
    private void showErrorDialog(int errorCode) {
        // Create a fragment for the error dialog
        ErrorDialogFragment dialogFragment = new ErrorDialogFragment();
        // Pass the error that should be displayed
        Bundle args = new Bundle();
        args.putInt(DIALOG_ERROR, errorCode);
        dialogFragment.setArguments(args);
        dialogFragment.show(getChildFragmentManager(), "errordialog");
    }

    /**
     * Callback method when an ErrorDialogFragment is dismissed
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     */
    private void onDialogDismissed() {
        mIsResolvingError = false;
    }

    /**
     * A DialogFragment to prompt the user to update Google Play Services
     *
     * https://developers.google.com/android/guides/api-client#handle_connection_failures
     */
    public static class ErrorDialogFragment extends DialogFragment {
        public ErrorDialogFragment() { }

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {
            // Get the error code and retrieve the appropriate dialog
            int errorCode = getArguments().getInt(DIALOG_ERROR);
            return GoogleApiAvailability.getInstance().getErrorDialog(getActivity(), errorCode, REQUEST_RESOLVE_ERROR);
        }

        @Override
        public void onDismiss(DialogInterface dialog) {
            ((GoogleApiManagerFragment) getParentFragment()).onDialogDismissed();
        }
    }
}
