package com.st.stchat.activity;

import android.content.Intent;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.animation.AlphaAnimation;
import android.view.animation.Animation;
import android.view.animation.Animation.AnimationListener;
import android.view.animation.AnimationSet;

import com.st.stchat.R;

/**
 * 
 * @author
 * 
 */
public class WelcomeActivity extends BaseActivity {

	@Override
	protected void onCreate(Bundle savedInstanceState) {

		super.onCreate(savedInstanceState);
		final View view = View.inflate(this, R.layout.activity_welcome, null);
		setContentView(view);
		// STChatApplication application = STChatApplication.getInstance();
		AnimationSet animationSet = new AnimationSet(true);
		// 从完全透明到完全不透明
		AlphaAnimation alphaAnimation = new AlphaAnimation(0, 1);
		alphaAnimation.setDuration(1444);
		alphaAnimation.setAnimationListener(new WelcomeAnimationListener());
		animationSet.addAnimation(alphaAnimation);
		// 执行animationSet里面的所有动画效果
		view.startAnimation(animationSet);

	}

	private class WelcomeAnimationListener implements AnimationListener {
		@Override
		public void onAnimationStart(Animation animation) {

		}

		@Override
		public void onAnimationEnd(Animation animation) {

			// Intent intent = null;

			if (true) {// 判断用户是否已登录
				WelcomeActivity.this.startActivity(new Intent(
						WelcomeActivity.this, LoginActivity.class));
			} else {
				WelcomeActivity.this.startActivity(new Intent(
						WelcomeActivity.this, MainActivity.class));
			}
			WelcomeActivity.this.finish();
		}

		@Override
		public void onAnimationRepeat(Animation animation) {

		}
	}

	@Override
	protected void onDestroy() {

		super.onDestroy();
	}

	/* 物理返回键无效 */
	@Override
	public boolean onKeyDown(int KeyCode, KeyEvent event) {
		return false;
	}
}
