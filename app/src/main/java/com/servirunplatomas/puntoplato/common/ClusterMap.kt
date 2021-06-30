package com.servirunplatomas.puntoplato.common

import com.google.android.gms.maps.model.LatLng
import com.google.maps.android.clustering.ClusterItem

class ClusterMap(private val nameItem: String, private val latlngItem: LatLng) : ClusterItem {

    override fun getSnippet(): String {
        return ""
    }

    override fun getTitle(): String {
        return nameItem
    }

    override fun getPosition(): LatLng {
        return latlngItem
    }
}