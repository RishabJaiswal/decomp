package com.decomp.comp.decomp;

import android.os.Bundle;
import android.support.v4.app.Fragment;

import java.io.File;
import java.util.List;

public class RetainFragment extends Fragment
{

    int status;
    List<File> selFiles;
    boolean[] isChecked;
    boolean isSharingOrDeleting;

    public RetainFragment()
    {
        isSharingOrDeleting = false;
    }

    @Override
    public void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);
        setRetainInstance(true);
    }
}
