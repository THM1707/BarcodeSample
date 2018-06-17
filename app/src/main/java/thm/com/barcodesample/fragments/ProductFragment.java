package thm.com.barcodesample.fragments;

import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import thm.com.barcodesample.R;

public class ProductFragment extends Fragment {

    private int mNum;
    private TextView mTextDisplay;

    public static ProductFragment newInstance(int num){
        ProductFragment fragment = new ProductFragment();
        Bundle args = new Bundle();
        args.putInt("num", num);
        fragment.setArguments(args);
        return fragment;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mNum = getArguments().getInt("num");
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container,
                             Bundle savedInstanceState) {
        ViewGroup rootView = (ViewGroup) inflater.inflate(
            R.layout.fragment_product, container, false);
        mTextDisplay = rootView.findViewById(R.id.tv_display);
        mTextDisplay.setText(String.valueOf(mNum));
        return rootView;
    }
}
