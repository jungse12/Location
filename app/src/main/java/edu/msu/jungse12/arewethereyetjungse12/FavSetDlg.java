package edu.msu.jungse12.arewethereyetjungse12;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.DialogFragment;

public class FavSetDlg extends DialogFragment {
    private AlertDialog dlg;
    @NonNull
    @Override
    public Dialog onCreateDialog(@Nullable Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());

        // Set the title
        builder.setTitle(R.string.fav_destination);
        builder.setMessage(R.string.fav_destination_ask);

        // Add a cancel button
        builder.setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                // Cancel just closes the dialog box
            }
        });

        builder.setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                save();
            }
        });
        dlg = builder.create();
        return dlg;
    }

    private void save() {
        if (!(getActivity() instanceof MainActivity)) {
            return;
        }

        final MainActivity activity = (MainActivity) getActivity();
        activity.saveToFavorite();
    }
}
