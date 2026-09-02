package com.example.personalisedfitnessmobileapplication.ui.health

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.CheckBox
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.personalisedfitnessmobileapplication.R
import com.example.personalisedfitnessmobileapplication.model.HealthTracking
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class HealthRecordsAdapter(
    private val records: List<HealthTracking>,
    private val onDelete: (HealthTracking) -> Unit,
    private val onUpdateStatus: (HealthTracking, Boolean) -> Unit
) : RecyclerView.Adapter<HealthRecordsAdapter.RecordViewHolder>() {

    class RecordViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val ivType: ImageView = view.findViewById(R.id.ivRecordType)
        val tvDate: TextView = view.findViewById(R.id.tvRecordDate)
        val tvMain: TextView = view.findViewById(R.id.tvRecordMain)
        val tvSub: TextView = view.findViewById(R.id.tvRecordSub)
        val cbCompleted: CheckBox = view.findViewById(R.id.cbGoalCompleted)
        val btnDelete: ImageButton = view.findViewById(R.id.btnDeleteRecord)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RecordViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_health_record, parent, false)
        return RecordViewHolder(view)
    }

    override fun onBindViewHolder(holder: RecordViewHolder, position: Int) {
        val record = records[position]

        val sdf = SimpleDateFormat("dd/MM/yyyy HH:mm", Locale.getDefault())
        // Since we moved to local storage, timestamp is now a Long
        val dateStr = sdf.format(Date(record.timestamp))
        holder.tvDate.text = "Date: $dateStr"

        if (record.type == "vitals") {
            holder.ivType.setImageResource(R.drawable.baseline_monitor_heart_24)
            holder.tvMain.text = "Weight: ${record.weight}kg | Height: ${record.height}cm"
            holder.tvSub.text = "Goal: ${record.goals}"
            holder.cbCompleted.visibility = View.VISIBLE
            holder.cbCompleted.isChecked = record.isCompleted

            holder.cbCompleted.setOnCheckedChangeListener { _, isChecked ->
                onUpdateStatus(record, isChecked)
            }
        } else if (record.type == "workout") {
            holder.ivType.setImageResource(R.drawable.baseline_device_thermostat_24)
            holder.tvMain.text = "${record.exerciseName} - ${record.equipment}"
            holder.tvSub.text = "${record.sets} Sets x ${record.reps} Reps | ${record.workoutWeight}kg"
            holder.cbCompleted.visibility = View.GONE
        }

        holder.btnDelete.setOnClickListener {
            onDelete(record)
        }
    }

    override fun getItemCount() = records.size
}