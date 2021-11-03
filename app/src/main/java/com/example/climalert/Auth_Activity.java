package com.example.climalert;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.SignInButton;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;

public class Auth_Activity extends AppCompatActivity {

    int RC_SIGN_IN = 0;
    GoogleSignInClient mGoogleSignInClient;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_auth);

        final SignInButton button = findViewById(R.id.sign_in_button);
        button.setOnClickListener(new View.OnClickListener() {
          @Override
          public void onClick(View v) {
              switch (v.getId()) {
                  case R.id.sign_in_button:
                      signIn();

                      break;
              }
              }
          });
           GoogleSignInOptions googleConf = new GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN).
                    requestEmail().
                    build();
            mGoogleSignInClient = GoogleSignIn.getClient(this, googleConf);


    }






    @Override
    protected void onStart() {
        //Comprovamos si el usuario ya había iniciado sesión
        super.onStart();
        GoogleSignInAccount account = GoogleSignIn.getLastSignedInAccount(this);
    }


    private void signIn() {
        Intent singInIntent = mGoogleSignInClient.getSignInIntent();
        startActivityForResult(singInIntent, RC_SIGN_IN);
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == RC_SIGN_IN) {
            Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent(data);
            handleSignInResult(task);
            setContentView(R.layout.activity_main);

        }
    }

    private void handleSignInResult (Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount account = completedTask.getResult(ApiException.class);
            Intent intent = new Intent(Auth_Activity.this, MainActivity.class);
            startActivity(intent);

        } catch (ApiException e) {
           Log.w("error","signInResult failed, code:=" + e.getStatusCode());

        }
    }

}




