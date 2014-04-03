package com.iliakplv.notes.gui.main.dialogs;

import android.app.AlertDialog;
import android.app.Dialog;
import android.app.Fragment;
import android.app.FragmentManager;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.CheckBox;
import android.widget.EditText;

import com.iliakplv.notes.NotesApplication;
import com.iliakplv.notes.R;
import com.iliakplv.notes.notes.Label;

public class LabelEditDialog extends AbstractItemDialog {

	public static final int NEW_LABEL = 0;

	private static final String FRAGMENT_TAG = "label_edit_dialog";

	private static final String EXTRA_NOTE_ID = "note_id";
	private static final String EXTRA_LABEL_NAME = "label_name";
	private static final String EXTRA_SELECTED_COLOR = "label_color";
	private EditText nameEditText;
	private int selectedColor;

	private static final int[] COLORS_CHECKBOXES_IDS = {
			R.id.color_1,
			R.id.color_2,
			R.id.color_3,
			R.id.color_4,
			R.id.color_5,
			R.id.color_6,
			R.id.color_7,
			R.id.color_8
	};

	@Override
	public Dialog onCreateDialog(Bundle savedInstanceState) {
		final LayoutInflater inflater = (LayoutInflater) activity.getSystemService(Context.LAYOUT_INFLATER_SERVICE);
		final View labelDialogView = inflater.inflate(R.layout.label_edit_dialog, null);

		final boolean editMode = id != NEW_LABEL;
		final boolean fromSavedInstanceState = savedInstanceState != null;

		final Label label = editMode ?
				dbFacade.getLabel(id).get(0).getEntry() :
				new Label("", Label.DEFAULT_COLOR_INDEX);
		final String labelName = fromSavedInstanceState ?
				savedInstanceState.getString(EXTRA_LABEL_NAME) :
				label.getName();
		nameEditText = (EditText) labelDialogView.findViewById(R.id.label_name);
		nameEditText.setText(labelName);
		selectedColor = fromSavedInstanceState ?
				savedInstanceState.getInt(EXTRA_SELECTED_COLOR) :
				label.getColor();

		final LabelEditDialogCheckBoxListener checkBoxListener =
				new LabelEditDialogCheckBoxListener((CheckBox) labelDialogView.findViewById(COLORS_CHECKBOXES_IDS[selectedColor]));
		for (int i = 0; i < COLORS_CHECKBOXES_IDS.length; i++) {
			final View checkBox = labelDialogView.findViewById(COLORS_CHECKBOXES_IDS[i]);
			checkBox.setOnClickListener(checkBoxListener);
			checkBox.setTag(i);
		}

		final boolean insertLabelToNote = getArguments() != null &&
				getArguments().containsKey(EXTRA_NOTE_ID);
		final int noteId = insertLabelToNote ?
				getArguments().getInt(EXTRA_NOTE_ID) :
				0;

		return new AlertDialog.Builder(activity)
				.setView(labelDialogView)
				.setPositiveButton(R.string.common_save, new DialogInterface.OnClickListener() {
					@Override
					public void onClick(DialogInterface dialogInterface, int i) {
						final String labelName = ((EditText) labelDialogView.findViewById(R.id.label_name)).getText().toString();
						NotesApplication.executeInBackground(new Runnable() {
							@Override
							public void run() {
								label.setName(labelName);
								label.setColor(selectedColor);
								if (editMode) {
									dbFacade.updateLabel(id, label);
								} else {
									final int labelId = dbFacade.insertLabel(label);
									if (insertLabelToNote) {
										dbFacade.insertLabelToNote(noteId, labelId);
									}
								}
								((LabelEditDialogCallback) getTargetFragment()).onLabelChanged();
							}
						});
					}
				})
				.setNegativeButton(R.string.common_cancel, null)
				.create();
	}

	@Override
	public void onSaveInstanceState(Bundle outState) {
		super.onSaveInstanceState(outState);
		outState.putString(EXTRA_LABEL_NAME, nameEditText.getText().toString());
		outState.putInt(EXTRA_SELECTED_COLOR, selectedColor);
	}

	// Show dialog
	public static void show(FragmentManager fragmentManager, int labelId, Fragment targetFragment) {
		createDialog(targetFragment, labelId, 0).show(fragmentManager, FRAGMENT_TAG);
	}

	// Show this dialog for new label creation. Set created label to specified note.
	public static void showCreateAndSet(FragmentManager fragmentManager, Fragment targetFragment, int noteId) {
		createDialog(targetFragment, NEW_LABEL, noteId).show(fragmentManager, FRAGMENT_TAG);
	}

	private static LabelEditDialog createDialog(Fragment targetFragment, int labelId, int noteId) {
		if (!(targetFragment instanceof LabelEditDialogCallback)) {
			throw new IllegalArgumentException("Target fragment must implement callback interface");
		}
		final LabelEditDialog dialog = new LabelEditDialog();
		final Bundle args = createArgumentsBundle(labelId);
		if (noteId > 0) {
			args.putInt(EXTRA_NOTE_ID, noteId);
		}
		dialog.setArguments(args);
		dialog.setTargetFragment(targetFragment, 0);
		return dialog;
	}



	/**
	 * ******************************************
	 *
	 * Inner classes
	 *
	 * *******************************************
	 */

	private class LabelEditDialogCheckBoxListener implements View.OnClickListener {
		private CheckBox currentSelectedCheckBox;

		public LabelEditDialogCheckBoxListener(CheckBox selectedCheckBox) {
			currentSelectedCheckBox = selectedCheckBox;
			currentSelectedCheckBox.setChecked(true);
		}

		@Override
		public void onClick(View newSelectedCheckBox) {
			currentSelectedCheckBox.setChecked(false);
			currentSelectedCheckBox = (CheckBox) newSelectedCheckBox;
			selectedColor = (Integer) newSelectedCheckBox.getTag();
		}
	}

	public static interface LabelEditDialogCallback {
		public void onLabelChanged();
	}
}