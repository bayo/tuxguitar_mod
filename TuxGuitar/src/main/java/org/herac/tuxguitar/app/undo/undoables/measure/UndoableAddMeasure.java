package org.herac.tuxguitar.app.undo.undoables.measure;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.undo.CannotRedoException;
import org.herac.tuxguitar.app.undo.CannotUndoException;
import org.herac.tuxguitar.app.undo.UndoableEdit;
import org.herac.tuxguitar.app.undo.undoables.UndoableCaretHelper;

public class UndoableAddMeasure implements UndoableEdit{
	private int doAction;
	private UndoableCaretHelper undoCaret;
	private UndoableCaretHelper redoCaret;
	private int number;

	private UndoableAddMeasure(){
		super();
	}

	@Override
	public void redo() throws CannotRedoException {
		if(!canRedo()){
			throw new CannotRedoException();
		}
		TuxGuitar.instance().getSongManager().addNewMeasure(this.number);
		TuxGuitar.instance().fireUpdate();
		this.redoCaret.update();

		this.doAction = UNDO_ACTION;
	}

	@Override
	public void undo() throws CannotUndoException {
		if(!canUndo()){
			throw new CannotUndoException();
		}
		TuxGuitar.instance().getSongManager().removeMeasure(this.number);
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

	public static UndoableAddMeasure startUndo(int number){
		UndoableAddMeasure undoable = new UndoableAddMeasure();
		undoable.undoCaret = new UndoableCaretHelper();
		undoable.doAction = UNDO_ACTION;
		undoable.number = number;
		return undoable;
	}

	public UndoableAddMeasure endUndo(){
		this.redoCaret = new UndoableCaretHelper();
		return this;
	}

}
