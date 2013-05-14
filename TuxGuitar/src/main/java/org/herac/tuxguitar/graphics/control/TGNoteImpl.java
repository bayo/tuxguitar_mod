/*
 * Created on 29-nov-2005
 */
package org.herac.tuxguitar.graphics.control;

import java.util.Iterator;

import org.herac.tuxguitar.graphics.TGPainter;
import org.herac.tuxguitar.graphics.TGRectangle;
import org.herac.tuxguitar.graphics.control.painters.TGKeySignaturePainter;
import org.herac.tuxguitar.graphics.control.painters.TGNotePainter;
import org.herac.tuxguitar.song.factory.TGFactory;
import org.herac.tuxguitar.song.models.TGBeat;
import org.herac.tuxguitar.song.models.TGDuration;
import org.herac.tuxguitar.song.models.TGNote;
import org.herac.tuxguitar.song.models.TGNoteEffect;
import org.herac.tuxguitar.song.models.TGVoice;
import org.herac.tuxguitar.song.models.effects.TGEffectHarmonic;

/**
 * @author julian
 */
public class TGNoteImpl extends TGNote {

	private final TGRectangle noteOrientation;

	private int tabPosY;

	private int scorePosY;

	private int accidental;

	public TGNoteImpl(TGFactory factory) {
		super(factory);
		this.noteOrientation = new TGRectangle();
	}

	/**
	 * Actualiza los valores para dibujar
	 */
	public void update(TGLayout layout) {
		this.accidental = getMeasureImpl().getNoteAccidental( getRealValue() );
		this.tabPosY = ( (getString() * layout.getStringSpacing()) - layout.getStringSpacing() );
		this.scorePosY = getVoiceImpl().getBeatGroup().getY1(layout,this,getMeasureImpl().getKeySignature(),getMeasureImpl().getClef());
	}

	/**
	 * Pinta la nota
	 */
	public void paint(TGLayout layout,TGPainter painter, int fromX, int fromY) {
		int spacing = getBeatImpl().getSpacing();
		paintScoreNote(layout, painter, fromX, fromY + getPaintPosition(TGTrackSpacing.POSITION_SCORE_MIDDLE_LINES),spacing);
		if(!layout.isPlayModeEnabled()){
			paintOfflineEffects(layout, painter,fromX,fromY, spacing);
		}
		paintTablatureNote(layout, painter, fromX, fromY + getPaintPosition(TGTrackSpacing.POSITION_TABLATURE),spacing);
	}

	private void paintOfflineEffects(TGLayout layout,TGPainter painter,int fromX, int fromY, int spacing){
		TGSpacing bs = getBeatImpl().getBs();
		TGSpacing ts = getMeasureImpl().getTs();
		TGNoteEffect effect = getEffect();

		int tsY = (fromY + ts.getPosition(TGTrackSpacing.POSITION_EFFECTS));
		int bsY = (tsY + (ts.getSize(TGTrackSpacing.POSITION_EFFECTS) - bs.getSize( )));

		layout.setOfflineEffectStyle(painter);
		if(effect.isAccentuatedNote()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_ACCENTUATED_EFFECT ));
			paintAccentuated(layout, painter, x, y);
		}
		if(effect.isHeavyAccentuatedNote()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_HEAVY_ACCENTUATED_EFFECT ));
			paintHeavyAccentuated(layout, painter, x, y);
		}
		if(effect.isFadeIn()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_FADE_IN ));
			paintFadeIn(layout, painter, x, y);
		}
		if(effect.isHarmonic() && (layout.getStyle() & TGLayout.DISPLAY_SCORE) == 0 ){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_HARMONIC_EFFEC ));
			String key = new String();
			key = effect.getHarmonic().isNatural()?TGEffectHarmonic.KEY_NATURAL:key;
			key = effect.getHarmonic().isArtificial()?TGEffectHarmonic.KEY_ARTIFICIAL:key;
			key = effect.getHarmonic().isTapped()?TGEffectHarmonic.KEY_TAPPED:key;
			key = effect.getHarmonic().isPinch()?TGEffectHarmonic.KEY_PINCH:key;
			key = effect.getHarmonic().isSemi()?TGEffectHarmonic.KEY_SEMI:key;
			painter.drawString(key,x, y);
		}
		if(effect.isTapping()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_TAPPING_EFFEC ));
			painter.drawString("T",x, y);
		}
		if(effect.isSlapping()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_SLAPPING_EFFEC ));
			painter.drawString("S",x, y);
		}
		if(effect.isPopping()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_POPPING_EFFEC ));
			painter.drawString("P",x, y);
		}
		if(effect.isPalmMute()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_PALM_MUTE_EFFEC ));
			painter.drawString("P.M",x, y);
		}
		if(effect.isLetRing()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_LET_RING_EFFEC ));
			painter.drawString("L.R",x, y);
		}
		if(effect.isVibrato()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_VIBRATO_EFFEC ));
			paintVibrato(layout, painter,x,y);
		}
		if(effect.isTrill()){
			int x = fromX + getPosX() + spacing;
			int y = (bsY + bs.getPosition( TGBeatSpacing.POSITION_TRILL_EFFEC ));
			paintTrill(layout, painter,x,y);
		}
	}

	/**
	 * Pinta la nota en la tablatura
	 */
	public void paintTablatureNote(TGLayout layout,TGPainter painter, int fromX, int fromY, int spacing) {
		int style = layout.getStyle();
		if((style & TGLayout.DISPLAY_TABLATURE) != 0){
			int stringSpacing = layout.getStringSpacing();
			int x = fromX + getPosX() + spacing + 2;
			int y = fromY + getTabPosY();

			this.noteOrientation.setX(x);
			this.noteOrientation.setY(y);
			this.noteOrientation.setWidth(1);
			this.noteOrientation.setHeight(1);

			layout.setTabNoteStyle(painter, (layout.isPlayModeEnabled() && getBeatImpl().isPlaying(layout)));
			//-------------ligadura--------------------------------------
			if (isTiedNote() && (style & TGLayout.DISPLAY_SCORE) == 0) {
				float tX = 0;
				float tY = 0;
				float tWidth = 0;
				float tHeight = (stringSpacing * 3);
				TGNoteImpl noteForTie = getNoteForTie();
				if (noteForTie != null) {
					tX = (fromX + noteForTie.getPosX() + noteForTie.getBeatImpl().getSpacing());
					tY = (fromY + noteForTie.getTabPosY() + stringSpacing);
					tWidth = (x - tX);
				}else{
					TGRectangle r = layout.getNoteOrientation(painter,x,y,this);
					tX = r.getX() - (stringSpacing * 2);
					tY = (fromY + getTabPosY() + stringSpacing);
					tWidth = (stringSpacing * 2);
				}
				painter.initPath();
				painter.addArc(tX, (tY - tHeight ), tWidth, tHeight, 225,90);
				painter.closePath();

			//-------------nota--------------------------------------
			} else if(!isTiedNote()){
				TGRectangle r = layout.getNoteOrientation(painter,x,y,this);
				this.noteOrientation.setX(r.getX());
				this.noteOrientation.setY(r.getY());
				this.noteOrientation.setWidth(r.getWidth());
				this.noteOrientation.setHeight(r.getHeight());
				String visualNote = (getEffect().isDeadNote())?"X":Integer.toString(getValue());
				visualNote = (getEffect().isGhostNote())?"(" + visualNote + ")":visualNote;

				final int fmWidth = painter.getFMWidth(visualNote);
				final int fmHeight = painter.getFMHeight();
				painter.initPath(TGPainter.PATH_FILL);
				painter.addRectangle(this.noteOrientation.getX(),this.noteOrientation.getY()+(int)(fmHeight*0.2),fmWidth, (int)(fmHeight*0.60));
				painter.closePath();
				painter.drawString(visualNote, this.noteOrientation.getX(), this.noteOrientation.getY(), true);
			}

			//-------------efectos--------------------------------------
			if(! layout.isPlayModeEnabled() ){

				paintEffects(layout,painter,fromX,fromY,spacing);

				if((style & TGLayout.DISPLAY_SCORE) == 0){
					int y1 = (fromY + getMeasureImpl().getTrackImpl().getTabHeight() + (stringSpacing / 2));
					int y2 = (fromY + getMeasureImpl().getTrackImpl().getTabHeight() + ((stringSpacing / 2) * 5));

					//-------------tremolo picking--------------------------------------
					if(getEffect().isTremoloPicking()){
						layout.setTabEffectStyle(painter);
						painter.initPath();
						int posy = (y1 + ((y2 - y1) / 2));
						for(int i = TGDuration.EIGHTH;i <= getEffect().getTremoloPicking().getDuration().getValue(); i += i){
							painter.moveTo(x - 3, posy - 1);
							painter.lineTo(x + 4,posy + 1);
							posy += 4;
						}
						painter.setLineWidth(2);
						painter.closePath();
						painter.setLineWidth(1);
					}
				}
			}
		}
	}

	/**
	 * Pinta la nota en la partitura
	 */
	private void paintScoreNote(TGLayout layout,TGPainter painter, int fromX, int fromY, int spacing) {
		if((layout.getStyle() & TGLayout.DISPLAY_SCORE) != 0 ){
			float scale = layout.getScoreLineSpacing();
			int direction = getVoiceImpl().getBeatGroup().getDirection();
			int key = getMeasureImpl().getKeySignature();
			int clef = getMeasureImpl().getClef();

			int x = ( fromX + getPosX() + spacing );
			int y1 = ( fromY + getScorePosY() ) ;

			//-------------foreground--------------------------------------
			boolean playing = (layout.isPlayModeEnabled() && getBeatImpl().isPlaying(layout));

			layout.setScoreNoteStyle(painter,playing);

			//----------ligadura---------------------------------------
			if (isTiedNote()) {
				TGNoteImpl noteForTie = getNoteForTie();
				float tScale = (scale / 8.0f);
				float tX = x - (20.0f * tScale);
				float tY = y1 - (2.0f * tScale);
				float tWidth = (20.0f * tScale);
				float tHeight = (30.0f * tScale);
				if (noteForTie != null) {
					int tNoteX = (fromX + noteForTie.getPosX() + noteForTie.getBeatImpl().getSpacing());
					int tNoteY = (fromY + getScorePosY());
					tX = tNoteX + (6.0f * tScale);
					tY = tNoteY - (3.0f * tScale);
					tWidth = (x - tNoteX) - (3.0f * tScale);
					tHeight = (35.0f * tScale);
				}
				painter.initPath();
				painter.addArc(tX,tY, tWidth, tHeight, 45, 90);
				painter.closePath();
			}
			//----------sostenido--------------------------------------
			if(this.accidental == TGMeasureImpl.NATURAL){
				painter.initPath(TGPainter.PATH_FILL);
				TGKeySignaturePainter.paintNatural(painter,(x - (scale - (scale / 4)) ),(y1 + (scale / 2)), scale);
				painter.closePath();
			}
			else if(this.accidental == TGMeasureImpl.SHARP){
				painter.initPath(TGPainter.PATH_FILL);
				TGKeySignaturePainter.paintSharp(painter,(x - (scale - (scale / 4)) ),(y1 + (scale / 2)), scale);
				painter.closePath();
			}
			else if(this.accidental == TGMeasureImpl.FLAT){
				painter.initPath(TGPainter.PATH_FILL);
				TGKeySignaturePainter.paintFlat(painter,(x - (scale - (scale / 4)) ),(y1 + (scale / 2)), scale);
				painter.closePath();
			}
			//----------fin sostenido--------------------------------------
			if(getEffect().isHarmonic()){
				if( layout.isBufferEnabled() ){
					painter.drawImage(layout.getResources().getHarmonicNote(getVoice().getDuration().getValue(),playing),x,y1);
				}else{
					boolean full = (getVoice().getDuration().getValue() >= TGDuration.QUARTER);
					painter.initPath((full ? TGPainter.PATH_DRAW | TGPainter.PATH_FILL : TGPainter.PATH_DRAW));
					TGNotePainter.paintHarmonic(painter,x,y1+1,(layout.getScoreLineSpacing() - 2) );
					painter.closePath();
				}
			}else{
				if( layout.isBufferEnabled() ){
					painter.drawImage(layout.getResources().getScoreNote(getVoice().getDuration().getValue(),playing),x,y1);
				}else{
					boolean full = (getVoice().getDuration().getValue() >= TGDuration.QUARTER);
					painter.initPath((full ? TGPainter.PATH_FILL : TGPainter.PATH_DRAW));
					TGNotePainter.paintNote(painter,x,y1+1, ((full ? layout.getScoreLineSpacing() + 1 : layout.getScoreLineSpacing() ) - 2) );
					painter.closePath();
				}
			}

			if(! layout.isPlayModeEnabled() ){

				if(getEffect().isGrace()){
					paintGrace(layout, painter,x ,y1);
				}

				//PUNTILLO y DOBLE PUNTILLO
				if (getVoice().getDuration().isDotted() || getVoice().getDuration().isDoubleDotted()) {
					getVoiceImpl().paintDot(layout, painter,( x + (12.0f * (scale / 8.0f) ) ), ( y1 + (layout.getScoreLineSpacing()/ 2)), (scale / 10.0f) );
				}

				//dibujo el pie
				if(getVoice().getDuration().getValue() >= TGDuration.HALF){
					layout.setScoreNoteFooterStyle(painter);
					int xMove = (direction == TGBeatGroup.DIRECTION_UP ? layout.getResources().getScoreNoteWidth() : 0);
					int y2 = fromY + getVoiceImpl().getBeatGroup().getY2(layout,getPosX() + spacing,key,clef);

					//staccato
					if (getEffect().isStaccato()) {
						int size = 3;
						int sX = x + xMove;
						int sY = (y2 + (4 * ((direction == TGBeatGroup.DIRECTION_UP)?-1:1)));
						layout.setScoreEffectStyle(painter);
						painter.initPath(TGPainter.PATH_FILL);
						painter.moveTo(sX - (size / 2),sY - (size / 2));
						painter.addOval(sX - (size / 2),sY - (size / 2),size,size);
						painter.closePath();
					}
					//tremolo picking
					if(getEffect().isTremoloPicking()){
						layout.setScoreEffectStyle(painter);
						painter.initPath();
						int tpY = fromY;
						if((direction == TGBeatGroup.DIRECTION_UP)){
							tpY += (getVoiceImpl().getBeatGroup().getMaxNote().getScorePosY() - layout.getScoreLineSpacing() - 4);
						}else{
							tpY += (getVoiceImpl().getBeatGroup().getMinNote().getScorePosY() + layout.getScoreLineSpacing() + 4);
						}
						for(int i = TGDuration.EIGHTH;i <= getEffect().getTremoloPicking().getDuration().getValue(); i += i){
							painter.moveTo(x + xMove - 3, tpY + 1);
							painter.lineTo(x + xMove + 4,tpY - 1);
							tpY += 4;
						}
						painter.setLineWidth(2);
						painter.closePath();
						painter.setLineWidth(1);
					}
				}else{
					//staccato
					if (getEffect().isStaccato()) {
						int size = 3;
						int sX = x + (layout.getResources().getScoreNoteWidth() / 2);
						int sY = (fromY + getVoiceImpl().getBeatGroup().getMinNote().getScorePosY() + layout.getScoreLineSpacing()) + 2;
						layout.setScoreEffectStyle(painter);
						painter.initPath(TGPainter.PATH_FILL);
						painter.moveTo(sX - (size / 2),sY - (size / 2));
						painter.addOval(sX - (size / 2),sY - (size / 2),size,size);
						painter.closePath();
					}
					//tremolo picking
					if(getEffect().isTremoloPicking()){
						layout.setScoreEffectStyle(painter);
						painter.initPath();
						int tpX = (x + (layout.getResources().getScoreNoteWidth() / 2));
						int tpY = fromY + (getVoiceImpl().getBeatGroup().getMaxNote().getScorePosY() - layout.getScoreLineSpacing() - 4);
						for(int i = TGDuration.EIGHTH;i <= getEffect().getTremoloPicking().getDuration().getValue(); i += i){
							painter.moveTo(tpX - 3, tpY + 1);
							painter.lineTo(tpX + 4,tpY - 1);
							tpY += 4;
						}
						painter.setLineWidth(2);
						painter.closePath();
						painter.setLineWidth(1);
					}
				}
			}
		}
	}

	/**
	 * Encuentra la nota a la que esta ligada
	 */
	private TGNoteImpl getNoteForTie() {
		for (int i = getMeasureImpl().countBeats() - 1; i >= 0; i--) {
			TGBeat beat = getMeasureImpl().getBeat(i);
			TGVoice voice = beat.getVoice( getVoice().getIndex() );
			if (beat.getStart() < getBeatImpl().getStart() && !voice.isRestVoice()) {
				Iterator<TGNote> it = voice.getNotes().iterator();
				while(it.hasNext()){
					TGNoteImpl note = (TGNoteImpl)it.next();
					if (note.getString() == getString()) {
						return note;
					}
				}
			}
		}
		return null;
	}

	/**
	 * Pinta los efectos
	 */
	private void paintEffects(TGLayout layout,TGPainter painter, int fromX, int fromY, int spacing){
		int x = fromX + getPosX() + spacing;
		int y = fromY + getTabPosY();
		TGNoteEffect effect = getEffect();
		if(effect.isGrace()){
			layout.setTabGraceStyle(painter);
			String value = Integer.toString(effect.getGrace().getFret());
			painter.drawString(value, (this.noteOrientation.getX() - painter.getFMWidth(value) - 2), this.noteOrientation.getY() );
		}
		if(effect.isBend()){
			paintBend(layout, painter,(this.noteOrientation.getX() + this.noteOrientation.getWidth()), y);
		}else if(effect.isTremoloBar()){
			paintTremoloBar(layout, painter,(this.noteOrientation.getX() + this.noteOrientation.getWidth()), y);
		}else if(effect.isSlide() || effect.isHammer()){
			int nextFromX = fromX;
			TGNoteImpl nextNote = (TGNoteImpl)layout.getSongManager().getMeasureManager().getNextNote(getMeasureImpl(),getBeatImpl().getStart(),getVoice().getIndex(),getString());
			if(effect.isSlide()){
				paintSlide(layout, painter, nextNote, x, y, nextFromX);
			}else if(effect.isHammer()){
				paintHammer(layout, painter, nextNote, x, y, nextFromX);
			}
		}
	}

	private void paintBend(TGLayout layout,TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x = (fromX + (1.0f * scale));
		float y = (fromY - (2.0f * scale));

		layout.setTabEffectStyle(painter);

		painter.initPath();
		painter.moveTo( x, y );
		painter.lineTo( x + (1.0f * scale), y );
		painter.cubicTo(x + (1.0f * scale), y,  x + (3.0f * scale), y , x + (3.0f * scale), y - (2.0f * scale));
		painter.moveTo( x + (3.0f * scale), y - (2.0f * scale) );
		painter.lineTo( x + (3.0f * scale), y - (12.0f * scale));
		painter.moveTo( x + (3.0f * scale), y - (12.0f * scale));
		painter.lineTo( x + (1.0f * scale), y - (10.0f * scale));
		painter.moveTo( x + (3.0f * scale), y - (12.0f * scale));
		painter.lineTo( x + (5.0f * scale), y - (10.0f * scale));
		painter.closePath();
	}

	private void paintTremoloBar(TGLayout layout,TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x1 = fromX + (1.0f * scale);
		float x2 = x1 + (8.0f * scale);
		float y1 = fromY;
		float y2 = y1 + (9.0f * scale);
		layout.setTabEffectStyle(painter);
		painter.initPath();
		painter.moveTo(x1,y1);
		painter.lineTo(x1 + ( (x2 - x1) / 2 ),y2);
		painter.moveTo(x1 + ( (x2 - x1) / 2 ),y2);
		painter.lineTo(x2,y1);
		painter.closePath();
	}

	private void paintSlide(TGLayout layout,TGPainter painter,TGNoteImpl nextNote,int fromX,int fromY,int nextFromX){
		float xScale = layout.getScale();
		float yScale = (layout.getStringSpacing() / 10.0f);
		float yMove = (3.0f * yScale);
		float x = fromX;
		float y = fromY;
		layout.setTabEffectStyle(painter);
		if(nextNote != null){
			float nextX = nextNote.getPosX() + nextFromX + nextNote.getBeatImpl().getSpacing();
			float nextY = y;

			if(nextNote.getValue() < getValue()){
				y -= yMove;
				nextY += yMove;
			}else if(nextNote.getValue() > getValue()){
				y += yMove;
				nextY -= yMove;
			}else{
				y -= yMove;
				nextY -= yMove;
			}
			painter.initPath();
			painter.moveTo(x + (6.0f * xScale),y);
			painter.lineTo(nextX - (3.0f * xScale),nextY);
			painter.closePath();
		}else{
			painter.initPath();
			painter.moveTo(x + (6.0f * xScale),y - yMove);
			painter.lineTo(x + (17.0f * xScale),y - yMove);
			painter.closePath();
		}
	}

	private void paintHammer(TGLayout layout,TGPainter painter,TGNoteImpl nextNote,int fromX,int fromY,int nextFromX){
		float xScale = layout.getScale();
		float yScale = (layout.getStringSpacing() / 10.0f);

		float x = (fromX + (7.0f * xScale));
		float y = (fromY - (5.0f * yScale));

		float width = (nextNote != null)?( (nextNote.getPosX() + nextFromX + nextNote.getBeatImpl().getSpacing()) - (4.0f * xScale) - x ):(10.0f * xScale);
		float height = (15.0f * yScale);
		layout.setTabEffectStyle(painter);
		painter.initPath();
		painter.addArc(x,y, width, height, 45,90);
		painter.closePath();
	}

	private void paintGrace(TGLayout layout, TGPainter painter,int fromX,int fromY){
		float scale = ( layout.getScoreLineSpacing() / 2.25f );

		float x = fromX - (2f * scale);
		float y = fromY + (scale / 3);

		layout.setScoreEffectStyle(painter);
		painter.initPath(TGPainter.PATH_FILL);
		TGNotePainter.paintFooter(painter,x, y , -1 , scale);
		painter.closePath();

		painter.initPath();
		painter.moveTo(x, y - (2f * scale));
		painter.lineTo(x, y + (2f * scale) - (scale / 4f));
		painter.closePath();

		painter.initPath(TGPainter.PATH_DRAW | TGPainter.PATH_FILL);
		TGNotePainter.paintNote(painter,x - scale*1.33f, y + scale + (scale / 4f),  scale);
		painter.closePath();

		painter.initPath();
		painter.moveTo(x - scale, y );
		painter.lineTo(x + scale, y - scale);
		painter.closePath();
	}

	private void paintVibrato(TGLayout layout, TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x = fromX;
		float y = fromY + (2.0f * scale);
		float width = ( getVoiceImpl().getWidth() - (2.0f * scale) );

		int loops = (int)(width / (6.0f * scale) );
		if(loops > 0 ){
			layout.setTabEffectStyle(painter);
			painter.initPath(TGPainter.PATH_FILL);
			painter.moveTo(( x + ((2.0f) * scale) ),( y + (1.0f * scale) ));
			for( int i = 0; i < loops ; i ++ ){
				x = (fromX + ( (6.0f * scale) * i ) );
				painter.lineTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ));
				painter.cubicTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ),( x + (3.0f * scale) ), y ,( x + (4.0f * scale) ),( y + (1.0f * scale) ));
				painter.lineTo(( x + (6.0f * scale) ),( y + (3.0f * scale) ));
			}

			painter.lineTo(( x + (7.0f * scale) ),( y + (2.0f * scale) ));
			painter.cubicTo(( x + (7.0f * scale) ),( y + (2.0f * scale) ),( x + (8.0f * scale) ),( y + (2.0f * scale) ),( x + (7.0f * scale) ),( y + (3.0f * scale) ));

			for( int i = (loops - 1); i >= 0 ; i -- ){
				x = (fromX + ( (6.0f * scale) * i ) );
				painter.lineTo(( x + (6.0f * scale) ),( y + (4.0f * scale) ));
				painter.cubicTo(( x + (6.0f * scale) ),( y + (4.0f * scale) ),( x + (5.0f * scale) ),( y + (5.0f * scale) ),( x + (4.0f * scale) ),( y + (4.0f * scale) ));
				painter.lineTo(( x + (2.0f * scale) ),( y + (2.0f * scale) ));
				painter.lineTo(( x + (1.0f * scale) ),( y + (3.0f * scale) ));
			}
			painter.cubicTo(( x + (1.0f * scale) ),( y + (3.0f * scale) ), x ,( y + (3.0f * scale) ),( x + (1.0f * scale) ),( y + (2.0f * scale) ));
			painter.lineTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ));
			painter.closePath();
		}
	}

	private void paintTrill(TGLayout layout, TGPainter painter,int fromX,int fromY){
		String string = "Tr";
		int fmWidth = painter.getFMWidth( string );
		int fmHeight = painter.getFMHeight();
		float scale = layout.getScale();
		float x = fromX + fmWidth;
		float y = fromY + ( (fmHeight - (5.0f * scale)) / 2.0f );
		float width = ( getVoiceImpl().getWidth() - fmWidth - (2.0f * scale) );

		int loops = (int)(width / (6.0f * scale) );
		if(loops > 0 ){
			painter.drawString(string, fromX, fromY);

			layout.setTabEffectStyle(painter);
			painter.initPath(TGPainter.PATH_FILL);
			painter.moveTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ));
			for( int i = 0; i < loops ; i ++ ){
				x = (fromX + fmWidth + ( (6.0f * scale) * i ) );
				painter.lineTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ));
				painter.cubicTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ),( x + (3.0f * scale) ), y ,( x + (4.0f * scale) ),( y + (1.0f * scale) ));
				painter.lineTo(( x + (6.0f * scale) ),( y + (3.0f * scale) ));
			}

			painter.lineTo(( x + (7.0f * scale) ),( y + (2.0f * scale) ));
			painter.cubicTo(( x + (7.0f * scale) ),( y + (2.0f * scale) ),( x + (8.0f * scale) ),( y + (2.0f * scale) ),( x + (7.0f * scale) ),( y + (3.0f * scale) ));

			for( int i = (loops - 1); i >= 0 ; i -- ){
				x = (fromX + fmWidth + ( (6.0f * scale) * i ) );
				painter.lineTo(( x + (6.0f * scale) ),( y + (4.0f * scale) ));
				painter.cubicTo(( x + (6.0f * scale) ),( y + (4.0f * scale) ),( x + (5.0f * scale) ),( y + (5.0f * scale) ),( x + (4.0f * scale) ),( y + (4.0f * scale) ));
				painter.lineTo(( x + (2.0f * scale) ),( y + (2.0f * scale) ));
				painter.lineTo(( x + (1.0f * scale) ),( y + (3.0f * scale) ));
			}
			painter.cubicTo(( x + (1.0f * scale) ),( y + (3.0f * scale) ), x ,( y + (3.0f * scale) ),( x + (1.0f * scale) ),( y + (2.0f * scale) ));
			painter.lineTo(( x + (2.0f * scale) ),( y + (1.0f * scale) ));
			painter.closePath();
		}
	}

	private void paintFadeIn(TGLayout layout, TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x = fromX;
		float y = fromY + (4.0f * scale );
		float width = getVoiceImpl().getWidth();
		painter.initPath();
		painter.moveTo ( x , y );
		painter.cubicTo( x , y , x + width, y,  x + width, y - (4.0f * scale ));
		painter.moveTo ( x , y );
		painter.cubicTo( x , y , x + width, y,  x + width, y + (4.0f * scale ));
		painter.moveTo ( x + width, y + (4.0f * scale ) );
		painter.closePath();
	}

	private void paintAccentuated(TGLayout layout, TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x = fromX;
		float y = fromY + (2f * scale );
		painter.initPath();
		painter.moveTo( x , y );
		painter.lineTo( x + (6.0f * scale ) , y + (2.5f * scale ));
		painter.moveTo( x + (6.0f * scale ) , y + (2.5f * scale ));
		painter.lineTo( x , y + (5.0f * scale ));
		painter.closePath();
	}

	private void paintHeavyAccentuated(TGLayout layout, TGPainter painter,int fromX,int fromY){
		float scale = layout.getScale();
		float x = fromX;
		float y = fromY;
		painter.initPath();
		painter.moveTo( x , y + (5.0f * scale ) );
		painter.lineTo( x + (3.0f * scale ) , y + (1.0f * scale ));
		painter.moveTo( x + (3.0f * scale ) , y + (1.0f * scale ));
		painter.lineTo( x + (6.0f * scale ) , y + (5.0f * scale ) );
		painter.closePath();
	}

	public int getRealValue(){
		return (getValue() + getMeasureImpl().getTrack().getString(getString()).getValue());
	}

	public int getScorePosY() {
		return this.scorePosY;
	}

	public int getTabPosY() {
		return this.tabPosY;
	}

	public TGMeasureImpl getMeasureImpl(){
		return getBeatImpl().getMeasureImpl();
	}

	public int getPaintPosition(int index){
		return getMeasureImpl().getTs().getPosition(index);
	}

	public TGBeatImpl getBeatImpl() {
		return getVoiceImpl().getBeatImpl();
	}

	public TGVoiceImpl getVoiceImpl() {
		return (TGVoiceImpl)super.getVoice();
	}

	public int getPosX() {
		return getBeatImpl().getPosX();
	}
}