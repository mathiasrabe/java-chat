/* Java Chat
 * (C) 2010 
 * Mathias Rabe, Benjamin Lesky, Sebastian Stock, Michael Götz, Andreas Gröger
 *
 * This code is licenced under the GPL v3.
 */

import com.google.api.translate.Language;

public class TranslationUser {
	
	protected int uid;
	protected Language lang;

	TranslationUser() {
	}
	
	TranslationUser(int uid) {
		this.uid = uid;
	}
	
	public int getUid() {
		return uid;
	}
	
	public void setLang(Language lang) {
		this.lang = lang;
	}
	
	public Language getLanguage() {
		return lang;
	}
	
	/*
	public void setLangScore(String lang, int score) {
		
	}
	*/
}
