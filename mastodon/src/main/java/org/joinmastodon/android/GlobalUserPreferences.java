package org.joinmastodon.android;

import android.content.Context;
import android.content.SharedPreferences;

import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Account;

public class GlobalUserPreferences{
	public static boolean playGifs;
	public static boolean useCustomTabs;
	public static boolean altTextReminders, confirmUnfollow, confirmBoost, confirmDeletePost;
	public static ThemePreference theme=ThemePreference.AUTO;

	private static SharedPreferences getPrefs(){
		return MastodonApp.context.getSharedPreferences("global", Context.MODE_PRIVATE);
	}

	private static SharedPreferences getPreReplyPrefs(){
		return MastodonApp.context.getSharedPreferences("pre_reply_sheets", Context.MODE_PRIVATE);
	}

	public static void load(){
		SharedPreferences prefs=getPrefs();
		playGifs=prefs.getBoolean("playGifs", true);
		useCustomTabs=prefs.getBoolean("useCustomTabs", true);
		altTextReminders=prefs.getBoolean("altTextReminders", false);
		confirmUnfollow=prefs.getBoolean("confirmUnfollow", false);
		confirmBoost=prefs.getBoolean("confirmBoost", false);
		confirmDeletePost=prefs.getBoolean("confirmDeletePost", true);
		theme=ThemePreference.values()[prefs.getInt("theme", 0)];
	}

	public static void save(){
		getPrefs().edit()
				.putBoolean("playGifs", playGifs)
				.putBoolean("useCustomTabs", useCustomTabs)
				.putInt("theme", theme.ordinal())
				.putBoolean("altTextReminders", altTextReminders)
				.putBoolean("confirmUnfollow", confirmUnfollow)
				.putBoolean("confirmBoost", confirmBoost)
				.putBoolean("confirmDeletePost", confirmDeletePost)
				.apply();
	}

	public static boolean isOptedOutOfPreReplySheet(PreReplySheetType type, Account account, String accountID){
		if(getPreReplyPrefs().getBoolean("opt_out_"+type, false))
			return true;
		if(account==null)
			return false;
		String accountKey=account.acct;
		if(!accountKey.contains("@"))
			accountKey+="@"+AccountSessionManager.get(accountID).domain;
		return getPreReplyPrefs().getBoolean("opt_out_"+type+"_"+accountKey.toLowerCase(), false);
	}

	public static void optOutOfPreReplySheet(PreReplySheetType type, Account account, String accountID){
		String key;
		if(account==null){
			key="opt_out_"+type;
		}else{
			String accountKey=account.acct;
			if(!accountKey.contains("@"))
				accountKey+="@"+AccountSessionManager.get(accountID).domain;
			key="opt_out_"+type+"_"+accountKey.toLowerCase();
		}
		getPreReplyPrefs().edit().putBoolean(key, true).apply();
	}

	public static void resetPreReplySheets(){
		getPreReplyPrefs().edit().clear().apply();
	}

	public enum ThemePreference{
		AUTO,
		LIGHT,
		DARK
	}

	public enum PreReplySheetType{
		OLD_POST,
		NON_MUTUAL
	}
}
