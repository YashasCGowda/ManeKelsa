package com.manekelsa.app.data.local

object BangaloreAreas {

    val allAreas = listOf(
        "Koramangala", "Whitefield", "Indiranagar", "HSR Layout", "Electronic City",
        "JP Nagar", "BTM Layout", "Malleshwaram", "Rajajinagar", "Jayanagar",
        "Basavanagudi", "Ulsoor", "Frazer Town", "Shivajinagar", "MG Road",
        "Brigade Road", "Sarjapur Road", "Bellandur", "Marathahalli", "KR Puram",
        "Yelahanka", "Hebbal", "Yeswanthpur", "Peenya", "Dasarahalli", "Kengeri",
        "Banashankari", "Padmanabhanagar", "Kumaraswamy Layout", "Bommanahalli",
        "Bommasandra", "Anekal", "Hoskote", "Devanahalli", "Nelamangala",
        "Magadi Road", "Vijayanagar", "Basaveshwara Nagar", "Nagarbhavi",
        "Moodalapalya", "Chandra Layout", "Rajarajeshwari Nagar", "Kengeri Satellite Town",
        "Wilson Garden", "Adugodi", "Domlur", "Cooke Town", "Kammanahalli", "Banaswadi"
    ).sorted()

    fun getRandomArea(): String {
        return allAreas.random()
    }
}
