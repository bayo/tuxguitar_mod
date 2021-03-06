/*
 * Created on 02-dic-2005
 */
package org.herac.tuxguitar.app.items.menu;

import org.eclipse.swt.SWT;
import org.eclipse.swt.dnd.TransferData;
import org.eclipse.swt.widgets.Menu;
import org.eclipse.swt.widgets.MenuItem;
import org.eclipse.swt.widgets.Shell;
import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.actions.measure.AddMeasureAction;
import org.herac.tuxguitar.app.actions.measure.CleanMeasureAction;
import org.herac.tuxguitar.app.actions.measure.CopyMeasureAction;
import org.herac.tuxguitar.app.actions.measure.GoFirstMeasureAction;
import org.herac.tuxguitar.app.actions.measure.GoLastMeasureAction;
import org.herac.tuxguitar.app.actions.measure.GoNextMeasureAction;
import org.herac.tuxguitar.app.actions.measure.GoPreviousMeasureAction;
import org.herac.tuxguitar.app.actions.measure.PasteMeasureAction;
import org.herac.tuxguitar.app.actions.measure.RemoveMeasureAction;
import org.herac.tuxguitar.app.clipboard.MeasureTransfer;
import org.herac.tuxguitar.app.items.MenuItems;
import org.herac.tuxguitar.graphics.control.TGMeasureImpl;

/**
 * @author julian
 */
public class MeasureMenuItem extends MenuItems{

	private final MenuItem measureMenuItem;
	private final Menu menu;
	private MenuItem first;
	private MenuItem last;
	private MenuItem next;
	private MenuItem previous;
	private MenuItem addMeasure;
	private MenuItem cleanMeasure;
	private MenuItem removeMeasure;
	private MenuItem copyMeasure;
	private MenuItem pasteMeasure;

	public MeasureMenuItem(Shell shell,Menu parent, int style) {
		this.measureMenuItem = new MenuItem(parent, style);
		this.menu = new Menu(shell, SWT.DROP_DOWN);
	}

	@Override
	public void showItems(){
		//--first--
		this.first = new MenuItem(this.menu, SWT.PUSH);
		this.first.addSelectionListener(TuxGuitar.instance().getAction(GoFirstMeasureAction.NAME));
		//--previous--
		this.previous = new MenuItem(this.menu, SWT.PUSH);
		this.previous.addSelectionListener(TuxGuitar.instance().getAction(GoPreviousMeasureAction.NAME));
		//--next--
		this.next = new MenuItem(this.menu, SWT.PUSH);
		this.next.addSelectionListener(TuxGuitar.instance().getAction(GoNextMeasureAction.NAME));
		//--last--
		this.last = new MenuItem(this.menu, SWT.PUSH);
		this.last.addSelectionListener(TuxGuitar.instance().getAction(GoLastMeasureAction.NAME));

		//--SEPARATOR
		new MenuItem(this.menu, SWT.SEPARATOR);
		//--add--
		this.addMeasure = new MenuItem(this.menu, SWT.PUSH);
		this.addMeasure.addSelectionListener(TuxGuitar.instance().getAction(AddMeasureAction.NAME));
		//--clean--
		this.cleanMeasure = new MenuItem(this.menu, SWT.PUSH);
		this.cleanMeasure.addSelectionListener(TuxGuitar.instance().getAction(CleanMeasureAction.NAME));
		//--remove--
		this.removeMeasure = new MenuItem(this.menu, SWT.PUSH);
		this.removeMeasure.addSelectionListener(TuxGuitar.instance().getAction(RemoveMeasureAction.NAME));

		//--SEPARATOR
		new MenuItem(this.menu, SWT.SEPARATOR);
		//--copy--
		this.copyMeasure = new MenuItem(this.menu, SWT.PUSH);
		this.copyMeasure.addSelectionListener(TuxGuitar.instance().getAction(CopyMeasureAction.NAME));
		//--paste--
		this.pasteMeasure = new MenuItem(this.menu, SWT.PUSH);

		this.pasteMeasure.addSelectionListener(TuxGuitar.instance().getAction(PasteMeasureAction.NAME));

		this.measureMenuItem.setMenu(this.menu);

		this.loadIcons();
		this.loadProperties();
	}

	@Override
	public void update(){
		TGMeasureImpl measure = TuxGuitar.instance().getTablatureEditor().getTablature().getCaret().getMeasure();
		boolean running = TuxGuitar.instance().getPlayer().isRunning();
		boolean isFirst = (measure.getNumber() == 1);
		boolean isLast = (measure.getNumber() == measure.getTrack().countMeasures());
		this.first.setEnabled(!isFirst);
		this.previous.setEnabled(!isFirst);
		this.next.setEnabled(!isLast);
		this.last.setEnabled(!isLast);
		this.addMeasure.setEnabled(!running);
		this.cleanMeasure.setEnabled(!running);
		this.removeMeasure.setEnabled(!running);
		this.copyMeasure.setEnabled(!running);

		TransferData[] availableTypes = TuxGuitar.instance().getClipBoard().getAvailableTypes();
		boolean hasTransferable = false;
		for(TransferData type: availableTypes) {
			if (MeasureTransfer.getInstance().isSupportedType(type)) {
				hasTransferable = true;
			}
		}
		this.pasteMeasure.setEnabled(!running && hasTransferable);
	}

	@Override
	public void loadProperties(){
		setMenuItemTextAndAccelerator(this.measureMenuItem, "measure", null);
		setMenuItemTextAndAccelerator(this.first, "measure.first", GoFirstMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.last, "measure.last", GoLastMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.previous, "measure.previous", GoPreviousMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.next, "measure.next", GoNextMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.addMeasure, "measure.add", AddMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.cleanMeasure, "measure.clean", CleanMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.removeMeasure, "measure.remove", RemoveMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.copyMeasure, "measure.copy", CopyMeasureAction.NAME);
		setMenuItemTextAndAccelerator(this.pasteMeasure, "measure.paste", PasteMeasureAction.NAME);
	}

	public void loadIcons(){
		//Nothing to do
	}
}
