package org.herac.tuxguitar.app.undo.undoables.track;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.editors.tab.Caret;
import org.herac.tuxguitar.app.undo.CannotRedoException;
import org.herac.tuxguitar.app.undo.CannotUndoException;
import org.herac.tuxguitar.app.undo.UndoableEdit;
import org.herac.tuxguitar.app.undo.undoables.UndoableCaretHelper;
import org.herac.tuxguitar.song.models.TGTrack;

public class UndoableRemoveTrack implements UndoableEdit{
	private int doAction;
	private UndoableCaretHelper undoCaret;
	private UndoableCaretHelper redoCaret;
	private TGTrack undoableTrack;

	private UndoableRemoveTrack(){
		super();
	}

	@Override
	public void redo() throws CannotRedoException {
		if(!canRedo()){
			throw new CannotRedoException();
		}
		TuxGuitar.instance().getSongManager().removeTrack(cloneTrack(this.undoableTrack));
		TuxGuitar.instance().fireUpdate();
		this.redoCaret.update();

		this.doAction = UNDO_ACTION;
	}

	@Override
	public void undo() throws CannotUndoException {
		if(!canUndo()){
			throw new CannotUndoException();
		}
		TuxGuitar.instance().getSongManager().addTrack(cloneTrack(this.undoableTrack));
		TuxGuitar.instance().fireUpdate();
		this.undoCaret.update();

		this.doAction = REDO_ACTION;
	}

	@Override
	public boolean canRedo() {
		return (this.doAction == REDO_ACTION);
	}

	@Override
	public boolean canUndo() {
		return (this.doAction == UNDO_ACTION);
	}

	public static UndoableRemoveTrack startUndo(){
		UndoableRemoveTrack undoable = new UndoableRemoveTrack();
		Caret caret = getCaret();
		undoable.doAction = UNDO_ACTION;
		undoable.undoCaret = new UndoableCaretHelper();
		undoable.undoableTrack = cloneTrack(caret.getTrack());

		return undoable;
	}

	public UndoableRemoveTrack endUndo(){
		this.redoCaret = new UndoableCaretHelper();
		return this;
	}

	private static Caret getCaret(){
		return TuxGuitar.instance().getTablatureEditor().getTablature().getCaret();
	}

	private static TGTrack cloneTrack(TGTrack track){
		return track.clone(TuxGuitar.instance().getSongManager().getFactory(), TuxGuitar.instance().getSongManager().getSong());
	}
}
