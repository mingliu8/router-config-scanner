package com.example.oltmanager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class OntAdapter extends RecyclerView.Adapter<OntAdapter.OntViewHolder> {
    private List<Ont> ontList;
    private OnRegisterClickListener registerClickListener;

    public interface OnRegisterClickListener {
        void onRegisterClick(Ont ont);
    }

    public OntAdapter(List<Ont> ontList, OnRegisterClickListener registerClickListener) {
        this.ontList = ontList;
        this.registerClickListener = registerClickListener;
    }

    @NonNull
    @Override
    public OntViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_ont, parent, false);
        return new OntViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull OntViewHolder holder, int position) {
        Ont ont = ontList.get(position);
        holder.tvSn.setText(ont.getSn());
        holder.tvPonPort.setText(ont.getPonPort());
        holder.tvStatus.setText(ont.getStatus());

        holder.btnRegister.setOnClickListener(v -> {
            if (registerClickListener != null) {
                registerClickListener.onRegisterClick(ont);
            }
        });
    }

    @Override
    public int getItemCount() {
        return ontList.size();
    }

    public static class OntViewHolder extends RecyclerView.ViewHolder {
        TextView tvSn;
        TextView tvPonPort;
        TextView tvStatus;
        Button btnRegister;

        public OntViewHolder(@NonNull View itemView) {
            super(itemView);
            tvSn = itemView.findViewById(R.id.tv_sn);
            tvPonPort = itemView.findViewById(R.id.tv_pon_port);
            tvStatus = itemView.findViewById(R.id.tv_status);
            btnRegister = itemView.findViewById(R.id.btn_register);
        }
    }
}