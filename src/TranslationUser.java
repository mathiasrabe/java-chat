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
