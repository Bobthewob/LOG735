/*******************************************************
 * Cours :        LOG735-E17 Groupe 01
 * Projet :       Laboratoire #2
 * Etudiants :    Philippe Rhéaume RHEP11089407
 *                Joey Roger ROGJ13039302
 *                Catherine Boivin BOIC19518909
 *******************************************************/
package events;

public class EventForPartTwoSync extends EventBase implements IEventSynchronized, IPartTwoEvent{

	private static final long serialVersionUID = -6784739853826767178L;

	public EventForPartTwoSync(String m){
		super(m);
	}
}