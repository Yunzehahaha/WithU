package tech.yunze.withu.util

import android.content.Context
import android.transition.CircularPropagation

import android.widget.ImageView
import androidx.swiperefreshlayout.widget.CircularProgressDrawable
import com.bumptech.glide.Glide
import tech.yunze.withu.R
import com.bumptech.glide.request.RequestOptions

fun populateImage(context: Context?, url: String?, imageView: ImageView,errorDrawble: Int = R.drawable.empty){

    if(context != null){
        val options = RequestOptions()
            .placeholder(CircularProgressDrawable(context))
            .error(errorDrawble)
        Glide.with(context)
            .load(url)
            .apply(options)
            .into(imageView)
    }
}
fun progressDrawable(context: Context): CircularProgressDrawable{
    return CircularProgressDrawable(context).apply {
        strokeWidth = 5f
        centerRadius = 30f
        start()
    }
}