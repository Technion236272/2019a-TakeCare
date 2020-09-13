package com.syv.takecare.takecare.fragments;


import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.appcompat.content.res.AppCompatResources;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.syv.takecare.takecare.activities.TakerMenuActivity;
import com.syv.takecare.takecare.customViews.CustomEditText;
import com.syv.takecare.takecare.R;

public class SignInFragment extends Fragment  implements View.OnClickListener {
    private static final String TAG = "SignInFragment";

    private CustomEditText emailView;
    private CustomEditText passwordView;
    private Button signInButton;
    private ImageButton backButton;

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_in, container, false);
        emailView = (CustomEditText) view.findViewById(R.id.sign_in_email);
        passwordView = (CustomEditText) view.findViewById(R.id.sign_in_password);
        signInButton = (Button) view.findViewById(R.id.sign_in_button_login_screen);
        backButton = (ImageButton) view.findViewById(R.id.sign_in_back_button);

        signInButton.setOnClickListener(this);
        backButton.setOnClickListener(this);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        passwordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
                boolean handled = false;
                if (actionId == EditorInfo.IME_ACTION_DONE) {
                    onClick(null);
                    handled = true;
                }
                return handled;
            }
        });

        passwordView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_mail_outline_black_24dp), null, null, null);
        emailView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_lock_outline_black_24dp), null, null, null);
        return view;
    }


    public void onClick(View view) {
        if (view == backButton) {
            getActivity().onBackPressed();
            return;
        }
        final String email = emailView.getText().toString();
        final String password = passwordView.getText().toString();

        if (checkFields(email, password)) {
            final ProgressDialog dialog = new ProgressDialog(getActivity());
            dialog.setMessage("Signing in...");
            dialog.setCancelable(false);
            dialog.setIndeterminate(true);
            dialog.show();
            auth.signInWithEmailAndPassword(email, password)
                    .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                        @Override
                        public void onComplete(@NonNull Task<AuthResult> task) {
                            if (task.isSuccessful()) {
                                assert auth.getCurrentUser() != null;
                                // Sign in success, update UI with the signed-in user's information
                                Log.d(TAG, "signInWithEmail:success");
                                Intent intent = new Intent(getActivity(), TakerMenuActivity.class);
                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                intent.putExtra(Intent.EXTRA_TEXT, true);
                                dialog.dismiss();
                                startActivity(intent);
                            } else {
                                emailView.setError("");
                                passwordView.setError("");
                                Toast.makeText(getActivity(), R.string.wrong_credentials,
                                        Toast.LENGTH_SHORT).show();
                                Log.d(TAG, "signInWithEmail:failure");
                                dialog.dismiss();
                            }
                        }
                    });
        }
    }

    private boolean isEmailValid(String email) {
        //TODO: Change this validation logic
        return email.contains("@");
    }

    private boolean checkFields(String email, String password) {
        boolean valid = true;
        if (TextUtils.isEmpty(email)) {
            emailView.setError(getString(R.string.error_field_required));
        } else if (!isEmailValid(email)) {
            emailView.setError(getString(R.string.error_invalid_email));
            valid = false;
        }
        if (TextUtils.isEmpty(password)) {
            passwordView.setError(getString(R.string.error_field_required));
        }

        if (TextUtils.isEmpty(email) || TextUtils.isEmpty(password)) {
            Toast.makeText(getActivity(), R.string.credentials_missing, Toast.LENGTH_SHORT)
                    .show();
            valid = false;
        }
        return valid;
    }
}