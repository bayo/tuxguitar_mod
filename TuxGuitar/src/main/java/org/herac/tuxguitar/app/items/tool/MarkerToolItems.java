/*
 * Created on 02-dic-2005
 */
package org.herac.tuxguitar.app.items.tool;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.ToolBar;
import org.eclipse.swt.widgets.ToolItem;
import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.actions.marker.AddMarkerAction;
import org.herac.tuxguitar.app.actions.marker.GoFirstMarkerAction;
import org.herac.tuxguitar.app.actions.marker.GoLastMarkerAction;
import org.herac.tuxguitar.app.actions.marker.GoNextMarkerAction;
import org.herac.tuxguitar.app.actions.marker.GoPreviousMarkerAction;
import org.herac.tuxguitar.app.actions.marker.ListMarkersAction;
import org.herac.tuxguitar.app.items.ToolItems;
/**
 * @author julian
 */
public class MarkerToolItems  extends ToolItems{
	public static final String NAME = "marker.items";

	private ToolItem add;
	private ToolItem list;
	private ToolItem first;
	private ToolItem last;
	private ToolItem previous;
	private ToolItem next;

	public MarkerToolItems(){
		super(NAME);
	}

	@Override
	public void showItems(ToolBar toolBar){
		//--ADD--
		this.add = new ToolItem(toolBar, SWT.PUSH);
		this.add.addSelectionListener(TuxGuitar.instance().getAction(AddMarkerAction.NAME));

		//--LIST--
		this.list = new ToolItem(toolBar, SWT.PUSH);
		this.list.addSelectionListener(TuxGuitar.instance().getAction(ListMarkersAction.NAME));

		//--SEPARATOR--
		new ToolItem(toolBar, SWT.SEPARATOR);

		//--FIRST--
		this.first = new ToolItem(toolBar, SWT.PUSH);
		this.first.addSelectionListener(TuxGuitar.instance().getAction(GoFirstMarkerAction.NAME));

		//--PREVIOUS--
		this.previous = new ToolItem(toolBar, SWT.PUSH);
		this.previous.addSelectionListener(TuxGuitar.instance().getAction(GoPreviousMarkerAction.NAME));

		//--PREVIOUS--
		this.next = new ToolItem(toolBar, SWT.PUSH);
		this.next.addSelectionListener(TuxGuitar.instance().getAction(GoNextMarkerAction.NAME));

		//--LAST--
		this.last = new ToolItem(toolBar, SWT.PUSH);
		this.last.addSelectionListener(TuxGuitar.instance().getAction(GoLastMarkerAction.NAME));

		this.loadIcons();
		this.loadProperties();
	}

	@Override
	public void update(){
		//Nothing to do
	}

	@Override
	public void loadProperties(){
		this.add.setToolTipText(TuxGuitar.getProperty("marker.add"));
		this.list.setToolTipText(TuxGuitar.getProperty("marker.list"));
		this.first.setToolTipText(TuxGuitar.getProperty("marker.first"));
		this.last.setToolTipText(TuxGuitar.getProperty("marker.last"));
		this.previous.setToolTipText(TuxGuitar.getProperty("marker.previous"));
		this.next.setToolTipText(TuxGuitar.getProperty("marker.next"));
	}

	public void loadIcons(){
		this.add.setImage(TuxGuitar.instance().getIconManager().getMarkerAdd());
		this.list.setImage(TuxGuitar.instance().getIconManager().getMarkerList());
		this.first.setImage(TuxGuitar.instance().getIconManager().getMarkerFirst());
		this.previous.setImage(TuxGuitar.instance().getIconManager().getMarkerPrevious());
		this.next.setImage(TuxGuitar.instance().getIconManager().getMarkerNext());
		this.last.setImage(TuxGuitar.instance().getIconManager().getMarkerLast());
	}
}
