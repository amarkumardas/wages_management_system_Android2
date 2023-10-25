package amar.das.acbook.ui.history;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;

import amar.das.acbook.databinding.FragmentHistoryTabBinding;



public class HistoryFragment extends Fragment {
    private FragmentHistoryTabBinding binding;

    public View onCreateView(@NonNull LayoutInflater inflater,ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentHistoryTabBinding.inflate(inflater, container, false);
        View root = binding.getRoot();




        return root;
    }
    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}