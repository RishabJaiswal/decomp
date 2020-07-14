package com.decomp.comp.decomp.features.home

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.decomp.comp.decomp.R
import com.decomp.comp.decomp.utils.extensions.getColorStateList
import com.decomp.comp.decomp.utils.extensions.visibleOrGone
import kotlinx.android.synthetic.main.item_task.view.*

class TasksListAdapter(
        private val list: List<Task>,
        val onTaskSelect: (task: Task) -> Unit
) : RecyclerView.Adapter<TasksListAdapter.TaskViewHolder>() {

    val VIEW_TYPE_EVEN = 0
    val VIEW_TYPE_ODD = 1

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): TaskViewHolder {
        val view = if (viewType == VIEW_TYPE_EVEN) {
            LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_task, parent, false)
        } else {
            LayoutInflater
                    .from(parent.context)
                    .inflate(R.layout.item_task_alt, parent, false)
        }
        return TaskViewHolder(view)
    }

    override fun getItemViewType(position: Int): Int {
        return if (position % 2 != 0) VIEW_TYPE_ODD else VIEW_TYPE_EVEN
    }

    override fun onBindViewHolder(holder: TaskViewHolder, position: Int) {
        holder.bind(list[position])
    }

    override fun getItemCount(): Int {
        return list.size
    }

    inner class TaskViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView), View.OnClickListener {

        fun bind(task: Task) {
            itemView.apply {
                tv_task_title.setText(task.title)
                tv_task_details.setText(task.details)
                tv_feature_ready.visibleOrGone(task.isFeatureReady.not())
                imv_task_art.setImageResource(task.art)
                btn_next.backgroundTintList = task.color.getColorStateList(context)
                setOnClickListener(this@TaskViewHolder)
            }
        }

        override fun onClick(v: View?) {
            onTaskSelect(list[bindingAdapterPosition])
        }
    }
}