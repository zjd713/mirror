
package com.vaporwarecorp.mirror.vendor.forecast.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;
import java.util.List;

public class Hourly {
// ------------------------------ FIELDS ------------------------------

    @SerializedName("data")
    @Expose
    public List<Datum> data = new ArrayList<>();
    @SerializedName("icon")
    @Expose
    public String icon;
    @SerializedName("summary")
    @Expose
    public String summary;
}
