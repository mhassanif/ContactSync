package com.example.webapiex

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.net.Uri
import android.os.Build
import android.os.Bundle
import android.provider.Settings
import android.util.Base64
import android.util.Log
import android.view.LayoutInflater
import android.widget.EditText
import android.widget.Toast
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.android.volley.Request
import com.android.volley.toolbox.JsonObjectRequest
import com.android.volley.toolbox.StringRequest
import com.android.volley.toolbox.Volley
import com.google.android.material.floatingactionbutton.FloatingActionButton
import de.hdodenhof.circleimageview.CircleImageView
import org.json.JSONObject
import java.io.ByteArrayOutputStream

class MainActivity : AppCompatActivity() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var adapter: ContactAdapter
    private val contactList = mutableListOf<Contact>()
    private val baseUrl = "http://192.168.100.26/smd/" // Ensure server IP is correct
    private var selectedBitmap: Bitmap? = null
    private var currentDialogImageView: CircleImageView? = null

    // Activity Result Launcher for picking an image
    private val pickImageLauncher = registerForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        uri?.let {
            try {
                val inputStream = contentResolver.openInputStream(it)
                selectedBitmap = BitmapFactory.decodeStream(inputStream)
                inputStream?.close()
                currentDialogImageView?.setImageBitmap(selectedBitmap)
                Log.d("MainActivity", "Image loaded successfully from gallery")
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to load image", e)
                Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }
    }

    // Activity Result Launcher for requesting permission
    private val requestPermissionLauncher = registerForActivityResult(ActivityResultContracts.RequestPermission()) { isGranted ->
        if (isGranted) {
            pickImage()
        } else {
            val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                Manifest.permission.READ_MEDIA_IMAGES
            } else {
                Manifest.permission.READ_EXTERNAL_STORAGE
            }
            if (!shouldShowRequestPermissionRationale(permission)) {
                Toast.makeText(this, "Permission permanently denied. Please enable it in Settings > Apps > WebApiEx > Permissions.", Toast.LENGTH_LONG).show()
                val intent = Intent(Settings.ACTION_APPLICATION_DETAILS_SETTINGS)
                intent.data = Uri.fromParts("package", packageName, null)
                startActivity(intent)
            } else {
                Toast.makeText(this, "Permission denied. Please allow access to photos to select an image.", Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Initialize RecyclerView
        recyclerView = findViewById(R.id.recyclerView)
        adapter = ContactAdapter(contactList, this, ::showEditContactDialog)
        recyclerView.adapter = adapter

        // Set up FAB to add new contact
        findViewById<FloatingActionButton>(R.id.fab_add).setOnClickListener {
            showAddContactDialog()
        }

        // Fetch contacts on startup
        fetchContacts()
    }

    private fun showAddContactDialog() {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_add_contact, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_name)
        val editRoll = dialogView.findViewById<EditText>(R.id.edit_roll)
        val editEmail = dialogView.findViewById<EditText>(R.id.edit_email)
        val profileImage = dialogView.findViewById<CircleImageView>(R.id.profile_image)

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Handle image selection
        profileImage.setOnClickListener {
            currentDialogImageView = profileImage
            checkAndRequestPermission()
        }

        dialogView.findViewById<android.widget.Button>(R.id.button_save).setOnClickListener {
            val name = editName.text.toString().trim()
            val roll = editRoll.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val profileImageBase64 = selectedBitmap?.let { encodeImage(it) } ?: ""

            if (name.isEmpty() || roll.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            addContact(name, roll, email, profileImageBase64)
            selectedBitmap = null // Reset after adding
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.button_cancel).setOnClickListener {
            selectedBitmap = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun showEditContactDialog(contact: Contact, position: Int) {
        val dialogView = LayoutInflater.from(this).inflate(R.layout.dialog_edit_contact, null)
        val editName = dialogView.findViewById<EditText>(R.id.edit_name)
        val editRoll = dialogView.findViewById<EditText>(R.id.edit_roll)
        val editEmail = dialogView.findViewById<EditText>(R.id.edit_email)
        val profileImage = dialogView.findViewById<CircleImageView>(R.id.profile_image)

        // Pre-fill fields
        editName.setText(contact.name)
        editRoll.setText(contact.roll)
        editEmail.setText(contact.email)
        if (contact.dp.isNotEmpty()) {
            try {
                Log.d("MainActivity", "Decoding image for edit dialog, id: ${contact.id}, dp length: ${contact.dp.length}")
                val cleanBase64 = contact.dp.replace("\n", "").replace("\r", "").trim()
                val decodedImage = Base64.decode(cleanBase64, Base64.DEFAULT)
                val bitmap = BitmapFactory.decodeByteArray(decodedImage, 0, decodedImage.size)
                if (bitmap != null) {
                    profileImage.setImageBitmap(bitmap)
                    selectedBitmap = bitmap
                    Log.d("MainActivity", "Image decoded successfully for edit dialog, id: ${contact.id}")
                } else {
                    Log.e("MainActivity", "Bitmap is null for edit dialog, id: ${contact.id}")
                }
            } catch (e: IllegalArgumentException) {
                Log.e("MainActivity", "Invalid Base64 string for edit dialog, id: ${contact.id}", e)
                Toast.makeText(this, "Failed to load image: Invalid Base64", Toast.LENGTH_SHORT).show()
            } catch (e: Exception) {
                Log.e("MainActivity", "Failed to decode image for edit dialog, id: ${contact.id}", e)
                Toast.makeText(this, "Failed to load image: ${e.message}", Toast.LENGTH_SHORT).show()
            }
        }

        val dialog = AlertDialog.Builder(this)
            .setView(dialogView)
            .create()

        // Handle image selection
        profileImage.setOnClickListener {
            currentDialogImageView = profileImage
            checkAndRequestPermission()
        }

        dialogView.findViewById<android.widget.Button>(R.id.button_save).setOnClickListener {
            val name = editName.text.toString().trim()
            val roll = editRoll.text.toString().trim()
            val email = editEmail.text.toString().trim()
            val profileImageBase64 = selectedBitmap?.let { encodeImage(it) } ?: contact.dp

            if (name.isEmpty() || roll.isEmpty() || email.isEmpty()) {
                Toast.makeText(this, "Please fill all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            updateContact(contact.id, name, roll, email, profileImageBase64, position)
            selectedBitmap = null
            dialog.dismiss()
        }

        dialogView.findViewById<android.widget.Button>(R.id.button_cancel).setOnClickListener {
            selectedBitmap = null
            dialog.dismiss()
        }

        dialog.show()
    }

    private fun checkAndRequestPermission() {
        val permission = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            Manifest.permission.READ_MEDIA_IMAGES
        } else {
            Manifest.permission.READ_EXTERNAL_STORAGE
        }

        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            requestPermissionLauncher.launch(permission)
        } else {
            pickImage()
        }
    }

    private fun pickImage() {
        pickImageLauncher.launch("image/*")
    }

    private fun encodeImage(bitmap: Bitmap): String {
        val scaledBitmap = Bitmap.createScaledBitmap(bitmap, 100, 100, true) // Resize to 100x100
        val outputStream = ByteArrayOutputStream()
        scaledBitmap.compress(Bitmap.CompressFormat.JPEG, 80, outputStream) // Reduce quality
        val byteArray = outputStream.toByteArray()
        return Base64.encodeToString(byteArray, Base64.DEFAULT)
    }

    private fun fetchContacts() {
        val queue = Volley.newRequestQueue(this)
        val url = "${baseUrl}getContacts.php"

        val jsonObjectRequest = JsonObjectRequest(
            Request.Method.GET, url, null,
            { response ->
                try {
                    val status = response.optInt("status", 0)
                    val message = response.optString("message", "Unknown")
                    if (status == 1) {
                        contactList.clear()
                        val dataArray = response.getJSONArray("data")
                        for (i in 0 until dataArray.length()) {
                            val contactJson = dataArray.getJSONObject(i)
                            val dp = contactJson.optString("dp", "").replace("\n", "").replace("\r", "").trim()
                            Log.d("MainActivity", "Contact id: ${contactJson.optString("id", "0")}, dp length: ${dp.length}")
                            contactList.add(
                                Contact(
                                    id = contactJson.optString("id", "0"),
                                    name = contactJson.optString("name", "N/A"),
                                    roll = contactJson.optString("roll", "N/A"),
                                    email = contactJson.optString("email", "N/A"),
                                    dp = dp
                                )
                            )
                        }
                        adapter.notifyDataSetChanged()
                        Toast.makeText(this, "Fetched ${contactList.size} contacts", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MainActivity", "Fetch failed: $message")
                        Toast.makeText(this, "Fetch Failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Fetch contacts error", e)
                    Toast.makeText(this, "Fetch Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("MainActivity", "Fetch contacts Volley error", error)
                Toast.makeText(this, "Fetch Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        )
        queue.add(jsonObjectRequest)
    }

    private fun addContact(name: String, roll: String, email: String, profileImageBase64: String) {
        val queue = Volley.newRequestQueue(this)
        val url = "${baseUrl}insert.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optInt("status", 0)
                    val message = jsonResponse.optString("message", "Unknown")
                    if (status == 1) {
                        val id = jsonResponse.optString("id", "0")
                        contactList.add(Contact(id, name, roll, email, profileImageBase64))
                        adapter.notifyItemInserted(contactList.size - 1)
                        recyclerView.scrollToPosition(contactList.size - 1)
                        Toast.makeText(this, "Contact Added", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MainActivity", "Add contact failed: $message")
                        Toast.makeText(this, "Add Failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Add contact error", e)
                    Toast.makeText(this, "Add Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("MainActivity", "Add contact Volley error", error)
                Toast.makeText(this, "Add Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["name"] = name
                params["roll"] = roll
                params["email"] = email
                params["dp"] = profileImageBase64
                return params
            }
        }
        queue.add(stringRequest)
    }

    private fun updateContact(id: String, name: String, roll: String, email: String, profileImageBase64: String, position: Int) {
        val queue = Volley.newRequestQueue(this)
        val url = "${baseUrl}update.php"

        val stringRequest = object : StringRequest(
            Method.POST, url,
            { response ->
                try {
                    val jsonResponse = JSONObject(response)
                    val status = jsonResponse.optInt("status", 0)
                    val message = jsonResponse.optString("message", "Unknown")
                    if (status == 1) {
                        contactList[position] = Contact(id, name, roll, email, profileImageBase64)
                        adapter.notifyItemChanged(position)
                        Toast.makeText(this, "Contact Updated", Toast.LENGTH_SHORT).show()
                    } else {
                        Log.e("MainActivity", "Update contact failed: $message")
                        Toast.makeText(this, "Update Failed: $message", Toast.LENGTH_LONG).show()
                    }
                } catch (e: Exception) {
                    Log.e("MainActivity", "Update contact error", e)
                    Toast.makeText(this, "Update Failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
            },
            { error ->
                Log.e("MainActivity", "Update contact Volley error", error)
                Toast.makeText(this, "Update Failed: ${error.message}", Toast.LENGTH_LONG).show()
            }
        ) {
            override fun getParams(): Map<String, String> {
                val params = HashMap<String, String>()
                params["id"] = id
                params["name"] = name
                params["roll"] = roll
                params["email"] = email
                params["dp"] = profileImageBase64
                return params
            }
        }
        queue.add(stringRequest)
    }
}