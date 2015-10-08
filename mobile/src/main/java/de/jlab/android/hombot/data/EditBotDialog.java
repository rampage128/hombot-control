package de.jlab.android.hombot.data;

import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.DialogFragment;
import android.support.v7.app.AlertDialog;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import de.jlab.android.hombot.R;

/**
 * Created by frede_000 on 08.10.2015.
 */
public class EditBotDialog extends DialogFragment implements TextView.OnEditorActionListener {

    private static final String ARG_ID = "id";
    private static final String ARG_NAME = "name";
    private static final String ARG_ADDRESS = "address";

    public interface BotEditDialogListener {
        void onFinishEditDialog(String name, String address, long id);
    }

    private EditText mNameEdit;
    private EditText mAddressEdit;

    public EditBotDialog() {
        // Empty constructor required for DialogFragment
    }

    public static EditBotDialog newInstance(String name, String address, long id) {
        EditBotDialog f = new EditBotDialog();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putLong(ARG_ID, id);
        args.putString(ARG_NAME, name);
        args.putString(ARG_ADDRESS, address);
        f.setArguments(args);

        return f;
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        View view = getActivity().getLayoutInflater().inflate(R.layout.fragment_edit_bot, null);

        mNameEdit = (EditText) view.findViewById(R.id.bot_name);
        mAddressEdit = (EditText) view.findViewById(R.id.bot_address);

        mNameEdit.setText(getArguments().getString(ARG_NAME));
        mAddressEdit.setText(getArguments().getString(ARG_ADDRESS));

        return new AlertDialog.Builder(getActivity())
                .setView(view)
                .setTitle(getArguments().getLong(ARG_ID) > -1 ? R.string.bot_edit : R.string.bot_add)
                .setPositiveButton(R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        BotEditDialogListener activity = (BotEditDialogListener) getActivity();
                        activity.onFinishEditDialog(mNameEdit.getText().toString(), mAddressEdit.getText().toString(), getArguments().getLong(ARG_ID, -1));
                        dialog.dismiss();
                    }
                })
                .setNegativeButton(R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.cancel();
                    }
                }).create();
    }

    @Override
    public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        if (EditorInfo.IME_ACTION_DONE == actionId) {
            // Return input text to activity
/*
            BotEditDialogListener activity = (BotEditDialogListener) getActivity();
            activity.onFinishEditDialog(mNameEdit.getText().toString(), mAddressEdit.getText().toString(), getArguments().getLong(ARG_ID, -1));
            this.dismiss();
*/
            return true;
        }
        return false;
    }
}
