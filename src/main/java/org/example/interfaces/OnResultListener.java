package org.example.interfaces;

import org.example.Schedule;

public interface OnResultListener {
    public void onResult(Schedule result);
    public void onError(String msg);
}
