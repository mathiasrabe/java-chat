import com.google.api.detect.Detect;
import com.google.api.translate.Translate;
import com.google.api.translate.Language;
import java.util.*;


public class TranslationEngine {
	
	private Vector<TranslationUser> users;
	private Language clientLang;

	TranslationEngine() {
		Translate.setHttpReferrer("moep.de"); //wozu?
		users = new Vector<TranslationUser>();
	}
	
	public void addUser(int uid) {
		TranslationUser user = new TranslationUser( uid );
		users.addElement(user);
	}
	
	public void removeUser(int uid) {
		TranslationUser TU = getUserObjById(uid);
		if (TU == null )
			return;
		users.removeElement( TU );
		TU = null;
	}
	
	public void setClientLanguage(String lang) {
		this.clientLang = Language.fromString(lang);
	}
	
	public String getClientLanguage() {
		return clientLang.toString();
	}
	
	/**
	 * Liefert das Objekt des TranslationUsers mit der passenden uid
	 * @param uid
	 * @return
	 */
	protected TranslationUser getUserObjById(int uid) {
		TranslationUser you = new TranslationUser();
		for(int i = 0; i <= users.size(); i++) {
			you = (TranslationUser) users.elementAt(i);
			if(you.getUid() == uid) {
				return you;
			}
		}
		return null;
	}
	
	/**
	 * Überprüfe den text des Benutzers mit der uid, welche Sprache es ist
	 * @param uid
	 * @param text
	 */
	public void checkForLanguage(int uid, String text) {
		Language lang = getLanguage(text);
		if (lang != null) {
			// TODO auf null überprüfen
			getUserObjById(uid).setLang(lang);
		}
	}
	
	/**
	 * Erhalte die Strache von text
	 * @param text
	 * @return
	 */
	private Language getLanguage(String text) {
		Language lang = null;
		try {
			lang = Detect.execute(text).getLanguage();
		} catch (Exception e) {
			System.err.println("Fehler bei Spracherkennung: " + e);
		}
		return lang;
	}
	
	public String translate(int uid, String text) {
		TranslationUser tu = getUserObjById(uid);
		if ( tu == null )
			return null;
		Language lang = tu.getLanguage();
		//Wenn keine Sprache gespeichert, dann überprüfe was für eine Sprache es sein könnte
		if (lang == null) {
			checkForLanguage(uid, text);
			lang = tu.getLanguage();
			//wenn immernoch nicht bekannt -> verlassen
			if (lang == null)
				return null;
		}
		
		String translatedText = null;
		try {
			translatedText = Translate.execute(text, lang, clientLang);
		} catch (Exception e) {
			System.err.println("Fehler bei Überstzung: " + e);
		}
		return translatedText;
	}
}
