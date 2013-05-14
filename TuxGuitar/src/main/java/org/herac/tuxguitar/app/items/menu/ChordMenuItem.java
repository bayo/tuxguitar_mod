package org.herac.tuxguitar.app.items.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.graphics.Rectangle;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.ToolItem;
import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.actions.ActionData;
import org.herac.tuxguitar.app.actions.insert.InsertChordAction;
import org.herac.tuxguitar.app.items.MenuItems;
import org.herac.tuxguitar.song.models.TGChord;

public class ChordMenuItem extends MenuItems{
	private MenuItem chordMenuItem;
	private Menu menu;
	private MenuItem insertChord;
	private MenuItem[] subMenuItems;
	
	private long lastEdit;
	
	public ChordMenuItem(Shell shell,Menu parent, int style) {
		this.chordMenuItem = new MenuItem(parent, style);
		this.menu = new Menu(shell, SWT.DROP_DOWN);
	}
	
	@Override
	public void showItems() {
		//--INSERT CHORD--
		this.insertChord = new MenuItem(this.menu, SWT.PUSH);
		this.insertChord.addSelectionListener(TuxGuitar.instance().getAction(InsertChordAction.NAME));
		//--SEPARATOR--
		new MenuItem(this.menu, SWT.SEPARATOR);
		//--CUSTOM CHORDS--
		this.addItems();
		
		this.chordMenuItem.setMenu(this.menu);
		
		this.loadIcons();
		this.loadProperties();
	}
	
	public void addItems() {
		this.disposeItems();
		this.subMenuItems = new MenuItem[TuxGuitar.instance().getCustomChordManager().countChords()];
		for(int i = 0;i < this.subMenuItems.length; i++){
			TGChord chord = TuxGuitar.instance().getCustomChordManager().getChord(i);
			ActionData actionData = new ActionData();
			actionData.put(InsertChordAction.PROPERTY_CHORD, chord);
			
			this.subMenuItems[i] = new MenuItem(this.menu, SWT.PUSH);
			this.subMenuItems[i].setData(actionData);
			this.subMenuItems[i].setText(chord.getName());
			this.subMenuItems[i].addSelectionListener(TuxGuitar.instance().getAction(InsertChordAction.NAME));
		}
	}
	
	public void disposeItems() {
		if(this.subMenuItems != null){
			for(int i = 0;i < this.subMenuItems.length; i++){
				this.subMenuItems[i].dispose();
			}
		}
	}
	
	public void widgetSelected(SelectionEvent event) {
		if (event.detail == SWT.ARROW && this.subMenuItems != null && this.subMenuItems.length > 0) {
			ToolItem item = (ToolItem) event.widget;
			Rectangle rect = item.getBounds();
			Point pt = item.getParent().toDisplay(new Point(rect.x, rect.y));
			this.menu.setLocation(pt.x, pt.y + rect.height);
			this.menu.setVisible(true);
		}else{
			TuxGuitar.instance().getAction(InsertChordAction.NAME).process(new ActionData());
		}
	}
	
	@Override
	public void update(){
		boolean running = TuxGuitar.instance().getPlayer().isRunning();
		if(this.lastEdit != TuxGuitar.instance().getCustomChordManager().getLastEdit()){
			this.addItems();
			this.lastEdit = TuxGuitar.instance().getCustomChordManager().getLastEdit();
		}
		this.insertChord.setEnabled(!running);
		for(int i = 0;i < this.subMenuItems.length; i++){
			this.subMenuItems[i].setEnabled(!running);
		}
	}
	
	@Override
	public void loadProperties() {
		setMenuItemTextAndAccelerator(this.chordMenuItem, "chord", null);
		setMenuItemTextAndAccelerator(this.insertChord, "insert.chord", InsertChordAction.NAME);
	}
	
	public void loadIcons() {
		//Nothing to do
	}
}
