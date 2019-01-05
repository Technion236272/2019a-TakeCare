package com.syv.takecare.takecare;

import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.content.res.AppCompatResources;
import android.support.v7.widget.AppCompatEditText;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.annotation.TargetApi;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AlertDialog;

import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;

import android.database.Cursor;

import android.os.Build;
import android.provider.ContactsContract;
import android.text.TextUtils;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View.OnClickListener;
import android.view.inputmethod.EditorInfo;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static android.Manifest.permission.READ_CONTACTS;
import static android.view.View.VISIBLE;

public class SignUpFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor>, View.OnClickListener {
    /**
     * Id to identity READ_CONTACTS permission request.
     */
    private static final int REQUEST_READ_CONTACTS = 0;

    private static final String TAG = "SignUpFragment";

    private FirebaseAuth auth;
    private FirebaseFirestore db;

    // UI references
    private AppCompatEditText mNameView;
    private AutoCompleteTextView mEmailView;
    private CustomEditText mPasswordView;
    private CustomEditText mRePasswordView;
    private View mProgressView;
    private ImageButton backButton;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_sign_up, container, false);
        mNameView = (AppCompatEditText) view.findViewById(R.id.user_name);
        mEmailView = (AutoCompleteTextView) view.findViewById(R.id.email);
        populateAutoComplete();
        mPasswordView = (CustomEditText) view.findViewById(R.id.password);
        mRePasswordView = (CustomEditText) view.findViewById(R.id.confirm_password);

        auth = FirebaseAuth.getInstance();
        db = FirebaseFirestore.getInstance();

        mRePasswordView.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });

        backButton = (ImageButton) view.findViewById(R.id.sign_up_back_button);
        backButton.setOnClickListener(this);

        Button mEmailSignInButton = (Button) view.findViewById(R.id.email_sign_in_button);
        mEmailSignInButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                attemptLogin();
            }
        });

        //mLoginFormView = view.findViewById(R.id.login_form);
        mProgressView = view.findViewById(R.id.sign_up_progress_bar);

        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser!=null) {
            Intent intent = new Intent(getActivity(), GatewayActivity.class);
            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
            startActivity(intent);
        }


        mNameView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_person_outline_black_24dp), null, null, null);
        mEmailView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_mail_outline_black_24dp), null, null, null);
        mPasswordView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_lock_outline_black_24dp), null, null, null);
        mRePasswordView.setCompoundDrawablesWithIntrinsicBounds(AppCompatResources.getDrawable(getActivity().getApplicationContext(), R.drawable.ic_lock_outline_black_24dp), null, null, null);
        return view;
    }

    private void populateAutoComplete() {
        if (!mayRequestContacts()) {
            return;
        }

        //getLoaderManager().initLoader(0, null, getActivity());
    }

    private boolean mayRequestContacts() {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return true;
        }
        if (ContextCompat.checkSelfPermission(getActivity(), READ_CONTACTS) == PackageManager.PERMISSION_GRANTED) {
            return true;
        }
        if (shouldShowRequestPermissionRationale(READ_CONTACTS)) {
            Snackbar.make(mEmailView, R.string.permission_rationale, Snackbar.LENGTH_INDEFINITE)
                    .setAction(android.R.string.ok, new View.OnClickListener() {
                        @Override
                        @TargetApi(Build.VERSION_CODES.M)
                        public void onClick(View v) {
                            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
                        }
                    });
        } else {
            requestPermissions(new String[]{READ_CONTACTS}, REQUEST_READ_CONTACTS);
        }
        return false;
    }

    /**
     * Callback received when a permissions request has been completed.
     */
    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        if (requestCode == REQUEST_READ_CONTACTS) {
            if (grantResults.length == 1 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                populateAutoComplete();
            }
        }
    }

    /**
     * Attempts to sign in or register the account specified by the login form.
     * If there are form errors (invalid email, missing fields, etc.), the
     * errors are presented and no actual login attempt is made.
     */
    private void attemptLogin() {

        // Reset errors.
        mNameView.setError(null);
        mEmailView.setError(null);
        mPasswordView.setError(null);
        mRePasswordView.setError(null);

        // Store values at the time of the login attempt.
        String name = mNameView.getText().toString();
        String email = mEmailView.getText().toString();
        String password = mPasswordView.getText().toString();
        String rePassword = mRePasswordView.getText().toString();

        boolean cancel = false;
        View focusView = null;

        //Check for a valid user name
        if (TextUtils.isEmpty(name)) {
            mNameView.setError(getString(R.string.error_field_required));
            focusView = mNameView;
            cancel = true;
        } else if (!isNameValid(name)) {
            mNameView.setError(getString(R.string.error_invalid_name));
            focusView = mNameView;
            cancel = true;
        }

        //Check for a valid email address
        if (TextUtils.isEmpty(email)) {
            mEmailView.setError(getString(R.string.error_field_required));
            focusView = mEmailView;
            cancel = true;
        } else if (!isEmailValid(email)) {
            mEmailView.setError(getString(R.string.error_invalid_email));
            focusView = mEmailView;
            cancel = true;
        }

        //Check for a valid password
        boolean validPassword = true;
        if (TextUtils.isEmpty(password)) {
            mPasswordView.setError(getString(R.string.error_field_required));
            focusView = mPasswordView;
            cancel = true;
            validPassword = false;
        } else if (!isPasswordValid(password)) {
            mPasswordView.setError(getString(R.string.error_invalid_password));
            focusView = mPasswordView;
            cancel = true;
            validPassword = false;
        }

        //Check for a valid re-password
        if(validPassword) {
            if (TextUtils.isEmpty(rePassword)) {
                mRePasswordView.setError(getString(R.string.error_field_required));
                focusView = mRePasswordView;
                cancel = true;
            } else if (!isRePasswordValid(rePassword, password)) {
                mRePasswordView.setError(getString(R.string.error_invalid_re_password));
                focusView = mRePasswordView;
                cancel = true;
            }
        }

        if (cancel) {
            // There was an error; don't attempt login and focus the first
            // form field with an error.
            focusView.requestFocus();
        } else {
            // Show a progress spinner, and kick off a background task to
            // perform the user login attempt.

            // showProgress(true);
            Log.d(TAG, "attemptLogin Completed");
            createAccount(name, email, password);
        }
    }

    private boolean isNameValid(String name) {
        int spaceCounter = 1;
        int comaCounter = 1;

        for(int i = 0; i<name.length(); i++) {
            char c = name.charAt(i);
            if(! ((c>='a' && c<='z')
                    || (c>='A' && c<='Z')
                    || (c>='0' && c<='9'))) {
                if((i!=0 && c == ' ' && spaceCounter > 0 && i!=name.length()-1 && name.charAt(i+1)!=' ')) {
                    spaceCounter--;
                } else if((i!=0 && c == '.' && comaCounter > 0 && i!=name.length()-1 && name.charAt(i+1)!='.')) {
                    comaCounter--;
                } else {
                    return false;
                }
            }
        }
        return true;
    }


    private boolean isEmailValid(String email) {
        //TODO: Change this validation logic
        return email.contains("@");
    }

    private boolean isPasswordValid(String password) {
        //TODO: Change this validation logic
        return password.length() >= 6;
    }

    private boolean isRePasswordValid(String rePassword, String password) {
        return rePassword.equals(password);
    }

    public Loader<Cursor> onCreateLoader(int i, Bundle bundle) {
        return new CursorLoader(getActivity(),
                // Retrieve data rows for the device user's 'profile' contact.
                Uri.withAppendedPath(ContactsContract.Profile.CONTENT_URI,
                        ContactsContract.Contacts.Data.CONTENT_DIRECTORY), ProfileQuery.PROJECTION,

                // Select only email addresses.
                ContactsContract.Contacts.Data.MIMETYPE +
                        " = ?", new String[]{ContactsContract.CommonDataKinds.Email
                .CONTENT_ITEM_TYPE},

                // Show primary email addresses first. Note that there won't be
                // a primary email address if the user hasn't specified one.
                ContactsContract.Contacts.Data.IS_PRIMARY + " DESC");
    }

    public void onLoadFinished(Loader<Cursor> cursorLoader, Cursor cursor) {
        List<String> emails = new ArrayList<>();
        emails.add("@gmail.com");
        cursor.moveToFirst();
        while (!cursor.isAfterLast()) {
            emails.add(cursor.getString(ProfileQuery.ADDRESS));
            cursor.moveToNext();
        }

        addEmailsToAutoComplete(emails);
    }

    public void onLoaderReset(Loader<Cursor> cursorLoader) {

    }

    private void addEmailsToAutoComplete(List<String> emailAddressCollection) {
        //Create adapter to tell the AutoCompleteTextView what to show in its dropdown list.
        ArrayAdapter<String> adapter =
                new ArrayAdapter<>(getActivity(),
                        android.R.layout.simple_dropdown_item_1line, emailAddressCollection);

        mEmailView.setAdapter(adapter);
    }


    private interface ProfileQuery {
        String[] PROJECTION = {
                ContactsContract.CommonDataKinds.Email.ADDRESS,
                ContactsContract.CommonDataKinds.Email.IS_PRIMARY,
        };

        int ADDRESS = 0;
        int IS_PRIMARY = 1;
    }


    private void createAccount(final String name, final String email, final String password) {
        final ProgressDialog dialog = new ProgressDialog(getActivity());
        dialog.setMessage("Signing up...");
        dialog.show();
        auth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(getActivity(), new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if(task.isSuccessful()) {
                            assert auth.getCurrentUser()!=null;
                            setUserInfo(auth.getCurrentUser(), name, email, password);
                            // Sign in success, update UI with the signed-in user's information
                            Log.d(TAG, "createUserWithEmail:success");
                            Intent intent = new Intent(getActivity(), GatewayActivity.class);
                            intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                            intent.putExtra(Intent.EXTRA_TEXT, true);
                            dialog.dismiss();
                            startActivity(intent);
                        } else {
                            dialog.dismiss();
                            try {
                                throw task.getException();
                            } catch(FirebaseAuthUserCollisionException e) {
                                AlertDialog.Builder builder;
                                builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Sign Up")
                                        .setMessage("Entered email is already registered")
                                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //close login session of the user
                                                FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        })
                                        .show();

                            } catch(FirebaseAuthInvalidCredentialsException e) {
                                AlertDialog.Builder builder;
                                builder = new AlertDialog.Builder(getActivity());
                                builder.setTitle("Sign Up")
                                        .setMessage("You have entered an illegal email")
                                        .setNeutralButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                                            public void onClick(DialogInterface dialog, int which) {
                                                //close login session of the user
                                                FirebaseAuth.getInstance().signOut();
                                                Intent intent = new Intent(getActivity(), LoginActivity.class);
                                                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_CLEAR_TASK);
                                                startActivity(intent);
                                            }
                                        })
                                        .show();
                            } catch(Exception e) {
                                //Do nothing
                            }
                        }
//                        pd.hide();
                    }
                });
    }

    private void setUserInfo(FirebaseUser user, String name, String email, String password) {
        assert user != null;
        Map<String, Object> userInfo = new HashMap<>();
        userInfo.put("name", name);
        userInfo.put("email", email);
        userInfo.put("password", password);
        userInfo.put("rating", 0);
        userInfo.put("ratingCount", 0);
        String uid = user.getUid();
        db.collection("users").document(uid)
                .set(userInfo)
                .addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void aVoid) {
                        Log.d("TAG", "DocumentSnapshot successfully written!");
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.w("TAG", "Error writing document", e);
                    }
                });
    }

    @Override
    public void onClick(View v) {
        getActivity().onBackPressed();
    }
}
