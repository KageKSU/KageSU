// IKsuInterface.aidl
package com.kageksu.zako;

import android.content.pm.PackageInfo;
import rikka.parcelablelist.ParcelableListSlice;

interface IKsuInterface {
    ParcelableListSlice<PackageInfo> getPackages(int flags);

    int[] getUserIds();
}