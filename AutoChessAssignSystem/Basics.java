import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import java.io.FileNotFoundException;
import java.io.ClassNotFoundException;
import java.io.IOException;

import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;

class Basics {

	private static int index = 1;
	private final String filePath = "participants.data"

	public Basics() {}
	
	public void AddParticipants(String name) {
	
		Participant participant = new Participant(index, name);
		WriteToFile(participant);

		index += 1;
	}
	
	public void DeleteParticipants(String name) {
		
		int number = p.length;
		int newListCounter = 0;
		Participant[] pList = ReadFromFile(filePath);
		Participant[] newList = Participant[number-1];
		
		for(int i=0; i < number; i++) {
			if((pList[i].name).equals(name)) continue;
			else {
				newList[newListCounter].index = newListCounter;
				newList[newListCounter].name = pList[i].name;
				newListCounter += 1;
			}
		}
	}
	
	public void WriteToFile(Participants p) {
		
		try {
			ObjectOutputStream objectOutputStream = new ObjectOutputStream(new FileOutputStream(filePath));
			objectOutputStream.writeObject(p);
			objectOutputStream.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		}
	}
	
	public Participant[] ReadFromFile(String filePath) {
		
		int index = 0;
		Participant[] participants;
		
		try {
			ObjectInputStream objectInputStream = new ObjectInputStream(new FileInputStream(filePath));
			while(objectInputStream.hasNext()) {
				participants[index] = (Participant)objectInputStream.readObject();
				index += 1;
			}
			objectInputStream.close();
		} catch(IOException ioe) {
			ioe.printStackTrace();
		} catch(ClassNotFoundException cnfe) {
			cnfe.printStackTrace();
		}
		
		return participants;
	}
}