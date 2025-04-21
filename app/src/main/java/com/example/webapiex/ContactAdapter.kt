package com.example.webapiex

import android.content.Context
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.appcompat.app.AlertDialog
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject

class ContactAdapter(
    private val contactList: MutableList<Contact>,
    private val context: Context,
    private val onEditClick: (Contact, Int) -> Unit
) : RecyclerView.Adapter<ContactAdapter.ContactViewHolder>() {

    class ContactViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val nameText: TextView = itemView.findViewById(R.id.contact_name)
        val rollText: TextView = itemView.findViewById(R.id.contact_roll)
        val emailText: TextView = itemView.findViewById(R.id.contact_email)
        val imageView: CircleImageView = itemView.findViewById(R.id.contact_image)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ContactViewHolder {
        val view = LayoutInflater.from(context).inflate(R.layout.item_contact, parent, false)
        return ContactViewHolder(view)
    }

    override fun onBindViewHolder(holder: ContactViewHolder, position: Int) {
        val contact = contactList[position]
        holder.nameText.text = contact.name
        holder.rollText.text = "Roll: ${contact.roll}"
        holder.emailText.text = contact.email

        // Decode and display profile image using provided logic
        if (contact.dp.isNotEmpty()) {
            try {
                val decodedImage = Base64.decode(contact.dp, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                holder.imageView.setImageBitmap(bitmap)
            } catch (e: Exception) {
                holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
            }
        } else {
            holder.imageView.setImageResource(android.R.drawable.ic_menu_gallery)
        }

        // Edit contact on click
        holder.itemView.setOnClickListener {
            onEditClick(contact, position)
        }

        // Delete contact on long press
        holder.itemView.setOnLongClickListener {
            AlertDialog.Builder(context)
                .setTitle("Delete Contact")
                .setMessage("Are you sure you want to delete ${contact.name}?")
                .setPositiveButton("Yes") { _, _ ->
                    deleteContact(contact.id, position)
                }
                .setNegativeButton("No", null)
                .show()
            true
        }
    }

    override fun getItemCount(): Int = contactList.size

    private fun deleteContact(id: String, position: Int) {
        val queue = Volley.newRequestQueue(context)
        val url = "http://192.168.100.26/smd/delete.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optInt("status", 0)
                    if (status == 1) {
                        contactList.removeAt(position)
                        notifyItemRemoved(position)
                        notifyItemRangeChanged(position, contactList.size)
                    } else {
                        val message = jsonResponse.optString("message", "Unknown error")
                        android.widget.Toast.makeText(context, "Delete Failed: $message", android.widget.Toast.LENGTH_SHORT).show()
                    }
                } catch (e: Exception) {
                    android.widget.Toast.makeText(context, "Delete Failed: ${e.message}", android.widget.Toast.LENGTH_SHORT).show()
                }
            },
            { error ->
                android.widget.Toast.makeText(context, "Delete Failed: ${error.message}", android.widget.Toast.LENGTH_SHORT).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                return params
            }
        }
        queue.add(stringRequest)
    }
}