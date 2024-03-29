package ro.zero.zeronotes.ui.adapters;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

import ro.zero.zeronotes.R;
import ro.zero.zeronotes.notes.Note;
import ro.zero.zeronotes.storage.DataStorageManager;
import ro.zero.zeronotes.ui.UIResourceManager;
import ro.zero.zeronotes.ui.listeners.implementations.OnNoteLongClickListenerImpl;

public class NoteRecyclerViewAdapter extends RecyclerView.Adapter<NoteRecyclerViewAdapter.ViewHolder> {
	private static final int ITEM_TYPE = 0;
	private static final int ADD_NOTE_TYPE = 1;

	private final List<Note> noteList;

	private View.OnClickListener addNoteButtonClickListener;
	private OnNoteLongClickListenerImpl noteLongClickListener;

	public NoteRecyclerViewAdapter(List<Note> noteList) {
		this.noteList = noteList;
	}

	public void setOnAddButtonClickListener(View.OnClickListener listener) {
		addNoteButtonClickListener = listener;
	}
	public void setNoteLongClickListener(OnNoteLongClickListenerImpl listener) {
		noteLongClickListener = listener;
	}
	// When the view holders are created, this function is called.
	@Override
	public ViewHolder onCreateViewHolder(ViewGroup viewGroup, int viewType) {
		if(viewType == ITEM_TYPE) {
			View view = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.component_note, viewGroup, false);

			return new ItemViewHolder(view,noteLongClickListener);
		} else if(viewType == ADD_NOTE_TYPE) {
			View view = LayoutInflater.from(viewGroup.getContext())
					.inflate(R.layout.component_add_note, viewGroup, false);

			return new AddNoteViewHolder(view,addNoteButtonClickListener);
		}
		return null;
	}

	// When the view holder needs new data, this function is called.
	@Override
	public void onBindViewHolder(ViewHolder viewHolder, final int position) {
		int type = getItemViewType(position);
		if(type == ITEM_TYPE) {
			ItemViewHolder itemHolder = (ItemViewHolder) viewHolder;

			itemHolder.setNote(noteList.get(position));
			itemHolder.setText(noteList.get(position).noteText);
			itemHolder.setCheckboxStatus(noteList.get(position).finished);
			itemHolder.updateSubTasks();
		} else if (type == ADD_NOTE_TYPE) {
			// ... we don't need to bind any data to it since it's a static element, with no data.
		}
	}

	// Return the size of the dataset. We return one more to have space for the "add note" button.
	@Override
	public int getItemCount() {
		return noteList.size() + 1;
	}

	@Override
	public int getItemViewType(int position) {
		if(position < noteList.size()) return ITEM_TYPE;
		else return ADD_NOTE_TYPE;
	}

	public static class ViewHolder extends RecyclerView.ViewHolder {
		public ViewHolder(View view) {
			super(view);
		}
	}
	public static class AddNoteViewHolder extends ViewHolder {
		ImageView addNoteImage;

		public AddNoteViewHolder(View view, View.OnClickListener listener) {
			super(view);

			addNoteImage = view.findViewById(R.id.add_note_button);
			if(listener != null) {
				addNoteImage.setOnClickListener(listener);
			}
		}
	}
	public static class ItemViewHolder extends ViewHolder {
		private final TextView noteTextView;
		private final ImageView checkBoxView;
		private final SubTaskRecyclerViewAdapter subTaskAdapter;
		private Note note = null;

		public ItemViewHolder(View view, OnNoteLongClickListenerImpl listener) {
			super(view);

			noteTextView = view.findViewById(R.id.note_content);
			checkBoxView = view.findViewById(R.id.note_checkBox);
			subTaskAdapter = new SubTaskRecyclerViewAdapter();
			subTaskAdapter.setNoteLongClickListener(listener);


			RecyclerView subTaskRecycler = view.findViewById(R.id.subtaskRecycler);
			subTaskRecycler.setLayoutManager(new LinearLayoutManager(view.getContext()));
			subTaskRecycler.setAdapter(subTaskAdapter);

			checkBoxView.setOnClickListener(v -> {
				if(note != null) {
					note.finished = !note.finished;
					setCheckboxStatus(note.finished);

					// TODO: perhaps cache these changes and mass-commit them?
					DataStorageManager.getInstance().save();
				}
			});

			if(listener != null) {
				view.setOnLongClickListener(v -> {
					listener.clickedNote = note;
					return listener.onLongClick(v);
				});
			}
		}

		public void setText(String str) {
			noteTextView.setText(str);
		}
		public void setCheckboxStatus(boolean checked) {
			if(checked) {
				checkBoxView.setImageDrawable(UIResourceManager.getInstance().note_checkedIcon);
				noteTextView.setBackgroundResource(R.color.note_finished);
				noteTextView.setTextColor(UIResourceManager.getInstance().note_textColor_finished);
			} else {
				checkBoxView.setImageDrawable(UIResourceManager.getInstance().note_uncheckedIcon);
				noteTextView.setBackgroundResource(R.color.note_default);
				noteTextView.setTextColor(UIResourceManager.getInstance().note_textColor_default);
			}
		}
		public void setNote(Note note) {
			this.note = note;
		}
		public void updateSubTasks() {
			if(note != null) {
				subTaskAdapter.setSubTaskList(note.subTasks);
				subTaskAdapter.notifyDataSetChanged();
			}
		}
	}
}
