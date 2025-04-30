package com.madkit.weatherapp.data.model


import com.google.gson.annotations.SerializedName

data class GeocodingResponse(
    @SerializedName("address")
    val address: Address,
    @SerializedName("addresstype")
    val addresstype: String,
    @SerializedName("boundingbox")
    val boundingbox: List<String>,
    @SerializedName("class")
    val classX: String,
    @SerializedName("display_name")
    val displayName: String,
    @SerializedName("importance")
    val importance: Double,
    @SerializedName("lat")
    val lat: String,
    @SerializedName("licence")
    val licence: String,
    @SerializedName("lon")
    val lon: String,
    @SerializedName("name")
    val name: String,
    @SerializedName("osm_id")
    val osmId: Long,
    @SerializedName("osm_type")
    val osmType: String,
    @SerializedName("place_id")
    val placeId: Int,
    @SerializedName("place_rank")
    val placeRank: Int,
    @SerializedName("type")
    val type: String
) {
    data class Address(
        @SerializedName("amenity")
        val amenity: String,
        @SerializedName("country")
        val country: String,
        @SerializedName("country_code")
        val countryCode: String,
        @SerializedName("ISO3166-2-lvl4")
        val iSO31662Lvl4: String,
        @SerializedName("postcode")
        val postcode: String,
        @SerializedName("region")
        val region: String,
        @SerializedName("road")
        val road: String,
        @SerializedName("state")
        val state: String,
        @SerializedName("suburb")
        val suburb: String,
        @SerializedName("town")
        val town: String
    )
}