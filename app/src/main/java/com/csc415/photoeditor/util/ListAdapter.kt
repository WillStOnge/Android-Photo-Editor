package com.csc415.photoeditor.util

import android.content.Context
import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import androidx.recyclerview.widget.RecyclerView
import com.csc415.photoeditor.R
import java.lang.ref.WeakReference
import java.util.*

/**
 * This ListAdapter supports a ButtonViewHolder for the clickable Buttons defined in
 * PhotoEditorActivity. the OnClick Listeners for the buttons are defined in  PhotoEditorActivity
 *
 * @author Alex Smithson
 */

class ListAdapter(context: Context?, list: LinkedList<EditorButtonEnum>,
                  private val listener: ClickListener?) :
    RecyclerView.Adapter<ListAdapter.ButtonViewHolder>() {
    private val mList: LinkedList<EditorButtonEnum> = list
    private var mInflater: LayoutInflater = LayoutInflater.from(context)

    /**
     * returns a ButtonViewHolder for an editor_list_item
     *
     * @param parent
     * @param viewType
     * @return
     */
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ButtonViewHolder {
        val mItemView: View
        val resource: Int = R.layout.editor_list_item
        mItemView = mInflater.inflate(resource, parent, false)
        return ButtonViewHolder(mItemView, listener)
    }

    /**
     *
     * @param holder
     * @param position
     */
    override fun onBindViewHolder(holder: ButtonViewHolder, position: Int) {
        val mCurrent : EditorButtonEnum = mList[position]
        holder.mEditorButton.text = mCurrent.text
    }

    override fun getItemCount(): Int {
        return mList.size
    }

    /**
     * ViewHolder of clickable Buttons
     */
    inner class ButtonViewHolder(itemView: View, listener: ClickListener?) :
        RecyclerView.ViewHolder(itemView), View.OnClickListener {
        val mEditorButton: Button = itemView.findViewById(R.id.button_editor)
        private val listenerRef: WeakReference<ClickListener> = WeakReference<ClickListener>(listener)

        init {
            mEditorButton.setOnClickListener(this)
        }
        /**
         * Calls onClickListener originally passed in as a parameter to ListAdapter
         * @param view
         */
        override fun onClick(view: View) {
            listenerRef.get()?.onPositionClicked(adapterPosition)
        }
    }
}