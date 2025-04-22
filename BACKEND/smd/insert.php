<?php
include "connection.php";
$response = array();

if (isset($_POST['name'], $_POST['roll'], $_POST['email'], $_POST['dp'])) {
    $name = $_POST['name'];
    $roll = $_POST['roll'];
    $email = $_POST['email'];
    $dp = $_POST['dp'];

    $query = "INSERT INTO `Users` (`name`, `roll`, `email`, `dp`) 
              VALUES ('$name', '$roll', ' $email', '$dp');";
    $result = mysqli_query($con, $query);
    if ($result) {
        $response["status"] = 1;
        $response["id"] = mysqli_insert_id($con);
        $response["message"] = "Data inserted successfully";
    } else {
        $response["status"] = 0;
        $response["id"] = -1;
        $response["message"] = "Data not inserted";
    }
} else {
    $response["status"] = 0;
    $response["id"] = -1;
    $response["message"] = "Incomplete Request";
}

echo json_encode($response);
?>