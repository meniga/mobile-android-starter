package is.meniga.mobileandroidstarter;

import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.content.Context;
import android.content.Intent;
import android.support.v7.app.AppCompatActivity;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.View;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
import android.widget.AutoCompleteTextView;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.meniga.sdk.MenigaSDK;
import com.meniga.sdk.MenigaSettings;
import com.meniga.sdk.providers.tasks.Continuation;
import com.meniga.sdk.providers.tasks.Task;

import butterknife.BindView;
import butterknife.ButterKnife;
import butterknife.OnClick;
import is.meniga.mobileandroidstarter.auth.AuthService;
import is.meniga.mobileandroidstarter.auth.LoginResponse;

/**
 * A login screen that offers login via email/password.
 */
public class LoginActivity extends AppCompatActivity {
    private AuthService authService;

    @BindView(R.id.email)
    AutoCompleteTextView email;
    @BindView(R.id.password)
    EditText password;
    @BindView(R.id.login_progress)
    View progress;
    @BindView(R.id.login_form)
    View loginForm;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        authService = AuthService.init();

        MenigaSettings.Builder builder = new MenigaSettings.Builder()
                .endpoint(BuildConfig.API_BASE_URL)
                .addNetworkInterceptor(authService)
                .authenticator(authService);
        MenigaSDK.init(builder.build());

        ButterKnife.bind(this);

        password.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int id, KeyEvent keyEvent) {
                if (id == EditorInfo.IME_ACTION_DONE || id == EditorInfo.IME_NULL) {
                    attemptLogin();
                    return true;
                }
                return false;
            }
        });
    }

    @OnClick(R.id.email_sign_in_button)
    public void attemptLogin() {
        showProgress(true);

        authService.loginWithEmailAndPassword(this, email.getText().toString(), password.getText().toString()).continueWith(new Continuation<LoginResponse, Object>() {
            @Override
            public Object then(Task<LoginResponse> task) throws Exception {
                if(task.getResult() == LoginResponse.SUCCESS) {
                    Intent intent = new Intent(LoginActivity.this, TransactionListActivity.class);
                    startActivity(intent);
                    finish();
                } else if (task.getResult() == LoginResponse.UNAUTHORIZED) {
                    Toast.makeText(LoginActivity.this, R.string.wrong_email_or_password, Toast.LENGTH_LONG).show();
                    showProgress(false);
                } else {
                    Toast.makeText(LoginActivity.this, R.string.error_logging_in, Toast.LENGTH_LONG).show();
                    showProgress(false);
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void showProgress(final boolean show) {
        InputMethodManager imm = (InputMethodManager)getSystemService(Context.INPUT_METHOD_SERVICE);
        if (imm != null) {
            imm.hideSoftInputFromWindow(password.getWindowToken(), 0);
        }

        int shortAnimTime = getResources().getInteger(android.R.integer.config_shortAnimTime);

        loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
        loginForm.animate().setDuration(shortAnimTime).alpha(
                show ? 0 : 1).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                loginForm.setVisibility(show ? View.GONE : View.VISIBLE);
            }
        });

        progress.setVisibility(show ? View.VISIBLE : View.GONE);
        progress.animate().setDuration(shortAnimTime).alpha(
                show ? 1 : 0).setListener(new AnimatorListenerAdapter() {
            @Override
            public void onAnimationEnd(Animator animation) {
                progress.setVisibility(show ? View.VISIBLE : View.GONE);
            }
        });
    }
}
