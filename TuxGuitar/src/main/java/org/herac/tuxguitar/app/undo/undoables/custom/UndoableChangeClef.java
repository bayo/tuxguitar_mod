package org.herac.tuxguitar.app.undo.undoables.custom;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.editors.tab.Caret;
import org.herac.tuxguitar.app.undo.CannotRedoException;
import org.herac.tuxguitar.app.undo.CannotUndoException;
import org.herac.tuxguitar.app.undo.UndoableEdit;
import org.herac.tuxguitar.app.undo.undoables.UndoableCaretHelper;
import org.herac.tuxguitar.graphics.control.TGMeasureImpl;
import org.herac.tuxguitar.song.models.TGMeasure;
import org.herac.tuxguitar.song.models.TGTrack;

public class UndoableChangeClef implements UndoableEdit{
	private int doAction;
	private UndoableCaretHelper undoCaret;
	private UndoableCaretHelper redoCaret;
	private long position;
	private int redoableClef;
	private int undoableClef;
	private List<ClefPosition> nextClefPositions;
	private boolean toEnd;
	private TGTrack track;

	private UndoableChangeClef(){
		super();
	}

	@Override
	public void redo() throws CannotRedoException {
		if(!canRedo()){
			throw new CannotRedoException();
		}
		TuxGuitar.instance().getSongManager().getTrackManager().changeClef(this.track,this.position,this.redoableClef,this.toEnd);
		TuxGuitar.instance().fireUpdate();
		this.redoCaret.update();

		this.doAction = UNDO_ACTION;
	}

	@Override
	public void undo() throws CannotUndoException {
		if(!canUndo()){
			throw new CannotUndoException();
		}
		TuxGuitar.instance().getSongManager().getTrackManager().changeClef(this.track,this.position,this.undoableClef,this.toEnd);
		if(this.toEnd){
			Iterator<ClefPosition> it = this.nextClefPositions.iterator();
			while(it.hasNext()){
				ClefPosition ksp = it.next();
				TuxGuitar.instance().getSongManager().getTrackManager().changeClef(this.track,ksp.getPosition(),ksp.getClef(),true);
			}
		}
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

	public static UndoableChangeClef startUndo(){
		UndoableChangeClef undoable = new UndoableChangeClef();
		Caret caret = getCaret();
		undoable.doAction = UNDO_ACTION;
		undoable.undoCaret = new UndoableCaretHelper();
		undoable.position = caret.getPosition();
		undoable.undoableClef = caret.getMeasure().getClef();
		undoable.track = caret.getTrack();
		undoable.nextClefPositions = new ArrayList<ClefPosition>();

		int prevClef = undoable.undoableClef;
		Iterator<TGMeasure> it = caret.getTrack().getMeasures();
		while(it.hasNext()){
			TGMeasureImpl measure = (TGMeasureImpl)it.next();
			if(measure.getStart() > undoable.position){
				int currClef = measure.getClef();
				if(prevClef != currClef){
					ClefPosition tsp = undoable.new ClefPosition(measure.getStart(),currClef);
					undoable.nextClefPositions.add(tsp);
				}
				prevClef = currClef;
			}
		}

		return undoable;
	}

	public UndoableChangeClef endUndo(int clef,boolean toEnd){
		this.redoCaret = new UndoableCaretHelper();
		this.redoableClef = clef;
		this.toEnd = toEnd;
		return this;
	}

	private static Caret getCaret(){
		return TuxGuitar.instance().getTablatureEditor().getTablature().getCaret();
	}

	private class ClefPosition{
		private final long position;
		private final int clef;

		public ClefPosition(long position,int clef) {
			this.position = position;
			this.clef = clef;
		}

		public long getPosition() {
			return this.position;
		}

		public int getClef() {
			return this.clef;
		}
	}
}
