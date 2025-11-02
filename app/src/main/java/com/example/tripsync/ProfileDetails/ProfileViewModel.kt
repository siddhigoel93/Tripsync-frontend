package com.example.tripsync.ProfileDetails

import android.net.Uri
import androidx.lifecycle.ViewModel

class ProfileViewModel : ViewModel() {
    var firstName: String = ""
    var lastName: String = ""
    var phoneNumber: String = ""
    var personalPhoneDigits: String = ""
    var dob: String = ""
    var gender: String = "male"
    var aboutMe: String = ""
    var imageUri: Uri? = null

    var bgroup: String = ""
    var allergies: String = ""
    var medical: String = ""

    var ename: String = ""
    var enumberRaw: String = ""
    var erelation: String = ""

    var preference: String = ""
}
