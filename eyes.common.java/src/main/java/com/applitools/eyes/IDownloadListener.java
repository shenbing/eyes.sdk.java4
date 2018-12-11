package com.applitools.eyes;

public interface IDownloadListener {
        void onDownloadComplete(String downloadedString);
        void onDownloadFailed();
}
