package com.example.cdmusicplayer.adapter

import android.app.Activity
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.cdmusicplayer.R
import com.example.cdmusicplayer.model.FamousArtistData
import com.squareup.picasso.Picasso

class FamousArtistAdapter(
    private val context: Activity,
    private val famousArtistDataList: List<FamousArtistData>,
    private val onItemClick: (position: Int) -> Unit
) : RecyclerView.Adapter<FamousArtistAdapter.FamousArtistViewHolder>() {
    class FamousArtistViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        var artistName: TextView
        var picture: ImageView

        init {
            artistName = itemView.findViewById(R.id.artistName)
            picture = itemView.findViewById(R.id.artistImg)
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): FamousArtistViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.artist_view_grid_item_ll, parent, false)
        return FamousArtistViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        return famousArtistDataList.size
    }

    override fun onBindViewHolder(holder: FamousArtistViewHolder, position: Int) {
        val gridItem=famousArtistDataList[position]

        holder.artistName.text=gridItem.name
        Picasso.get().load(gridItem.picture).into(holder.picture)

//        holder.itemView.setOnClickListener {
//            onItemClick(position)
//        }
    }


}