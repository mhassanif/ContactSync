# ContactSync - Android Contact Manager

## Description

**ContactSync** is a modern Android application built with Kotlin, designed for seamless contact management through a RESTful web API. The app enables users to create, read, update, and delete (CRUD) contacts, complete with Base64-encoded profile images, stored and retrieved from a PHP/MySQL backend. Featuring a clean interface with a `RecyclerView` and dialog-based interactions, ContactSync demonstrates robust API integration, efficient image processing, and a user-friendly experience, showcasing my ability to build functional, connected mobile apps.

## Features

- **Full CRUD Functionality**: Add, view, edit, and delete contacts with name, roll number, email, and optional profile images, synced with a backend server.
- **Profile Image Handling**: Select images from the gallery, encode them to Base64 for storage, and decode for display in circular `CircleImageView` components.
- **RESTful API Integration**: Communicate with a PHP/MySQL backend using Volley to perform real-time data operations, handling JSON responses and errors gracefully.
- **Intuitive UI**: Display contacts in a scrollable `RecyclerView`, with a Floating Action Button for adding contacts and dialogs for editing or deleting.
- **Robust Error Handling**: Manage network failures, invalid Base64 strings, and permission issues, ensuring a smooth user experience.

## Technologies

- **Android**: Kotlin, Android SDK, RecyclerView, Volley, CircleImageView (`de.hdodenhof.circleimageview`).
- **Image Processing**: Base64 encoding/decoding, BitmapFactory for image rendering.
- **Backend**: PHP, MySQL (API endpoints: `insert.php`, `update.php`, `delete.php`, `getContacts.php`).
- **Tools**: Android Studio, Git, Postman, MySQL Workbench.

## Screenshots

![image](https://github.com/user-attachments/assets/be497b24-5c00-4acb-938e-4f57b12132b2)
![image](https://github.com/user-attachments/assets/0968085f-28fe-47dc-9c76-47ed8904c85c)
![image](https://github.com/user-attachments/assets/80f3a235-2d1a-40ed-b8fc-95c214995971)




## Prerequisites

- Android device/emulator (API 23+).
- Web server with PHP 7.4+, MySQL 5.7+, hosting API endpoints at `http://<your-server-ip>/smd/`.
- MySQL database with `contacts` table:

  ```sql
  CREATE TABLE contacts (
      id INT AUTO_INCREMENT PRIMARY KEY,
      name VARCHAR(255) NOT NULL,
      roll VARCHAR(50) NOT NULL,
      email VARCHAR(255) NOT NULL,
      dp LONGTEXT
  );
  ```
- Android Studio (2023.3.1+), JDK 17, Gradle 8.0+.

## Setup

1. **Clone the Repository**:

   ```bash
   git clone https://github.com/your-username/ContactSync.git
   cd ContactSync
   ```

2. **Set Up Backend**:

   - Deploy PHP scripts (`insert.php`, `update.php`, `delete.php`, `getContacts.php`) to your server (e.g., `/var/www/html/smd/`).
   - Create the MySQL database and table (see above).
   - Update database credentials in PHP scripts.
   - Ensure the server is accessible (e.g., `http://192.168.100.26/smd/`).

3. **Configure the App**:

   - Open the project in Android Studio and sync Gradle.
   - Update `baseUrl` in `MainActivity.kt`:

     ```kotlin
     private val baseUrl = "http://<your-server-ip>/smd/"
     ```

4. **Run**:

   - Connect a device or start an emulator.
   - Run the app from Android Studio.
   - Grant storage permissions for image uploads.

## Project Structure

```
ContactSync/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/contactsync/
│   │   │   ├── Contact.kt          # Contact data class
│   │   │   ├── ContactAdapter.kt   # RecyclerView adapter
│   │   │   ├── MainActivity.kt     # Main UI and API logic
│   │   ├── res/layout/
│   │   │   ├── activity_main.xml         # Main UI
│   │   │   ├── item_contact.xml          # Contact item
│   │   │   ├── dialog_add_contact.xml    # Add dialog
│   │   │   ├── dialog_edit_contact.xml   # Edit dialog
├── backend/ (assumed)
│   ├── insert.php, update.php, delete.php, getContacts.php
```

