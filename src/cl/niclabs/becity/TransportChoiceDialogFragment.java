package cl.niclabs.becity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.view.View;
import android.widget.Button;
import cl.niclabs.adkmobile.monitor.data.Journey.Transport;
import cl.niclabs.becity.sampler.R;

public class TransportChoiceDialogFragment extends DialogFragment {
	/* The activity that creates an instance of this dialog fragment must
     * implement this interface in order to receive event callbacks.
     * Each method passes the DialogFragment in case the host needs to query it. */
	public interface TransportChoiceDialogListener {
		public void onTransportChoiceCancelled(DialogFragment dialog);
		public void onTransportSelected(DialogFragment dialog, Transport transport);
	}
	
	private TransportChoiceDialogListener listener;
	
	private Transport selectedTransport;
	
	// Override the Fragment.onAttach() method to instantiate the TransportChoiceDialogListener
    @Override
    public void onAttach(Activity activity) {
        super.onAttach(activity);
        // Verify that the host activity implements the callback interface
        try {
            // Instantiate the NoticeDialogListener so we can send events to the host
        	listener = (TransportChoiceDialogListener) activity;
        } catch (ClassCastException e) {
            // The activity doesn't implement the interface, throw exception
            throw new ClassCastException(activity.toString()
                    + " must implement TransportChoiceDialogListener");
        }
    }
	
	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		
	    AlertDialog.Builder builder = new AlertDialog.Builder(getActivity());
		builder.setTitle(R.string.dialog_title)
				.setSingleChoiceItems(R.array.transport_choices, -1,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog,
									int which) {
								int[] ids = getResources().getIntArray(
										R.array.transport_ids);
								selectedTransport = Transport
										.getInstance(ids[which]);
							}
						})
				.setPositiveButton(R.string.start,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								// Do nothing here since it will be overriden later
							}
						})
				.setNegativeButton(R.string.cancel,
						new DialogInterface.OnClickListener() {
							@Override
							public void onClick(DialogInterface dialog, int id) {
								listener.onTransportChoiceCancelled(TransportChoiceDialogFragment.this);
							}
						});
	    return builder.create();
	}
	
	@Override
	public void onStart() {
		super.onStart(); // super.onStart() is where dialog.show() is actually
							// called on the underlying dialog, so we have to do
							// it after this point
		AlertDialog d = (AlertDialog) getDialog();
		if (d != null) {
			Button positiveButton = (Button) d
					.getButton(Dialog.BUTTON_POSITIVE);
			positiveButton.setOnClickListener(new View.OnClickListener() {
				@Override
				public void onClick(View v) {
					if (selectedTransport != null) {
						listener.onTransportSelected(
								TransportChoiceDialogFragment.this,
								selectedTransport);

						// Only close the dialog if the user made a choice
						dismiss();
					}
				}
			});
		}
	}
}
