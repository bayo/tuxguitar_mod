package org.herac.tuxguitar.app.widgets;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.ColorDialog;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;
import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.actions.ActionLock;
import org.herac.tuxguitar.app.editors.TGUpdateListener;
import org.herac.tuxguitar.app.helper.SyncThread;
import org.herac.tuxguitar.app.undo.undoables.UndoableJoined;
import org.herac.tuxguitar.app.undo.undoables.track.UndoableTrackGeneric;
import org.herac.tuxguitar.app.undo.undoables.track.UndoableTrackInfo;
import org.herac.tuxguitar.app.undo.undoables.track.UndoableTrackInstrument;
import org.herac.tuxguitar.app.util.TGMusicKeyUtils;
import org.herac.tuxguitar.graphics.control.TGTrackImpl;
import org.herac.tuxguitar.song.managers.TGSongManager;
import org.herac.tuxguitar.song.models.TGChannel;
import org.herac.tuxguitar.song.models.TGColor;
import org.herac.tuxguitar.song.models.TGString;
import org.herac.tuxguitar.song.models.TGTrack;
import org.herac.tuxguitar.util.TGSynchronizer;

/**
 * @author julian
 * 
 * TODO To change the template for this generated type comment go to Window - Preferences - Java - Code Style - Code Templates
 */
public class TrackPropertiesDialog extends Shell implements TGUpdateListener {

	private static final String[] NOTE_NAMES = TGMusicKeyUtils.getSharpKeyNames(TGMusicKeyUtils.PREFIX_TUNING);
	private static final int MINIMUM_LEFT_CONTROLS_WIDTH = 180;
	private static final int MINIMUM_BUTTON_WIDTH = 80;
	private static final int MINIMUM_BUTTON_HEIGHT = 25;
	private static final int MAX_STRINGS = 7;
	private static final int MIN_STRINGS = 4;
	private static final int MAX_OCTAVES = 10;
	private static final int MAX_NOTES = 12;

	private Text nameText;
	private final TGColor trackColor;
	private List<TGString> tempStrings;
	private Button stringTransposition;
	private Button stringTranspositionTryKeepString;
	private Button stringTranspositionApplyToChords;
	private Spinner stringCountSpinner;
	private final Combo[] stringCombos = new Combo[MAX_STRINGS];
	private Combo offsetCombo;
	private int stringCount;
	private Combo instrumentCombo;
	private boolean percussionChannel;

	TGTrackImpl selectedTrack;

	public TrackPropertiesDialog(Shell shell, TGTrackImpl track) {
		super(shell, SWT.DIALOG_TRIM | SWT.APPLICATION_MODAL);

		if (track == null) {
			throw new IllegalArgumentException("Initialized track expected");
		}

		selectedTrack = track;

		this.stringCount = track.getStrings().size();
		this.trackColor = track.getColor().clone(TuxGuitar.instance().getSongManager().getFactory());
		this.percussionChannel = TuxGuitar.instance().getSongManager().isPercussionChannel(track.getChannelId());
		this.initTempStrings(track.getStrings());

		this.setLayout(new GridLayout(2,false));
		this.setText(TuxGuitar.getProperty("track.properties"));

		this.addListeners();

		Composite left = new Composite(this,SWT.NONE);
		left.setLayout(new GridLayout());
		left.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		Composite right = new Composite(this,SWT.NONE);
		right.setLayout(new GridLayout());
		right.setLayoutData(new GridData(SWT.FILL,SWT.FILL,true,true));

		Composite bottom = new Composite(this, SWT.NONE);
		bottom.setLayout(new GridLayout(2,false));
		bottom.setLayoutData(new GridData(SWT.END,SWT.FILL,true,true,2,1));

		//GENERAL
		initTrackInfo(makeGroup(left,1,TuxGuitar.getProperty("track.properties.general")), track);

		//INSTRUMENT
		initInstrumentFields(makeGroup(left,1,TuxGuitar.getProperty("instrument")), track);

		//TUNING
		initTuningInfo(makeGroup(right,2,TuxGuitar.getProperty("tuning")), track);

		//BUTTONS
		initButtons(bottom);

		updateTuningGroup(!this.percussionChannel);
	}

	@Override
	protected void checkSubclass() {
		// Allow to inherit SWT components... I would like to cook OOP thanks
	}

	private Group makeGroup(Composite parent,int horizontalSpan,String text){
		Group group = new Group(parent, SWT.SHADOW_ETCHED_IN);
		group.setLayoutData(makeGridData(horizontalSpan));
		group.setText(text);

		return group;
	}

	private GridData makeGridData(int horizontalSpan){
		GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
		data.horizontalSpan = horizontalSpan;
		return data;
	}

	private GridData getButtonsData(){
		GridData data = new GridData(SWT.FILL,SWT.FILL,true,true);
		data.minimumWidth = MINIMUM_BUTTON_WIDTH;
		data.minimumHeight = MINIMUM_BUTTON_HEIGHT;
		return data;
	}

	private void initTrackInfo(Composite composite,TGTrackImpl track) {
		composite.setLayout(new GridLayout(/*2,false*/));
		Composite top = new Composite(composite, SWT.NONE);
		top.setLayout(new GridLayout(/*2,false*/));
		top.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));
		Composite bottom = new Composite(composite, SWT.NONE);
		bottom.setLayout(new GridLayout(/*2,false*/));
		bottom.setLayoutData(new GridData(SWT.FILL,SWT.BOTTOM,true,true));

		//-----------------------NAME---------------------------------
		Label nameLabel = new Label(top, SWT.NONE);
		nameLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));
		nameLabel.setText(TuxGuitar.getProperty("track.name") + ":");

		this.nameText = new Text(top, SWT.BORDER);
		this.nameText.setLayoutData(getAlignmentData(MINIMUM_LEFT_CONTROLS_WIDTH,SWT.FILL));
		this.nameText.setText(track.getName());

		//-----------------------COLOR---------------------------------
		Label colorLabel = new Label(bottom, SWT.NONE);
		colorLabel.setText(TuxGuitar.getProperty("track.color") + ":");
		colorLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true));

		final Button colorButton = new Button(bottom, SWT.PUSH);
		colorButton.setLayoutData(getAlignmentData(MINIMUM_LEFT_CONTROLS_WIDTH,SWT.FILL));
		colorButton.setAlignment(SWT.RIGHT);
		colorButton.setText(TuxGuitar.getProperty("choose"));
		colorButton.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent event) {
				ColorDialog dlg = new ColorDialog(TrackPropertiesDialog.this);
				dlg.setRGB(new RGB(trackColor.getR(), trackColor.getG(), trackColor.getB()));
				dlg.setText(TuxGuitar.getProperty("choose-color"));
				RGB rgb = dlg.open();
				if (rgb != null) {
					TrackPropertiesDialog.this.trackColor.setR(rgb.red);
					TrackPropertiesDialog.this.trackColor.setG(rgb.green);
					TrackPropertiesDialog.this.trackColor.setB(rgb.blue);
					TrackPropertiesDialog.this.setButtonColor(colorButton);
				}
			}
		});
		colorButton.addDisposeListener(new DisposeListener() {
			@Override
			public void widgetDisposed(DisposeEvent e) {
				TrackPropertiesDialog.this.disposeButtonColor(colorButton);
			}
		});
		this.setButtonColor(colorButton);
	}

	private void initTuningInfo(Composite composite,TGTrackImpl track) {
		composite.setLayout(new GridLayout(2,false));
		initTuningData(composite,track);
		initTuningCombos(composite);
	}

	private void initTuningCombos(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(/*MAX_STRINGS, false*/));
		composite.setLayoutData(new GridData(SWT.RIGHT,SWT.FILL,false,true));
		String[] tuningTexts = getAllValueNames();
		for (int i = 0; i < MAX_STRINGS; i++) {
			this.stringCombos[i] = new Combo(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
			this.stringCombos[i].setItems(tuningTexts);
		}
	}

	private void initTuningData(Composite parent,TGTrackImpl track) {
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout());
		composite.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));

		Composite top = new Composite(composite, SWT.NONE);
		top.setLayout(new GridLayout());
		top.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));

		Composite middle = new Composite(composite, SWT.NONE);
		middle.setLayout(new GridLayout());
		middle.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));

		Composite bottom = new Composite( composite , SWT.NONE );
		bottom.setLayout( new GridLayout() );
		bottom.setLayoutData( new GridData(SWT.FILL,SWT.TOP,true,true) );

		//---------------------------------STRING--------------------------------
		Label stringCountLabel = new Label(top, SWT.NONE);
		stringCountLabel.setText(TuxGuitar.getProperty("tuning.strings") + ":");
		stringCountLabel.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true));

		this.stringCountSpinner = new Spinner(top, SWT.BORDER);
		this.stringCountSpinner.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));
		this.stringCountSpinner.setMinimum(MIN_STRINGS);
		this.stringCountSpinner.setMaximum(MAX_STRINGS);
		this.stringCountSpinner.setSelection(this.stringCount);
		this.stringCountSpinner.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				TrackPropertiesDialog.this.stringCount = TrackPropertiesDialog.this.stringCountSpinner.getSelection();
				setDefaultTuning(TrackPropertiesDialog.this.percussionChannel);
				updateTuningGroup(!TrackPropertiesDialog.this.percussionChannel);
			}
		});

		//---------------------------------OFFSET--------------------------------
		Label offsetLabel = new Label(middle, SWT.NONE);
		offsetLabel.setText(TuxGuitar.getProperty("tuning.offset") + ":");
		offsetLabel.setLayoutData(new GridData(SWT.LEFT,SWT.CENTER,true,true));

		this.offsetCombo = new Combo(middle, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.offsetCombo.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));
		for(int i = TGTrack.MIN_OFFSET;i <= TGTrack.MAX_OFFSET;i ++){
			this.offsetCombo.add(Integer.toString(i));
			if(i == track.getOffset()){
				this.offsetCombo.select(i - TGTrack.MIN_OFFSET);
			}
		}

		//---------------------------------OPTIONS----------------------------------
		this.stringTransposition = new Button( bottom , SWT.CHECK );
		this.stringTransposition.setLayoutData( new GridData(SWT.FILL,SWT.CENTER,true,true) );
		this.stringTransposition.setText(TuxGuitar.getProperty("tuning.strings.transpose"));
		this.stringTransposition.setSelection( true );

		this.stringTranspositionApplyToChords = new Button( bottom , SWT.CHECK );
		this.stringTranspositionApplyToChords.setLayoutData( new GridData(SWT.FILL,SWT.CENTER,true,true) );
		this.stringTranspositionApplyToChords.setText(TuxGuitar.getProperty("tuning.strings.transpose.apply-to-chords"));
		this.stringTranspositionApplyToChords.setSelection( true );

		this.stringTranspositionTryKeepString = new Button( bottom , SWT.CHECK );
		this.stringTranspositionTryKeepString.setLayoutData( new GridData(SWT.FILL,SWT.CENTER,true,true) );
		this.stringTranspositionTryKeepString.setText(TuxGuitar.getProperty("tuning.strings.transpose.try-keep-strings"));
		this.stringTranspositionTryKeepString.setSelection( true );

		this.stringTransposition.addSelectionListener( new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button stringTransposition = TrackPropertiesDialog.this.stringTransposition;
				Button stringTranspositionApplyToChords = TrackPropertiesDialog.this.stringTranspositionApplyToChords;
				Button stringTranspositionTryKeepString = TrackPropertiesDialog.this.stringTranspositionTryKeepString;
				stringTranspositionApplyToChords.setEnabled( ( stringTransposition.isEnabled() && stringTransposition.getSelection() ) );
				stringTranspositionTryKeepString.setEnabled( ( stringTransposition.isEnabled() && stringTransposition.getSelection() ) );
			}
		});
	}

	private GridData getAlignmentData(int minimumWidth,int horizontalAlignment){
		GridData data = new GridData();
		data.minimumWidth = minimumWidth;
		data.horizontalAlignment = horizontalAlignment;
		data.verticalAlignment = SWT.DEFAULT;
		data.grabExcessHorizontalSpace = true;
		data.grabExcessVerticalSpace = true;
		return data;
	}

	private void initButtons(final Composite parent) {
		Button buttonOK = new Button(parent, SWT.PUSH);
		buttonOK.setText(TuxGuitar.getProperty("ok"));
		buttonOK.setLayoutData(getButtonsData());
		buttonOK.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				updateTrackProperties();
				TrackPropertiesDialog.this.dispose();
			}
		});

		Button buttonCancel = new Button(parent, SWT.PUSH);
		buttonCancel.setText(TuxGuitar.getProperty("cancel"));
		buttonCancel.setLayoutData(getButtonsData());
		buttonCancel.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent arg0) {
				TrackPropertiesDialog.this.dispose();
			}
		});

		this.setDefaultButton( buttonOK );
	}

	private void initInstrumentFields(Composite composite,TGTrackImpl track) {
		composite.setLayout(new GridLayout());

		Composite top = new Composite(composite, SWT.NONE);
		top.setLayout(new GridLayout(2,false));
		top.setLayoutData(new GridData(SWT.FILL,SWT.TOP,true,true));

		//------------Instrument Combo-------------------------------------
		Label instrumentLabel = new Label(top, SWT.NONE);
		instrumentLabel.setText(TuxGuitar.getProperty("instrument") + ":");
		instrumentLabel.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,true,2,1));

		this.instrumentCombo = new Combo(top, SWT.DROP_DOWN | SWT.READ_ONLY);
		this.instrumentCombo.setLayoutData(new GridData(SWT.FILL,SWT.CENTER,true,false));
		this.instrumentCombo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				checkPercussionChannel();
			}
		});
		this.loadChannels(track.getChannelId());

		Button settings = new Button(top, SWT.PUSH);
		settings.setImage(TuxGuitar.instance().getIconManager().getSettings());
		settings.setToolTipText(TuxGuitar.getProperty("settings"));
		settings.setLayoutData(new GridData(SWT.RIGHT,SWT.CENTER,false,false));
		settings.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if(!TuxGuitar.instance().getChannelManager().isDisposed()){
					TuxGuitar.instance().getChannelManager().dispose();
				}
				TuxGuitar.instance().getChannelManager().show(TrackPropertiesDialog.this);
			}
		});
	}

	private void loadChannels(int selectedChannelId){
		List<Integer> tgChannelsData = new ArrayList<Integer>();
		List<TGChannel> tgChannelsAvailable = getSongManager().getChannels();

		Combo tgChannelsCombo = this.instrumentCombo;
		tgChannelsCombo.removeAll();
		tgChannelsCombo.add(TuxGuitar.getProperty("track.instrument.default-select-option"));
		tgChannelsCombo.select(0);
		tgChannelsData.add(new Integer(-1));

		for( int i = 0 ; i < tgChannelsAvailable.size() ; i ++) {
			TGChannel tgChannel = tgChannelsAvailable.get(i);
			tgChannelsData.add(new Integer(tgChannel.getChannelId()));
			tgChannelsCombo.add(tgChannel.getName());

			if( tgChannel.getChannelId() == selectedChannelId ){
				tgChannelsCombo.select(tgChannelsCombo.getItemCount() - 1);
			}

		}
		tgChannelsCombo.setData(tgChannelsData);
	}

	private void reloadChannels(){
		loadChannels(getSelectedChannelId());
	}

	private void checkPercussionChannel(){
		boolean percussionChannel = getSongManager().isPercussionChannel( getSelectedChannelId() );
		if( this.percussionChannel != percussionChannel ){
			this.setDefaultTuning(percussionChannel);
			this.updateTuningGroup(!percussionChannel);
		}
		this.percussionChannel = percussionChannel;
	}

	private int getSelectedChannelId(){
		int index = this.instrumentCombo.getSelectionIndex();
		if( index >= 0 ){
			Object data = this.instrumentCombo.getData();
			if( data instanceof List ){
				List<?> list = (List<?>) data;
				if ( list.size() > index ) {
					return ((Integer)list.get(index)).intValue();
				}
			}
		}
		return -1;
	}

	private void updateTrackProperties() {
		final TGSongManager songManager = getSongManager();

		final TGTrackImpl track = selectedTrack;

		final String trackName = this.nameText.getText();

		final List<TGString> strings = new ArrayList<TGString>();
		for (int i = 0; i < this.stringCount; i++) {
			strings.add(TGSongManager.newString(getSongManager().getFactory(),(i + 1), this.stringCombos[i].getSelectionIndex()));
		}

		final int channelId = getSelectedChannelId();
		final int offset = ((songManager.isPercussionChannel(channelId))?0:TGTrack.MIN_OFFSET + this.offsetCombo.getSelectionIndex());


		final TGColor trackColor = this.trackColor;
		final boolean infoChanges = hasInfoChanges(track,trackName,trackColor,offset);
		final boolean tuningChanges = hasTuningChanges(track,strings);
		final boolean channelChanges = hasChannelChanges(track, channelId);
		final boolean transposeStrings = shouldTransposeStrings(track, channelId);
		final boolean transposeApplyToChords = (transposeStrings && this.stringTranspositionApplyToChords.getSelection());
		final boolean transposeTryKeepString = (transposeStrings && this.stringTranspositionTryKeepString.getSelection());

		try {
			if(infoChanges || tuningChanges || channelChanges){
				ActionLock.lock();
				TGSynchronizer.instance().runLater(new TGSynchronizer.TGRunnable() {
					@Override
					public void run() throws Throwable {
						TuxGuitar.instance().loadCursor(SWT.CURSOR_WAIT);
						new Thread( new Runnable() {
							@Override
							public void run() {
								TuxGuitar.instance().getFileHistory().setUnsavedFile();
								UndoableJoined undoable = new UndoableJoined();

								UndoableTrackGeneric undoableGeneric = null;
								if(tuningChanges){
									undoableGeneric = UndoableTrackGeneric.startUndo(track);
								}

								//--------------------------------------info---------------------------------------
								if(infoChanges){
									UndoableTrackInfo undoableInfo = null;
									if(!tuningChanges){
										undoableInfo = UndoableTrackInfo.startUndo(track);
									}
									songManager.getTrackManager().changeInfo(track,trackName,trackColor,offset);
									if(!tuningChanges && undoableInfo != null){
										undoable.addUndoableEdit(undoableInfo.endUndo(track));
									}
								}
								//--------------------------------------tuning---------------------------------------
								if(tuningChanges){
									updateTrackTunings(track, strings, transposeStrings, transposeTryKeepString, transposeApplyToChords);
								}
								//-----------------------------instrument----------------------------------------------
								if(channelChanges){
									UndoableTrackInstrument undoableInstrument = null;
									if(!tuningChanges){
										undoableInstrument = UndoableTrackInstrument.startUndo(track);
									}
									songManager.getTrackManager().changeChannel(track,channelId);
									if(!tuningChanges && undoableInstrument != null){
										undoable.addUndoableEdit(undoableInstrument.endUndo(track));
									}
								}
								if(tuningChanges && undoableGeneric != null){
									undoable.addUndoableEdit(undoableGeneric.endUndo(track));
								}
								TuxGuitar.instance().getUndoableManager().addEdit(undoable.endUndo());

								new SyncThread(new Runnable() {
									@Override
									public void run() {
										if(!TuxGuitar.isDisposed()){
											TuxGuitar.instance().fireUpdate();
											TuxGuitar.instance().updateCache( true );
											TuxGuitar.instance().loadCursor(SWT.CURSOR_ARROW);
											ActionLock.unlock();
										}
									}
								}).start();
							}
						}).start();
					}
				});
			}
		} catch (Throwable throwable) {
			TuxGuitar.instance().loadCursor(SWT.CURSOR_ARROW);
			ActionLock.unlock();
			throwable.printStackTrace();
		}
	}

	private boolean shouldTransposeStrings(TGTrackImpl track, int selectedChannelId){
		if(this.stringTransposition.getSelection()){
			boolean percussionChannelNew = getSongManager().isPercussionChannel(selectedChannelId);
			boolean percussionChannelOld = getSongManager().isPercussionChannel(track.getChannelId());

			return (!percussionChannelNew && !percussionChannelOld);
		}
		return false;
	}

	private boolean hasInfoChanges(TGTrackImpl track,String name,TGColor color,int offset){
		if(!name.equals(track.getName())){
			return true;
		}
		if(!color.isEqual(track.getColor())){
			return true;
		}
		if(offset != track.getOffset()){
			return true;
		}
		return false;
	}

	private boolean hasChannelChanges(TGTrackImpl track,int channelId){
		return ( track.getChannelId() != channelId );
	}

	private boolean hasTuningChanges(TGTrackImpl track,List<TGString> newStrings){
		List<TGString> oldStrings = track.getStrings();
		//check the number of strings
		if(oldStrings.size() != newStrings.size()){
			return true;
		}
		//check the tuning of strings
		for(int i = 0;i < oldStrings.size();i++){
			TGString oldString = oldStrings.get(i);
			boolean stringExists = false;
			for(int j = 0;j < newStrings.size();j++){
				TGString newString = newStrings.get(j);
				if(newString.isEqual(oldString)){
					stringExists = true;
				}
			}
			if(!stringExists){
				return true;
			}
		}
		return false;
	}

	private void updateTrackTunings(TGTrackImpl track, List<TGString> strings, boolean transposeStrings , boolean transposeTryKeepString , boolean transposeApplyToChords ){
		int[] transpositions = getStringTranspositions(track, strings);
		getSongManager().getTrackManager().changeInstrumentStrings(track,strings);
		if( transposeStrings ){
			getSongManager().getTrackManager().transposeNotes(track, transpositions, transposeTryKeepString, transposeApplyToChords );
		}
	}

	private int[] getStringTranspositions(TGTrackImpl track, List<TGString> newStrings ){
		int[] transpositions = new int[ newStrings.size() ];

		TGString newString = null;
		TGString oldString = null;
		for( int index = 0; index < transpositions.length ; index ++ ){
			for( int i = 0; i < track.stringCount() ; i ++ ){
				TGString string = track.getString( i + 1 );
				if( string.getNumber() == (index + 1) ){
					oldString = string;
					break;
				}
			}
			for( int i = 0; i < newStrings.size() ; i ++ ){
				TGString string = newStrings.get( i );
				if( string.getNumber() == (index + 1) ){
					newString = string;
					break;
				}
			}
			if( oldString != null && newString != null ){
				transpositions[ index ] = (oldString.getValue() - newString.getValue());
			}else{
				transpositions[ index ] = 0;
			}

			newString = null;
			oldString = null;
		}

		return transpositions;
	}

	private void setButtonColor(Button button){
		Color color = new Color(this.getDisplay(), this.trackColor.getR(), this.trackColor.getG(), this.trackColor.getB());

		final int size = 16;
		Image icon = new Image(TuxGuitar.instance().getDisplay(), size, size);
		GC gc = new GC(icon);
		gc.setBackground(color);
		gc.fillRectangle(0, 0, size, size);
		gc.dispose();
		color.dispose();

		this.disposeButtonColor(button);
		button.setImage(icon);
	}

	private void disposeButtonColor(Button button){
		Image icon = button.getImage();
		button.setImage(null);
		if(icon != null && !icon.isDisposed()){
			icon.dispose();
		}
	}

	private void updateTuningGroup(boolean enabled) {
		for (int i = 0; i < this.tempStrings.size(); i++) {
			TGString string = this.tempStrings.get(i);
			this.stringCombos[i].select(string.getValue());
			this.stringCombos[i].setVisible(true);
			this.stringCombos[i].setEnabled(enabled);
		}

		for (int i = this.tempStrings.size(); i < MAX_STRINGS; i++) {
			this.stringCombos[i].select(0);
			this.stringCombos[i].setVisible(false);
		}
		this.offsetCombo.setEnabled(enabled);
		this.stringTransposition.setEnabled(enabled);
		this.stringTranspositionApplyToChords.setEnabled(enabled && this.stringTransposition.getSelection());
		this.stringTranspositionTryKeepString.setEnabled(enabled && this.stringTransposition.getSelection());
	}

	private void initTempStrings(List<TGString> realStrings) {
		this.tempStrings = new ArrayList<TGString>();
		for (int i = 0; i < realStrings.size(); i++) {
			TGString realString = realStrings.get(i);
			this.tempStrings.add(realString.clone(getSongManager().getFactory()));
		}
	}

	private void setDefaultTuning( boolean percussionChannel ) {
		this.tempStrings.clear();
		if( percussionChannel ) {
			for (int i = 1; i <= this.stringCount; i++) {
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),i, 0));
			}
		}
		else {
			switch (this.stringCount) {
			case 7:
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),1, 64));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),2, 59));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),3, 55));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),4, 50));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),5, 45));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),6, 40));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),7, 35));
				break;
			case 6:
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),1, 64));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),2, 59));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),3, 55));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),4, 50));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),5, 45));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),6, 40));
				break;
			case 5:
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),1, 43));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),2, 38));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),3, 33));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),4, 28));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),5, 23));
				break;
			case 4:
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),1, 43));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),2, 38));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),3, 33));
				this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),4, 28));
				break;
			default:
				for( int i = 1 ; i <= this.stringCount ; i ++ ){
					this.tempStrings.add(TGSongManager.newString(getSongManager().getFactory(),i,0));
				}
				break;
			}
		}
	}

	private TGSongManager getSongManager() {
		return TuxGuitar.instance().getSongManager();
	}

	private String[] getAllValueNames() {
		String[] valueNames = new String[MAX_NOTES * MAX_OCTAVES];
		for (int i = 0; i < valueNames.length; i++) {
			valueNames[i] =  NOTE_NAMES[ (i -  ((i / MAX_NOTES) * MAX_NOTES) ) ] + (i / MAX_NOTES);
		}
		return valueNames;
	}

	private void addListeners(){
		TuxGuitar.instance().getEditorManager().addUpdateListener(this);
	}

	private void removeListeners(){
		TuxGuitar.instance().getEditorManager().removeUpdateListener(this);
	}

	private void updateItems(){
		if( this != null && !this.isDisposed() ){
			this.reloadChannels();
		}
	}

	@Override
	public void doUpdate(int type) {
		if( type == TGUpdateListener.SELECTION ){
			this.updateItems();
		}
	}

	@Override
	public void dispose() {
		removeListeners();
		super.dispose();
	}
}