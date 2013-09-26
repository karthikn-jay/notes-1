package com.iliakplv.notes.gui.main;

import android.app.Dialog;
import android.os.Bundle;
import android.support.v4.app.DialogFragment;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.TextNote;
import com.iliakplv.notes.notes.db.NotesDatabaseEntry;
import com.iliakplv.notes.notes.db.NotesDatabaseFacade;
import com.iliakplv.notes.utils.StringUtils;

/**
 * Author: Ilya Kopylov
 * Date:  16.09.2013
 */
public class NoteDialogFragment extends DialogFragment implements View.OnClickListener {

	private NotesDatabaseEntry noteEntry;
	private boolean editMode;

	private EditText title;
	private EditText body;
	private Button saveButton;

	public NoteDialogFragment(NotesDatabaseEntry notesDatabaseEntry) {
		noteEntry = notesDatabaseEntry;
		editMode = noteEntry != null;
	}

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final Dialog dialog = super.onCreateDialog(savedInstanceState);
		dialog.setTitle(editMode ? R.string.note_dialog_edit_note : R.string.note_dialog_new_note);
		return dialog;
	}

	@Override
	public View onCreateView(LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
		final View view = inflater.inflate(R.layout.note_dialog, container, false);
		initControls(view);
		return view;
	}

	private void initControls(View view) {
		final NoteTextWatcher watcher = new NoteTextWatcher();

		title = (EditText) view.findViewById(R.id.note_dialog_title);
		title.addTextChangedListener(watcher);
		body = (EditText) view.findViewById(R.id.note_dialog_body);
		body.addTextChangedListener(watcher);
		saveButton = (Button) view.findViewById(R.id.note_dialog_save);
		saveButton.setOnClickListener(this);
		if (editMode) {
			title.setText(noteEntry.getNote().getTitle());
			body.setText(noteEntry.getNote().getBody());
		}
		view.findViewById(R.id.note_dialog_cancel).setOnClickListener(this);
	}

	@Override
	public void onClick(View button) {
		switch (button.getId()) {
			case R.id.note_dialog_cancel:
				dismiss();
				break;
			case R.id.note_dialog_save:
				final TextNote newNote = getNoteToSave();
				new Thread(new Runnable() {
					@Override
					public void run() {
						final NotesDatabaseFacade dbFacade = NotesDatabaseFacade.getInstance();
						if (editMode) {
							dbFacade.updateNote(noteEntry.getId(), newNote);
						} else {
							dbFacade.insertNote(newNote);
						}
					}
				}).start();
				dismiss();
				break;
		}
	}

	private TextNote getNoteToSave() {
		final String newTitle = title.getText().toString();
		final String newBody = body.getText().toString();
		final TextNote note;

		if (editMode) {
			note = (TextNote) noteEntry.getNote();
			note.setTitle(newTitle);
			note.setBody(newBody);
			note.updateChangeTime();
		} else {
			note = new TextNote(newTitle, newBody);
		}

		return note;
	}


	/*********************************************
	 *
	 *            Inner classes
	 *
	 *********************************************/

	private class NoteTextWatcher implements TextWatcher {

		@Override
		public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

		@Override
		public void onTextChanged(CharSequence s, int start, int before, int count) {}

		@Override
		public void afterTextChanged(Editable s) {

			final boolean notEmptyNote = !(StringUtils.isBlank(title.getText().toString()) &&
					StringUtils.isBlank(body.getText().toString()));

			final boolean ableToSaveNote;

			if (editMode) {
				final String originalTitle = noteEntry.getNote().getTitle();
				final String originalBody = noteEntry.getNote().getBody();
				// Edited note not empty and at least one field (title or body) has been changed
				ableToSaveNote = notEmptyNote &&
						(!StringUtils.equals(originalTitle, title.getText().toString()) ||
								!StringUtils.equals(originalBody, body.getText().toString()));

			} else {
				ableToSaveNote = notEmptyNote;
			}

			saveButton.setEnabled(ableToSaveNote);
		}
	}
}