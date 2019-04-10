package com.decomp.comp.decomp;


import android.os.Bundle;

import java.io.File;
import java.util.List;

import androidx.fragment.app.Fragment;

public class RetainFragment extends Fragment {

    int status;
    List<File> selFiles;
    boolean[] isChecked;
    boolean isSharingOrDeleting;

    public RetainFragment() {
        isSharingOrDeleting = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
