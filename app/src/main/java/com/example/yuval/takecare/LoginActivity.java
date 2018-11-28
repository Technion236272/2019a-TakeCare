package com.example.yuval.takecare;

import android.graphics.drawable.BitmapDrawable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.transition.Explode;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.transition.TransitionManager;
import android.support.transition.Fade;
import android.view.View;
import android.widget.TextView;

public class LoginActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

    }

    public void transition(View view) {
        ConstraintSet constraintSet = new ConstraintSet();
        constraintSet.clone(this,R.layout.activity_login2);
        TransitionManager tr = new TransitionManager();
        TransitionManager.beginDelayedTransition((ConstraintLayout) findViewById(R.id.shit));
        constraintSet.applyTo((ConstraintLayout) findViewById(R.id.shit));

    }
}
