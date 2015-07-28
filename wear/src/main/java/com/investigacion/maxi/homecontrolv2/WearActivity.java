package com.investigacion.maxi.homecontrolv2;

import android.app.Fragment;
import android.app.FragmentManager;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;
import android.support.wearable.view.CardFragment;
import android.support.wearable.view.FragmentGridPagerAdapter;
import android.support.wearable.view.WatchViewStub;
import android.widget.TextView;

import com.investigacion.maxi.shared.MockDb;

public class WearActivity extends FragmentActivity {

    private TextView mTextView;
    public static MockDb mockDb = new MockDb();
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_wear);
        final WatchViewStub stub = (WatchViewStub) findViewById(R.id.watch_view_stub);
        stub.setOnLayoutInflatedListener(new WatchViewStub.OnLayoutInflatedListener() {
            @Override
            public void onLayoutInflated(WatchViewStub stub) {
                mTextView = (TextView) stub.findViewById(R.id.text);
                mTextView.setText("Luz 3 Cuarto 1 estado:" + mockDb.getCuartos().get(2).getLuces().get(0).isPrendida());
            }
        });
    }

    private static final class GridPagerAdapter extends FragmentGridPagerAdapter {

        private GridPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public Fragment getFragment(int row, int column) {
            return (CardFragment.create(mockDb.getCuartos().get(row).getNombre(), mockDb.getCuartos().get(row).getLuces().get(column).getNombre()));
        }

        @Override
        public int getRowCount() {
            return mockDb.getCuartos().size();
        }

        @Override
        public int getColumnCount(int row) {
            return mockDb.getCuartos().get(row).getLuces().size();
        }
    }
}
