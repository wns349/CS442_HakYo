package cj.js.hak_yo.character;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;
import android.view.animation.LinearInterpolator;
import android.view.animation.RotateAnimation;
import android.view.animation.ScaleAnimation;
import android.view.animation.TranslateAnimation;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import cj.js.hak_yo.Const;
import cj.js.hak_yo.Const.Character;
import cj.js.hak_yo.R;
import cj.js.hak_yo.db.FriendInfo;

public class CharacterView extends RelativeLayout {

	private static final String TAG = "CJS_Char";

	private ImageView imgCharacter = null;

	private final FriendInfo friendInfo;

	private final ViewGroup mainLayout;

	private final PrevAnimation prevAnimation = new PrevAnimation();

	public CharacterView(Context context, FriendInfo friendInfo,
			ViewGroup mainLayout) {
		super(context);
		this.friendInfo = friendInfo;
		this.mainLayout = mainLayout;
		initializeView(context);
	}

	private void initializeView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.character_view, this);

		setCharacterImageView((ImageView) this.findViewById(R.id.img_character));

		Character character = Const.Character
				.valueOf(friendInfo.getCharacter());
		switch (character) {
		case c0:
			getCharacterImageView().setImageResource(R.drawable.c0);
			break;
		case c1:
			getCharacterImageView().setImageResource(R.drawable.c1);
			break;
		case c2:
			getCharacterImageView().setImageResource(R.drawable.c2);
			break;
		case c3:
			getCharacterImageView().setImageResource(R.drawable.c3);
			break;
		case c4:
			getCharacterImageView().setImageResource(R.drawable.c4);
		default:
			break;
		}
	}

	public void moveTo(int targetIndexToGo, boolean doAnimation) {
		final View self = this;
		if (doAnimation) {
			if (prevAnimation.isFirstRun()) {
				// For some reason, skip first anim
				prevAnimation.setFirstRun(false);
				return;
			}

			// Calculate targetIndex
			final int newTargetIndexToGo;
			if (prevAnimation.getLastIndex() + 1 < targetIndexToGo) {
				newTargetIndexToGo = prevAnimation.getLastIndex() + 1;
			} else if (prevAnimation.getLastIndex() - 1 > targetIndexToGo) {
				newTargetIndexToGo = prevAnimation.getLastIndex() - 1;
			} else {
				newTargetIndexToGo = targetIndexToGo;
			}
			final View destView = getDestinationView(newTargetIndexToGo);

			float tx, ty, ttx, tty;
			float sx, sy, ssx, ssy;
			int fromLoc[] = new int[2];
			self.getLocationOnScreen(fromLoc);
			int toLoc[] = new int[2];
			destView.getLocationOnScreen(toLoc);
//			ttx = toLoc[0];
//			tty = toLoc[1];
//			tx = fromLoc[0];
//			ty = fromLoc[1];
			tx = self.getX();
			ty = self.getY();
			ttx = destView.getX();
			tty = destView.getY();

			sx = self.getLayoutParams().width;
			sy = self.getLayoutParams().height;
			ssx = destView.getLayoutParams().width;
			ssy = destView.getLayoutParams().height;

			// Close existing animation
			this.clearAnimation();

		

			TranslateAnimation animTranslate = new TranslateAnimation(0, ttx
					- tx, 0, tty - ty);
			Log.d(TAG, "Translate: " + tx + "," + ttx + "," + ty + "," + tty
					+ " (ttx-tx) :" + (ttx - tx) + " (tty-ty):" + (tty - ty));
			animTranslate.setFillEnabled(true);
			animTranslate.setFillAfter(true);

			ScaleAnimation animScale = new ScaleAnimation(1, ssx / sx, 1, ssy
					/ sy, sx / 2, sy / 2);

			Log.d(TAG, "Scale: " + sx + "," + ssx + "," + sy + "," + ssy);

			RotateAnimation animRotate = new RotateAnimation(-5, 5, sx / 2,
					sy / 2);
			animRotate.setRepeatCount(Animation.INFINITE);
			animRotate.setRepeatMode(AnimationSet.REVERSE);

			final AnimationSet animSet = new AnimationSet(true);
			animSet.setDuration(Const.ANIMATION_TIME);
			animSet.setInterpolator(new LinearInterpolator());
			animSet.setRepeatCount(0);
			animSet.setFillAfter(true);
			animSet.setFillEnabled(true);
			animSet.addAnimation(animTranslate);
			animSet.addAnimation(animScale);
			animSet.addAnimation(animRotate);

			animSet.setAnimationListener(new AnimationListener() {
				@Override
				public void onAnimationStart(Animation animation) {
					prevAnimation.setAnimation(animSet);
				}

				@Override
				public void onAnimationRepeat(Animation animation) {
				}

				@Override
				public void onAnimationEnd(Animation animation) {
					self.setLayoutParams(destView.getLayoutParams());
					self.clearAnimation();
					prevAnimation.setLastIndex(newTargetIndexToGo);
					prevAnimation.setAnimation(null);

					startStationaryAnimation();
				}
			});
			this.startAnimation(animSet);
		} else {
			final View destView = getDestinationView(targetIndexToGo);
			this.setLayoutParams(destView.getLayoutParams());
			prevAnimation.setLastIndex(targetIndexToGo);
			startStationaryAnimation();
		}
	}

	protected void startStationaryAnimation() {
		RotateAnimation animRotate = new RotateAnimation(-5, 5,
				getLayoutParams().width / 2.0f, getLayoutParams().height / 2.0f);
		animRotate.setRepeatCount(Animation.INFINITE);
		animRotate.setRepeatMode(AnimationSet.REVERSE);
		final AnimationSet animSet = new AnimationSet(true);
		animSet.setDuration(Const.ANIMATION_TIME);
		animSet.setInterpolator(new LinearInterpolator());
		animSet.setRepeatCount(0);
		animSet.setFillAfter(true);
		animSet.setFillEnabled(true);
		animSet.addAnimation(animRotate);
		prevAnimation.setAnimation(animSet);
		this.startAnimation(animSet);
	}

	private View getDestinationView(int targetIndexToGo) {
		switch (targetIndexToGo) {
		case 4:
			return mainLayout.findViewById(R.id.space_character_loc_05);
		case 3:
			return mainLayout.findViewById(R.id.space_character_loc_04);
		case 2:
			return mainLayout.findViewById(R.id.space_character_loc_03);
		case 1:
		default:
			return mainLayout.findViewById(R.id.space_character_loc_02);
		case 0:
			return mainLayout.findViewById(R.id.space_character_loc_01);
		}
	}

	public ImageView getCharacterImageView() {
		return imgCharacter;
	}

	public void setCharacterImageView(ImageView imgCharacter) {
		this.imgCharacter = imgCharacter;
	}

	public PrevAnimation getPrevAnimation() {
		return this.prevAnimation;
	}

	class PrevAnimation {
		private AnimationSet animation;
		private int lastIndex;

		private boolean isFirstRun;

		public PrevAnimation() {
			animation = null;
			lastIndex = -1;
			isFirstRun = true;
		}

		public int getLastIndex() {
			return lastIndex;
		}

		public void setLastIndex(int lastIndex) {
			this.lastIndex = lastIndex;
		}

		public AnimationSet getAnimation() {
			return animation;
		}

		public void setAnimation(AnimationSet animation) {
			this.animation = animation;
		}

		public boolean isFirstRun() {
			return isFirstRun;
		}

		public void setFirstRun(boolean isFirstRun) {
			this.isFirstRun = isFirstRun;
		}
	}

}
