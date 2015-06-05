package cj.js.hak_yo.character;

import android.content.Context;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.TextView;
import cj.js.hak_yo.R;
import cj.js.hak_yo.db.FriendInfo;

public class CharacterBalloonView extends FrameLayout {
	private ImageView imgBalloon = null;
	private TextView txtBalloon = null;

	public CharacterBalloonView(Context context, FriendInfo friendInfo) {
		super(context);

		initializeView(context);

		txtBalloon.setText(friendInfo.getAlias() + "\n");
	}

	private void initializeView(Context context) {
		LayoutInflater inflater = LayoutInflater.from(context);
		inflater.inflate(R.layout.character_balloon_view, this);

		imgBalloon = (ImageView) findViewById(R.id.img_character_balloon);
		txtBalloon = (TextView) findViewById(R.id.txt_character_balloon);
	}

}
