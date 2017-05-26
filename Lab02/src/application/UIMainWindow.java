/******************************************************
	Cours :           LOG730
	Session :         �t� 2010
	Groupe :          01
	Projet :          Laboratoire #2
	Date cr�ation :   2010-05-21
******************************************************
Interface graphique des applications simul�es. MainPartOne,
MainPartTwo et MainPartThree instancient cette classe.

L'interface offre les fonctionnalit�s suivantes :
-Envoyer � App Un/Deux/Trois : envoie l'�v�nement associ�
 de l'application source � l'application de destination.
-Envoyer � Tous : envoie l'�v�nement associ�
 de l'application source aux deux autres applications.
-Envoi Synchronis� : envoie l'�v�nement qui doit �tre
synchronis� � toutes les applications.

NOTE : Seules les classes internes impl�mentant ActionListener
situ�es � la fin de la classe ont le potentiel de n�cessiter 
des modifications.
******************************************************/ 
package application;

import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;

import events.*;

public class UIMainWindow extends JFrame implements IObserver {

	private static final long serialVersionUID = 17889303454552887L;
	
	private int delay; //Temps artificiel de d�lai de traitement des �v�nements
	private String syncText; //Texte � afficher lors de l'�v�nement synchronis�
	
	private JList lstResultatEvent;
	private DefaultListModel model;
	private JScrollPane scrollPane;
	private IEventBusConnector eventBus;
	private String name;
	
	//Construit l'interface graphique.
	//Ne devrait pas �tre modifi�.
	public UIMainWindow(IEventBusConnector eventBusConn, String name, String syncText, int delay) {
		super();
		this.delay = delay;
		this.syncText = syncText;

		this.eventBus = eventBusConn;
		this.name = name;

		setSize(450,480);
		setDefaultCloseOperation(EXIT_ON_CLOSE);
		setTitle(name);
		getContentPane().setLayout(null);
		setResizable(false);

		model = new DefaultListModel(); 
		lstResultatEvent = new JList(model);
		scrollPane = new JScrollPane();
		JButton sendToPartOne = new JButton();
		JButton sendToPartTwo = new JButton();
		JButton sendToPartThree = new JButton();
		JButton sendToAll = new JButton();
		JButton sendSynchroToAll = new JButton();
		
		scrollPane.getViewport().setView(lstResultatEvent);
		sendToPartOne.setText("Envoyer � App Un");
		sendToPartTwo.setText("Envoyer � App Deux");
		sendToPartThree.setText("Envoyer � App Trois");
		sendToAll.setText("Envoyer � Tous");
		sendSynchroToAll.setText("Envoie Synchronis�");

		sendToPartOne.addActionListener(new PartOneActionListener(name, eventBusConn));
		sendToPartTwo.addActionListener(new PartTwoActionListener(name, eventBusConn));
		sendToPartThree.addActionListener(new PartThreeActionListener(name, eventBusConn));
		sendToAll.addActionListener(new AllActionListener(name, eventBusConn));
		sendSynchroToAll.addActionListener(new AllSynchroActionListener(name, eventBusConn));
		
		// Une couleur exag�r�e pour �tre s�r que tu comprennes 
		// que c'est le bouton important du laboratoire :)
		sendSynchroToAll.setBackground(Color.CYAN);

		sendToPartOne.setBounds(10, 10, 200, 30);
		sendToPartTwo.setBounds(10, 50, 200, 30);
		sendToPartThree.setBounds(10, 90, 200, 30);
		sendToAll.setBounds(220, 10, 200, 30);
		sendSynchroToAll.setBounds(220, 50, 200, 70);
		scrollPane.setBounds(10, 130, 420, 300);

		add(sendToPartOne);
		add(sendToPartTwo);
		add(sendToPartThree);
		add(sendToAll);
		add(sendSynchroToAll);
		add(scrollPane);
	}
	

	//Affichage du message contenu dans les �v�nements re�us
	//par utilisation du patron Observer.
	//Si l'�v�nement est de type IEventSynchronized,
	//affiche le texte contenu dans syncText.
	
	public void update(Object o, Object arg) {
		
		System.out.println("R�ception de l'�v�nement: " + arg.toString());
		IEvent event = (IEvent)arg;
		try {
			Thread.sleep(1000*delay);
		}
		catch(InterruptedException ie) {
			ie.printStackTrace();
		}
		
		if(event instanceof IEventSynchronized){
			model.addElement(syncText);
			//model.addElement(event.getMessage());

			if(event.getMessage().equals("App Un")){
				eventBus.callEvent(new EventForPartOne("ACK " + name));
			}else if(event.getMessage().equals("App Deux")){
				eventBus.callEvent(new EventForPartTwo("ACK " + name));
			}else if(event.getMessage().equals("App Trois")){
				eventBus.callEvent(new EventForPartThree("ACK " + name));
			}
		}
		else {

			if(event.getMessage().indexOf("ACK") != -1) { // si on a un accuser de reception

				//model.addElement(event.getMessage()); //afficher le ACK

				if(event.getMessage().indexOf("Un") != -1){
					eventBus.callEvent(new EventForPartTwoSync(name));
				}else if(event.getMessage().indexOf("Deux") != -1){
					eventBus.callEvent(new EventForPartThreeSync(name));
				}
			}else{
				model.addElement(event.toString() + " - " + event.getMessage());
			}
		}
	}
}

class PartOneActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartOneActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartOne(name));
	}
}

class PartTwoActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartTwoActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartTwo(name));
	}
}

class PartThreeActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public PartThreeActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForPartThree(name));
	}
}

class AllActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public AllActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) {
		eventBusConn.callEvent(new EventForAll(name));
	}
}

class AllSynchroActionListener implements ActionListener {
	private IEventBusConnector eventBusConn;
	private String name;
	public AllSynchroActionListener(String name, IEventBusConnector eventBusConn) {
		this.eventBusConn = eventBusConn;
		this.name = name;
	}
	public void actionPerformed(ActionEvent arg0) { //sync envoi
		eventBusConn.callEvent(new EventForPartOneSync(name));
		//eventBusConn.callEvent(new EventThatShouldBeSynchronized(name));
	}
}