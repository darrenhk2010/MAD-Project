package com.mad.project.view;

/**
 * Created by Darren on 14/05/2018.
 */

import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.text.TextUtils;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.mad.project.R;

/**
 * Reset password screen. It will request Firebase to send reset password email
 */
public class ResetPasswordActivity extends AppCompatActivity {

    private EditText mInputEmail;
    private Button mBtnReset, mBtnBack;
    private FirebaseAuth mAuth;
    private ProgressBar mProgressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_reset_password);

        mInputEmail = (EditText) findViewById(R.id.email);
        mBtnReset = (Button) findViewById(R.id.btn_reset_password);
        mBtnBack = (Button) findViewById(R.id.btn_back);
        mProgressBar = (ProgressBar) findViewById(R.id.progressBar);

        mAuth = FirebaseAuth.getInstance();

        mBtnBack.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                finish();
            }
        });

        mBtnReset.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String email = mInputEmail.getText().toString().trim();

                if (TextUtils.isEmpty(email)) {
                    Toast.makeText(getApplication(), getResources().getString(R.string.EmailEmpty), Toast.LENGTH_SHORT).show();
                    return;
                }

                mProgressBar.setVisibility(View.VISIBLE);
                mAuth.sendPasswordResetEmail(email)
                        .addOnCompleteListener(new OnCompleteListener<Void>() {
                            @Override
                            public void onComplete(@NonNull Task<Void> task) {
                                if (task.isSuccessful()) {
                                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.resetEmail), Toast.LENGTH_SHORT).show();
                                } else {
                                    Toast.makeText(ResetPasswordActivity.this, getResources().getString(R.string.Failed), Toast.LENGTH_SHORT).show();
                                }

                                mProgressBar.setVisibility(View.GONE);
                            }
                        });
            }
        });
    }

}