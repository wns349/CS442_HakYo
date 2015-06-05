package cj.js.hak_yo.character;

import android.content.Context;
import android.os.Handler;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;
import cj.js.hak_yo.db.FriendInfo;

public class Character {
	private final CharacterView characterView;
	private final CharacterBalloonView characterBalloonView;

	public Character(Context context, FriendInfo friendInfo,
			ViewGroup mainLayout) {
		this.characterBalloonView = new CharacterBalloonView(context,
				friendInfo);
		this.characterView = new CharacterView(context, friendInfo, mainLayout);

		this.characterView.setId((int) (System.currentTimeMillis() % 10000));
	}

	public CharacterView getCharacterView() {
		return characterView;
	}

	public CharacterBalloonView getCharacterBalloonView() {
		return characterBalloonView;
	}

	public void moveCharacterTo(int targetIndexToGo, boolean doAnimation) {
		if (this.characterView.getPrevAnimation().getLastIndex() == targetIndexToGo) {
			return;
		} else {
			showBalloon(false);
			this.characterView.moveTo(targetIndexToGo, doAnimation);
		}
	}

	public void initialize() {
		showBalloon(false);

		// Start at 0
		this.characterView.moveTo(2, false);

		this.characterView.setOnClickListener(new View.OnClickListener() {
			@Override
			public void onClick(View v) {
				// Update layout param
				RelativeLayout.LayoutParams balloonLayoutParams = new RelativeLayout.LayoutParams(
						ViewGroup.LayoutParams.WRAP_CONTENT,
						ViewGroup.LayoutParams.WRAP_CONTENT);

				balloonLayoutParams.addRule(RelativeLayout.ABOVE,
						characterView.getId());
				// balloonLayoutParams.addRule(RelativeLayout.ALIGN_LEFT,
				// characterView.getId());
				characterBalloonView.setLayoutParams(balloonLayoutParams);
				showBalloon(true);

				Handler h = new Handler();
				h.postDelayed(new Runnable() {

					@Override
					public void run() {
						showBalloon(false);
					}
				}, 3000L);
			}
		});
	}

	public void showBalloon(boolean isShow) {
		characterBalloonView.setVisibility(isShow ? View.VISIBLE : View.GONE);
	}
}
