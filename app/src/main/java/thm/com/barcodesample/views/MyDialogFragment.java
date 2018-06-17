package thm.com.barcodesample.views;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;

import thm.com.barcodesample.R;
import thm.com.barcodesample.utils.Constants;

public class MyDialogFragment extends DialogFragment {
    private String mMessage;
    private MyDialogListener mListener;

    public interface MyDialogListener {
        void onDialogPositiveClick(DialogFragment dialog);
        void onDialogNegativeClick(DialogFragment dialog);
    }

    public static MyDialogFragment newInstance(String message) {
        Bundle args = new Bundle();
        args.putString(Constants.BUNDLE_MESSAGE, message);
        MyDialogFragment fragment = new MyDialogFragment();
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        try {
            mListener = (MyDialogListener) context;
        } catch (ClassCastException e) {
            throw new ClassCastException(context.getClass()
                + " must implement NoticeDialogListener");
        }
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (getArguments() != null) {
            mMessage = getArguments().getString(Constants.BUNDLE_MESSAGE);
        }
    }

    @NonNull
    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
        builder.setMessage(mMessage)
            .setTitle("Barcode content")
            .setPositiveButton(R.string.ok, (dialog, id) -> mListener.onDialogPositiveClick(this));
        return builder.create();
    }
}
