package cj.js.hak_yo;

import java.util.List;

import android.animation.ObjectAnimator;
import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Path;
import android.view.View;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import cj.js.hak_yo.ble.FoundBeacon;

public class CharacterView {

	private ImageView imgCharacter;
	private RelativeLayout balloonLayout;
	private TextView txtBalloon;
	private Animation anim;

	public CharacterView(Context c, View v, int imageViewId, int layoutId,
			int txtId) {
		imgCharacter = (ImageView) v.findViewById(imageViewId);
		balloonLayout = (RelativeLayout) v.findViewById(layoutId);
		txtBalloon = (TextView) v.findViewById(txtId);

		anim = AnimationUtils.loadAnimation(c, R.anim.character_anim);
		doAnimation(true);
	}

	private void doAnimation(boolean isStart) {
		if (isStart) {
			imgCharacter.startAnimation(anim);
		} else {
			imgCharacter.clearAnimation();
		}
	}

	public void hideView() {
		doAnimation(false);
		balloonLayout.setVisibility(View.GONE);
		imgCharacter.setVisibility(View.GONE);
	}

	public void showView(List<FoundBeacon> friendList) {
		if (friendList.size() > 1) {
			txtBalloon.setText("+" + friendList.size());
			balloonLayout.setVisibility(View.VISIBLE);
		} else {
			balloonLayout.setVisibility(View.GONE);
		}
//		if(friendList.get(0) != null && friendList.get(0).getBeacon() != null){
//		txtBalloon.setText(String.valueOf(friendList.get(0).getBeacon().getRssi()));
//		balloonLayout.setVisibility(View.VISIBLE);
//		}

		if (!isVisible()) {
			imgCharacter.setImageResource(R.drawable.c0);
			imgCharacter.setVisibility(View.VISIBLE);

			doAnimation(true);
		}
	}

	public boolean isVisible() {
		return imgCharacter.getVisibility() == View.VISIBLE;
	}
}
