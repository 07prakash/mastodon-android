package org.joinmastodon.android.fragments.settings;

import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import org.joinmastodon.android.GlobalUserPreferences;
import org.joinmastodon.android.R;
import org.joinmastodon.android.api.session.AccountSession;
import org.joinmastodon.android.api.session.AccountSessionManager;
import org.joinmastodon.android.model.Preferences;
import org.joinmastodon.android.model.viewmodel.CheckableListItem;
import org.joinmastodon.android.model.viewmodel.ListItem;
import org.joinmastodon.android.ui.M3AlertDialogBuilder;
import org.joinmastodon.android.ui.viewcontrollers.ComposeLanguageAlertViewController;

import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

public class SettingsBehaviorFragment extends BaseSettingsFragment<Void>{
	private ListItem<Void> languageItem, customTabsItem;
	private CheckableListItem<Void> altTextItem, playGifsItem, confirmUnfollowItem, confirmBoostItem, confirmDeleteItem;
	private Locale postLanguage;
	private ComposeLanguageAlertViewController.SelectedOption newPostLanguage;

	@Override
	public void onCreate(Bundle savedInstanceState){
		super.onCreate(savedInstanceState);
		setTitle(R.string.settings_behavior);

		AccountSession s=AccountSessionManager.get(accountID);
		if(s.preferences!=null && s.preferences.postingDefaultLanguage!=null){
			postLanguage=Locale.forLanguageTag(s.preferences.postingDefaultLanguage);
		}

		onDataLoaded(List.of(
				languageItem=new ListItem<>(getString(R.string.default_post_language), postLanguage!=null ? postLanguage.getDisplayName(Locale.getDefault()) : null, R.drawable.ic_language_24px, this::onDefaultLanguageClick),
				customTabsItem=new ListItem<>(R.string.settings_custom_tabs, GlobalUserPreferences.useCustomTabs ? R.string.in_app_browser : R.string.system_browser, R.drawable.ic_open_in_browser_24px, this::onCustomTabsClick),
				altTextItem=new CheckableListItem<>(R.string.settings_alt_text_reminders, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.altTextReminders, R.drawable.ic_alt_24px, this::toggleCheckableItem),
				playGifsItem=new CheckableListItem<>(R.string.settings_gif, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.playGifs, R.drawable.ic_animation_24px, this::toggleCheckableItem),
				confirmUnfollowItem=new CheckableListItem<>(R.string.settings_confirm_unfollow, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.confirmUnfollow, R.drawable.ic_person_remove_24px, this::toggleCheckableItem),
				confirmBoostItem=new CheckableListItem<>(R.string.settings_confirm_boost, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.confirmBoost, R.drawable.ic_repeat_24px, this::toggleCheckableItem),
				confirmDeleteItem=new CheckableListItem<>(R.string.settings_confirm_delete_post, 0, CheckableListItem.Style.SWITCH, GlobalUserPreferences.confirmDeletePost, R.drawable.ic_delete_24px, this::toggleCheckableItem)
		));
	}

	@Override
	protected void doLoadData(int offset, int count){}

	private void onDefaultLanguageClick(ListItem<?> item){
		ComposeLanguageAlertViewController vc=new ComposeLanguageAlertViewController(getActivity(), null, new ComposeLanguageAlertViewController.SelectedOption(-1, postLanguage, null), null);
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.default_post_language)
				.setView(vc.getView())
				.setPositiveButton(R.string.ok, (dlg, which)->{
					ComposeLanguageAlertViewController.SelectedOption opt=vc.getSelectedOption();
					if(!opt.locale.equals(postLanguage)){
						newPostLanguage=opt;
						languageItem.subtitle=newPostLanguage.locale.getDisplayLanguage(Locale.getDefault());
						rebindItem(languageItem);
					}
				})
				.setNegativeButton(R.string.cancel, null)
				.show();
	}

	private void onCustomTabsClick(ListItem<?> item){
//		GlobalUserPreferences.useCustomTabs=customTabsItem.checked;
		Intent intent=new Intent(Intent.ACTION_VIEW, Uri.parse("http://example.com"));
		ResolveInfo info=getActivity().getPackageManager().resolveActivity(intent, PackageManager.MATCH_DEFAULT_ONLY);
		final String browserName;
		if(info==null){
			browserName="??";
		}else{
			browserName=info.loadLabel(getActivity().getPackageManager()).toString();
		}
		ArrayAdapter<CharSequence> adapter=new ArrayAdapter<>(getActivity(), R.layout.item_alert_single_choice_2lines_but_different, R.id.text,
				new String[]{getString(R.string.in_app_browser), getString(R.string.system_browser)}){
			@Override
			public boolean hasStableIds(){
				return true;
			}

			@NonNull
			@Override
			public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent){
				View view=super.getView(position, convertView, parent);
				TextView subtitle=view.findViewById(R.id.subtitle);
				if(position==0){
					subtitle.setVisibility(View.GONE);
				}else{
					subtitle.setVisibility(View.VISIBLE);
					subtitle.setText(browserName);
				}
				return view;
			}
		};
		new M3AlertDialogBuilder(getActivity())
				.setTitle(R.string.settings_custom_tabs)
				.setSingleChoiceItems(adapter, GlobalUserPreferences.useCustomTabs ? 0 : 1, (dlg, which)->{
					GlobalUserPreferences.useCustomTabs=which==0;
					customTabsItem.subtitleRes=GlobalUserPreferences.useCustomTabs ? R.string.in_app_browser : R.string.system_browser;
					rebindItem(customTabsItem);
					dlg.dismiss();
				})
				.show();
	}

	@Override
	protected void onHidden(){
		super.onHidden();
		GlobalUserPreferences.playGifs=playGifsItem.checked;
		GlobalUserPreferences.altTextReminders=altTextItem.checked;
		GlobalUserPreferences.confirmUnfollow=confirmUnfollowItem.checked;
		GlobalUserPreferences.confirmBoost=confirmBoostItem.checked;
		GlobalUserPreferences.confirmDeletePost=confirmDeleteItem.checked;
		GlobalUserPreferences.save();
		if(newPostLanguage!=null){
			AccountSession s=AccountSessionManager.get(accountID);
			if(s.preferences==null)
				s.preferences=new Preferences();
			s.preferences.postingDefaultLanguage=newPostLanguage.locale.toLanguageTag();
			s.savePreferencesLater();
		}
	}
}
