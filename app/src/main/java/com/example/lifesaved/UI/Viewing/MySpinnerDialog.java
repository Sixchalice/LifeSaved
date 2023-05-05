package com.example.lifesaved.UI.Viewing;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.os.Bundle;

import androidx.fragment.app.DialogFragment;

public class MySpinnerDialog extends DialogFragment {

    public MySpinnerDialog() {
    // Empty constructor required for DialogFragment
    }

    @Override
    public Dialog onCreateDialog(final Bundle savedInstanceState) {

        ProgressDialog _dialog = new ProgressDialog(getActivity());
        this.setStyle(STYLE_NO_TITLE, getTheme());
        _dialog.setMessage("Generating your video..");

        _dialog.setCancelable(false);

        return _dialog;
    }
}