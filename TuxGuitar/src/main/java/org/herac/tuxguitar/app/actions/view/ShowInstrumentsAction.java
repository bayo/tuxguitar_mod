/*
 * Created on 17-dic-2005
 */
package org.herac.tuxguitar.app.actions.view;

import org.herac.tuxguitar.app.TuxGuitar;
import org.herac.tuxguitar.app.actions.Action;
import org.herac.tuxguitar.app.actions.ActionData;

/**
 * @author julian
 */
public class ShowInstrumentsAction extends Action{

	public static final String NAME = "action.view.show-instruments";

	public ShowInstrumentsAction() {
		super(NAME, AUTO_LOCK | AUTO_UNLOCK | AUTO_UPDATE | KEY_BINDING_AVAILABLE);
	}

	@Override
	protected int execute(ActionData actionData){
		if(TuxGuitar.instance().getChannelManager().isDisposed()){
			TuxGuitar.instance().getChannelManager().show();
		}else{
			TuxGuitar.instance().getChannelManager().dispose();
		}
		return 0;
	}
}
