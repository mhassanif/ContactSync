<?php

include "connection.php";
$response=array();

if(isset($_POST['id']))
{
    $id=$_POST['id'];
    $query="DELETE FROM `Users` WHERE `id`='$id';";
    // echo $query;
    $result=mysqli_query($con,$query);
    if($result)
    {
        $response["status"]= 1;
        $response["id"]= $id;
        $response["message"]= "Record Deleted";
    }
    else
    {
        $response["status"]= 0;
        $response["id"]= $id;
        $response["message"]= "Record not deleted";
    }

}
else
{
    $response["status"]= 0;
    $response["message"]= "Incomplete Request";
}

echo json_encode($response);
?>