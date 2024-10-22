package com.example.footcard;

import android.graphics.drawable.Drawable;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.bumptech.glide.load.DataSource;
import com.bumptech.glide.load.engine.GlideException;
import com.bumptech.glide.request.RequestListener;
import com.bumptech.glide.request.target.Target;

import org.jetbrains.annotations.Nullable;

import java.util.List;

public class PlayerAdapter extends RecyclerView.Adapter<PlayerAdapter.PlayerViewHolder> {

    private List<Player> playerList;
    private String baseUrl = "http://10.0.2.2:8080";  // Update this as needed for production

    public PlayerAdapter(List<Player> playerList) {
        this.playerList = playerList;
    }

    // Method to reset the player list and notify the adapter
    public void setPlayers(List<Player> newPlayers) {
        this.playerList.clear();  // Clear the existing players
        this.playerList.addAll(newPlayers);  // Add the new players
        notifyDataSetChanged();  // Notify the adapter to refresh the UI
    }

    // Method to add new players to the list
    public void addPlayers(List<Player> newPlayers) {
        int startPosition = playerList.size();  // Get current size
        playerList.addAll(newPlayers);          // Add new players to the list
        notifyItemRangeInserted(startPosition, newPlayers.size());  // Notify the adapter
    }

    @NonNull
    @Override
    public PlayerViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.player_item, parent, false);
        return new PlayerViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PlayerViewHolder holder, int position) {
        Player player = playerList.get(position);

        // Set text values
        holder.playerName.setText(player.getName());
        holder.playerPosition.setText(player.getPosition());

        // Load images using Glide with error handling
        loadPlayerImage(holder.playerImage, baseUrl + player.getImageUrl());
        loadPlayerImage(holder.leagueImage, baseUrl + player.getLeagueImageUrl());
        loadPlayerImage(holder.teamImage, baseUrl + player.getTeamImageUrl());
        loadPlayerImage(holder.nationalityImage, baseUrl + player.getNationalityImageUrl());
    }

    @Override
    public int getItemCount() {
        return playerList.size();
    }

    private void loadPlayerImage(ImageView imageView, String imageUrl) {
        Glide.with(imageView.getContext())
                .load(imageUrl)
                .listener(new RequestListener<Drawable>() {
                    @Override
                    public boolean onLoadFailed(@Nullable GlideException e, Object model, Target<Drawable> target, boolean isFirstResource) {
                        Log.e("PlayerAdapter", "Image load failed for url: " + imageUrl, e);
                        return false;  // Important to return false so the error placeholder can be put
                    }

                    @Override
                    public boolean onResourceReady(Drawable resource, Object model, Target<Drawable> target, DataSource dataSource, boolean isFirstResource) {
                        return false;
                    }
                })
                .into(imageView);
    }

    public static class PlayerViewHolder extends RecyclerView.ViewHolder {
        public TextView playerName, playerPosition;
        public ImageView playerImage, leagueImage, teamImage, nationalityImage;

        public PlayerViewHolder(@NonNull View itemView) {
            super(itemView);

            // Bind the views
            playerName = itemView.findViewById(R.id.playerName);
            playerPosition = itemView.findViewById(R.id.playerPosition);
            playerImage = itemView.findViewById(R.id.playerImage);
            leagueImage = itemView.findViewById(R.id.leagueImage);
            teamImage = itemView.findViewById(R.id.teamImage);
            nationalityImage = itemView.findViewById(R.id.nationalityImage);
        }
    }
}
