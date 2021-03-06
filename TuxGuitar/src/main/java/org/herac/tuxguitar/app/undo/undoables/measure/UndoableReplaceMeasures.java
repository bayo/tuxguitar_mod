package org.herac.tuxguitar.app.undo.undoables.measure;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.undo.CannotRedoException;
import org.herac.tuxguitar.app.undo.CannotUndoException;
import org.herac.tuxguitar.app.undo.UndoableEdit;
import org.herac.tuxguitar.app.undo.undoables.UndoableCaretHelper;
import org.herac.tuxguitar.song.helpers.TGSongSegment;
import org.herac.tuxguitar.song.helpers.TGSongSegmentHelper;
import org.herac.tuxguitar.song.models.TGMarker;

public class UndoableReplaceMeasures implements UndoableEdit{
	private int doAction;
	private final UndoableCaretHelper undoCaret;
	private UndoableCaretHelper redoCaret;
	private final UndoMarkers undoMarkers;
	private final TGSongSegment undoTrackMeasures;
	private TGSongSegment redoTrackMeasures;
	private final int toTrack;
	private int count;
	private int freeSpace;
	private long theMove;

	public UndoableReplaceMeasures(int p1,int p2,int toTrack){
		this.doAction = UNDO_ACTION;
		this.toTrack = toTrack;
		this.undoCaret = new UndoableCaretHelper();
		this.undoMarkers = new UndoMarkers();
		this.undoTrackMeasures = new TGSongSegmentHelper(TuxGuitar.instance().getSongManager()).copyMeasures(p1,p2);
	}

	@Override
	public void redo() throws CannotRedoException {
		if(!canRedo()){
			throw new CannotRedoException();
		}
		for(int i = this.freeSpace;i < this.count;i ++){
			TuxGuitar.instance().getSongManager().addNewMeasureBeforeEnd();
		}
		new TGSongSegmentHelper(TuxGuitar.instance().getSongManager()).replaceMeasures(this.redoTrackMeasures.clone(TuxGuitar.instance().getSongManager().getFactory()),this.theMove,this.toTrack);

		TuxGuitar.instance().fireUpdate();
		this.redoCaret.update();

		this.doAction = UNDO_ACTION;
	}

	@Override
	public void undo() throws CannotUndoException {
		if(!canUndo()){
			throw new CannotUndoException();
		}

		for(int i = this.freeSpace;i < this.count;i ++){
			TuxGuitar.instance().getSongManager().removeLastMeasure();
		}
		new TGSongSegmentHelper(TuxGuitar.instance().getSongManager()).replaceMeasures(this.undoTrackMeasures.clone(TuxGuitar.instance().getSongManager().getFactory()),0,0);

		TuxGuitar.instance().fireUpdate();
		this.undoMarkers.undo();
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

	public UndoableReplaceMeasures endUndo(TGSongSegment tracksMeasures,int count,int freeSpace,long theMove){
		this.redoCaret = new UndoableCaretHelper();
		this.redoTrackMeasures = tracksMeasures;
		this.count = count;
		this.freeSpace = freeSpace;
		this.theMove = theMove;
		return this;
	}

	private class UndoMarkers{
		private final List<TGMarker> markers;

		public UndoMarkers(){
			this.markers = new ArrayList<TGMarker>();
			Iterator<TGMarker> it = TuxGuitar.instance().getSongManager().getMarkers().iterator();
			while(it.hasNext()){
				this.markers.add(it.next().clone(TuxGuitar.instance().getSongManager().getFactory()));
			}
		}

		public void undo(){
			TuxGuitar.instance().getSongManager().removeAllMarkers();
			Iterator<TGMarker> it = this.markers.iterator();
			while(it.hasNext()){
				TGMarker marker = it.next();
				TuxGuitar.instance().getSongManager().updateMarker(marker.clone(TuxGuitar.instance().getSongManager().getFactory()));
			}
		}
	}
}
