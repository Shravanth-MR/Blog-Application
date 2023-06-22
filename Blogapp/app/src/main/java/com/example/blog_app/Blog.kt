package com.example.blog_app



import android.os.Parcel
import android.os.Parcelable
import com.google.firebase.Timestamp
import java.util.Date


data class Blog(
    val userId: String? = null,
    val title: String? = null,
    val description: String? = null,
    val timestamp: Date? = null,
    val image: String? = null,
    var username: String? = null,
    var profileImageUrl: String? = null
): Parcelable {
    // Implement Parcelable methods
    // ...

    constructor(parcel: Parcel) : this(
        // Read properties from parcel
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        // Write properties to parcel
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<Blog> {
        override fun createFromParcel(parcel: Parcel): Blog {
            return Blog(parcel)
        }

        override fun newArray(size: Int): Array<Blog?> {
            return arrayOfNulls(size)
        }
    }
}


class User {
    var username: String? = null
    var profileImageUrl: String? = null
}

