<?php
include "connection.php";
$response = array();

if (isset($_POST['id'], $_POST['name'], $_POST['roll'], $_POST['email'], $_POST['dp'])) {
    $id = $_POST['id'];
    $name = $_POST['name'];
    $roll = $_POST['roll'];
    $email = $_POST['email'];
    $dp = $_POST['dp'];

    $query = "UPDATE `Users` SET `name`='$name', `roll`='$roll', `email`='$email', `dp`='$dp' WHERE `id`='$id';";
    $result = mysqli_query($con, $query);
    if ($result) {
        $response["status"] = 1;
        $response["message"] = "Record Updated";
    } else {
        $response["status"] = 0;
        $response["message"] = "Record not updated";
    }
} else {
    $response["status"] = 0;
    $response["message"] = "Incomplete Request";
}

echo json_encode($response);
?>