package is.meniga.mobileandroidstarter;

import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.view.View;
import android.widget.Toast;

import com.google.gson.internal.LinkedTreeMap;
import com.meniga.sdk.MenigaSDK;
import com.meniga.sdk.models.transactions.MenigaTransaction;
import com.meniga.sdk.models.transactions.MenigaTransactionPage;
import com.meniga.sdk.models.transactions.TransactionsFilter;
import com.meniga.sdk.providers.tasks.Continuation;
import com.meniga.sdk.providers.tasks.Task;
import com.meniga.sdk.webservices.APIRequest;
import com.meniga.sdk.webservices.HttpMethod;

import org.joda.time.DateTime;

import java.util.List;
import java.util.Map;

import butterknife.BindView;
import butterknife.ButterKnife;
import is.meniga.mobileandroidstarter.adapter.TransactionsAdapter;

public class TransactionListActivity extends AppCompatActivity {
    private TransactionsAdapter adapter;

    @BindView(R.id.toolbar)
    Toolbar toolbar;
    @BindView(R.id.transactions_list)
    RecyclerView list;
    @BindView(R.id.list_progress)
    View spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_transaction_list);

        ButterKnife.bind(this);

        setSupportActionBar(toolbar);

        adapter = new TransactionsAdapter();
        list.setLayoutManager(new LinearLayoutManager(this, LinearLayoutManager.VERTICAL, false));
        list.setAdapter(adapter);

        showBusy(true);

        MenigaTransaction.fetch(new TransactionsFilter.Builder()
                .period(DateTime.now().minusYears(5), DateTime.now())
                .page(1000, 0)
                .build()).getTask().continueWith(new Continuation<MenigaTransactionPage, Object>() {
            @Override
            public Object then(Task<MenigaTransactionPage> task) throws Exception {
                if (task.isFaulted()) {
                    Toast.makeText(TransactionListActivity.this, R.string.error_loading_transactions, Toast.LENGTH_SHORT).show();
                } else {
                    adapter.addViewItems(task.getResult());
                    adapter.notifyDataSetChanged();
                }

                showBusy(false);
                return null;
            }
        });

        APIRequest.genericRequest(HttpMethod.GET, BuildConfig.API_BASE_URL + "/me?includeAll=true").getTask().continueWith(new Continuation<Object, Object>() {
            @Override
            public Object then(Task<Object> task) throws Exception {
                if (!task.isFaulted()) {
                    LinkedTreeMap data = ((List<LinkedTreeMap>) ((Map)task.getResult()).get("data")).get(0);
                    toolbar.setTitle(data.get("email").toString());
                }
                return null;
            }
        }, Task.UI_THREAD_EXECUTOR);
    }

    private void showBusy(boolean show) {
        spinner.setVisibility(show ? View.VISIBLE : View.GONE);
    }
}
