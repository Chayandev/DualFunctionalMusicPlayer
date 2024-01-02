package com.example.cdmusicplayer.adapter

import android.app.Activity
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.airbnb.lottie.LottieAnimationView
import com.example.cdmusicplayer.ApiData.Data
import com.example.cdmusicplayer.R
import com.squareup.picasso.Picasso


class MusicAdapter(
    private val context: Activity,
    private val dataList: List<Data>,
    private var selectedPosition: Int = RecyclerView.NO_POSITION,
    private val onItemClick: (position: Int, dataList: List<Data>) -> Unit
) :
    RecyclerView.Adapter<MusicAdapter.MusicViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MusicViewHolder {
        val itemView =
            LayoutInflater.from(context).inflate(R.layout.music_item_layout, parent, false)
        return MusicViewHolder(itemView)
    }

    override fun getItemCount(): Int {
        Log.d("Tag:Total Song Count","${dataList.size}")
        return dataList.size

    }

    override fun onBindViewHolder(holder: MusicViewHolder, position: Int) {
        //populate the data into view
        val currentData = dataList[position]

        holder.musicTitle.text = currentData.title
        holder.musicTitle.setHorizontallyScrolling(true)
        holder.musicTitle.isSelected=true
        holder.artistName.text = currentData.artist.name
        holder.artistName.setHorizontallyScrolling(true)
        holder.artistName.isSelected=true
        Picasso.get().load(currentData.album.cover_big).into(holder.musicImage)

        // Set background based on the selected position

        if (position == selectedPosition) {
            holder.itemView.setBackgroundResource(R.color.primary)
            holder.playingAnimation.visibility=View.VISIBLE
            holder.musicTitle.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.selectedItem
                )
            )
        } else {
            holder.itemView.setBackgroundResource(android.R.color.transparent)
            holder.playingAnimation.visibility=View.GONE
            holder.musicTitle.setTextColor(
                ContextCompat.getColor(
                    holder.itemView.context,
                    R.color.white
                )
            )
        }

        // Set click listener to handle item click
        holder.itemView.setOnClickListener {
            // Update the selected position
           //  setSelectedPosition(holder.adapterPosition)
            // Handle item click here...
           onItemClick(holder.absoluteAdapterPosition, dataList)
        }
    }
    fun setSelectedPosition(position: Int) {
        notifyItemChanged(selectedPosition)
        selectedPosition = position
        notifyItemChanged(selectedPosition)
    }
    class MusicViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        //create the view in case the layout manager fails to view for the data
        val musicImage: ImageView
        val musicTitle: TextView
        val artistName: TextView
        val playingAnimation:LottieAnimationView
        init {
            musicImage = itemView.findViewById(R.id.musicImg)
            musicTitle = itemView.findViewById(R.id.music_title)
            artistName = itemView.findViewById(R.id.artist_name)
            playingAnimation=itemView.findViewById(R.id.playLottieAnim)
        }
    }

}