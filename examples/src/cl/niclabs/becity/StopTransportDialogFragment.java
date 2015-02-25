package cl.niclabs.becity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import cl.niclabs.becity.sampler.R;

public class StopTransportDialogFragment extends DialogFragment {
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
	public interface StopTransportDialogListener {
		public void onStopTransportAccept(DialogFragment dialog);
		public void onStopTransportCancel(DialogFragment dialog);
	}
	
	private StopTransportDialogListener listener;
	
	// Override the Fragment.onAttach() method to instantiate the SendDataDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	listener = (StopTransportDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement SendDataDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.stop_title)
				.setMessage(R.string.stop_message)
				.setPositiveButton(R.string.stop,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								listener.onStopTransportAccept(StopTransportDialogFragment.this);
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								listener.onStopTransportCancel(StopTransportDialogFragment.this);
							}
						});
	    return builder.create();
	}
}
