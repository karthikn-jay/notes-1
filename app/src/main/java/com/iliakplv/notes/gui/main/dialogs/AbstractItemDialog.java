package com.iliakplv.notes.gui.main.dialogs;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

import com.iliakplv.notes.notes.db.NotesDatabaseFacade;

public abstract class AbstractItemDialog extends DialogFragment {

	public static final String EXTRA_ID = "item_id";

	protected NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();

	protected Activity activity;
	protected int id;

	@Override
	public void onAttach(Activity activity) {
		super.onAttach(activity);

		this.activity = getActivity();
	}

	@Override
	public void onCreate(Bundle savedInstanceState) {
		super.onCreate(savedInstanceState);

		final Bundle args = getArguments();
		if (args == null || !args.containsKey(EXTRA_ID)) {
			throw new RuntimeException("Item id required");
		}
		id = args.getInt(EXTRA_ID);
	}

	protected static Bundle createArgumentsBundle(int noteId) {
		final Bundle args = new Bundle();
		args.putInt(EXTRA_ID, noteId);
		return args;
	}
}